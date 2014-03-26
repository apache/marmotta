/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.services.http;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.cache.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.client.cache.DefaultHttpCacheEntrySerializer;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.*;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.http.HttpClientService;
import org.apache.marmotta.platform.core.api.statistics.StatisticsModule;
import org.apache.marmotta.platform.core.api.statistics.StatisticsService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.model.config.CoreOptions;
import org.apache.marmotta.platform.core.qualifiers.cache.MarmottaCache;
import org.apache.marmotta.platform.core.services.http.response.LastModifiedResponseHandler;
import org.apache.marmotta.platform.core.services.http.response.StatusCodeResponseHandler;
import org.apache.marmotta.platform.core.services.http.response.StringBodyResponseHandler;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 **/
@ApplicationScoped
public class HttpClientServiceImpl implements HttpClientService {

    private static final String          TASK_GROUP_CLIENT       = "HttpClient";

    private static final String[]        KEYS                    = {
        "requests executed", "payload sent", "payload received", "payload from cache",
        "ConnectionManager", "max connections", "available connections",
        "active connections", "requests waiting"
    };

    private static final Charset         DEFAULT_CHARSET         = Charset.defaultCharset();

    @Inject
    private Logger                       log;

    @Inject
    private TaskManagerService           taskManagerService;

    @Inject
    private ConfigurationService         configurationService;

    @Inject
    private StatisticsService            statisticsService;

    @Inject
    @MarmottaCache("http-client-cache")
    private ConcurrentMap httpCache;

    private HttpClient                   httpClient;
    private IdleConnectionMonitorThread  idleConnectionMonitorThread;
    private BasicHttpParams              httpParams;

    private  AtomicLong             bytesSent               = new AtomicLong();
    private  AtomicLong             bytesReceived           = new AtomicLong();
    private  AtomicLong             bytesFromCache          = new AtomicLong();
    private  AtomicLong             requestsExecuted        = new AtomicLong();

    private  ReentrantReadWriteLock lock                    = new ReentrantReadWriteLock();

    /**
     * Execute a request and pass the response to the provided handler.
     * 
     * @param request
     * @param handler
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Override
    public <T> T execute(HttpRequestBase request, ResponseHandler<? extends T> handler) throws ClientProtocolException, IOException {
        if (handler == null)
            throw new IllegalArgumentException("Response handler must not be null.");
        final long start = System.nanoTime();
        String logMessageF = "Request '{}' failed after {}";
        try {
            final T result = httpClient.execute(request, handler);
            logMessageF = "Request '{}' took {}";
            return result;
        } finally {
            log.debug(logMessageF, request.getRequestLine(), formatNanoDuration(System.nanoTime() - start));
        }
    }

    /**
     * Get a ready-to-use {@link HttpClient}.
     */
    @Override
    public HttpClient getHttpClient() {
        return new ReadLockHttpClient();
    }

    /**
     * Execute a request.
     * 
     * <b>ATTENTION</b> Make sure to close the {@link InputStream} in the {@link HttpEntity}, or
     * just use {@link #cleanupResponse(HttpResponse)}!
     * 
     * @param request
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @see #cleanupResponse(HttpResponse)
     * @see EntityUtils#consume(HttpEntity)
     */
    @Override
    public HttpResponse execute(HttpRequestBase request) throws ClientProtocolException, IOException {
        final long start = System.nanoTime();
        String logMessageF = "Request '{}' failed after {}";
        try {
            final HttpResponse resp = httpClient.execute(request);
            logMessageF = "Request '{}' took {}";
            return resp;
        } finally {
            log.debug(logMessageF, request.getRequestLine(), formatNanoDuration(System.nanoTime() - start));
        }
    }

    @Override
    public void cleanupResponse(HttpResponse response) {
        if (response == null) throw new IllegalArgumentException("Response must not be null");
        EntityUtils.consumeQuietly(response.getEntity());
    }

    private static String formatNanoDuration(long nano) {
        // convert to microseconds (1/1000s)
        final long micro = nano / 1000;
        if (micro > 1000 * 1000l) {
            // more than a second
            long millis = micro / 1000l;
            if (millis > 60000l)
                // more than a minute
                return String.format("%d min %.1f sec", millis / (1000 * 60), 0.001d * millis % 60);
            else
                // more than a second
                return String.format("%.1f sec", 0.001d * millis);
        } else
            // some millis
            return String.format("%f ms", 0.001d * micro);
    }

    @Override
    public String doGet(String requestUrl) throws IOException {
        return doGet(requestUrl, new StringBodyResponseHandler());
    }

    @Override
    public <T> T doGet(String requestUrl, ResponseHandler<? extends T> responseHandler) throws IOException {
        final HttpGet get = new HttpGet(requestUrl);

        return execute(get, responseHandler);
    }

    @Override
    public <T> T doPost(String requestUrl, HttpEntity body, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpPost post = new HttpPost(requestUrl);
        post.setEntity(body);

        return execute(post, responseHandler);
    }

    @Override
    public String doPost(String url, String body) throws IOException {
        return doPost(url, new StringEntity(body, ContentType.create("text/plain", DEFAULT_CHARSET)), new StringBodyResponseHandler());
    }

    @Override
    public <T> T doPut(String url, HttpEntity body, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpPut put = new HttpPut(url);
        put.setEntity(body);

        return execute(put, responseHandler);
    }

    @Override
    public String doPut(String url, String body) throws IOException {
        return doPut(url, new StringEntity(body, ContentType.create("text/plain", DEFAULT_CHARSET)), new StringBodyResponseHandler());
    }

    @Override
    public <T> T doDelete(String url, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpDelete delete = new HttpDelete(url);

        return execute(delete, responseHandler);
    }

    @Override
    public int doDelete(String url) throws IOException {
        return doDelete(url, new StatusCodeResponseHandler());
    }

    @Override
    public <T> T doHead(String url, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpHead head = new HttpHead(url);

        return execute(head, responseHandler);
    }

    @Override
    public Date doHead(String url) throws IOException {
        return doHead(url, new LastModifiedResponseHandler());
    }

    protected void onConfigurationChange(@Observes ConfigurationChangedEvent event) {
        if (event.containsChangedKeyWithPrefix("core.http.")) {
            try {
                lock.writeLock().lock();
                shutdown();
                initialize();
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    @PostConstruct
    protected void initialize() {
        try {
            lock.writeLock().lock();

            httpParams = new BasicHttpParams();
            String userAgentString =
                    "Apache Marmotta/" + configurationService.getStringConfiguration("kiwi.version") +
                    " (running at " + configurationService.getServerUri() + ")" +
                    " lmf-core/" + configurationService.getStringConfiguration("kiwi.version");
            userAgentString = configurationService.getStringConfiguration("core.http.user_agent", userAgentString);

            httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, configurationService.getIntConfiguration("core.http.so_timeout", 60000));
            httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                    configurationService.getIntConfiguration("core.http.connection_timeout", 10000));

            httpParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
            httpParams.setIntParameter(ClientPNames.MAX_REDIRECTS, 3);

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

            PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
            cm.setMaxTotal(configurationService.getIntConfiguration(CoreOptions.HTTP_MAX_CONNECTIONS, 20));
            cm.setDefaultMaxPerRoute(configurationService.getIntConfiguration(CoreOptions.HTTP_MAX_CONNECTIONS_PER_ROUTE, 10));

            final DefaultHttpClient hc = new DefaultHttpClient(cm, httpParams);
            hc.setRedirectStrategy(new LMFRedirectStrategy());
            hc.setHttpRequestRetryHandler(new LMFHttpRequestRetryHandler());
            hc.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
            hc.addRequestInterceptor(new LMFRequestUserAgent(userAgentString));

            if (configurationService.getBooleanConfiguration(CoreOptions.HTTP_CLIENT_CACHE_ENABLE, true)) {
                CacheConfig cacheConfig = new CacheConfig();
                // FIXME: Hardcoded constants - is this useful?
                cacheConfig.setMaxCacheEntries(1000);
                cacheConfig.setMaxObjectSize(81920);

                final HttpCacheStorage cacheStore = new MapHttpCacheStorage(httpCache);

                this.httpClient = new MonitoredHttpClient(new CachingHttpClient(hc, cacheStore, cacheConfig));
            } else {
                this.httpClient = new MonitoredHttpClient(hc);
            }
            bytesSent.set(0);
            bytesReceived.set(0);
            requestsExecuted.set(0);

            idleConnectionMonitorThread = new IdleConnectionMonitorThread(httpClient.getConnectionManager());
            idleConnectionMonitorThread.start();

            StatisticsProvider stats = new StatisticsProvider(cm);
            statisticsService.registerModule(HttpClientService.class.getSimpleName(), stats);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @PreDestroy
    protected void shutdown() {
        try {
            lock.writeLock().lock();
            statisticsService.unregisterModule(HttpClientService.class.getSimpleName());
            idleConnectionMonitorThread.shutdown();
            httpClient.getConnectionManager().shutdown();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static class LMFRequestUserAgent implements HttpRequestInterceptor {

        private final String baseUserAgent;

        public LMFRequestUserAgent(String baseUserAgent) {
            this.baseUserAgent = baseUserAgent;
        }

        private final String buildUserAgentString(String localPart) {
            if (localPart == null || localPart.length() == 0)
                return baseUserAgent;
            else if (localPart.endsWith(baseUserAgent)) return localPart;
            return localPart + " " + baseUserAgent;
        }

        @Override
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            if (request == null) throw new IllegalArgumentException("HTTP request must not be null");
            if (!request.containsHeader(HTTP.USER_AGENT)) {
                String useragent = HttpProtocolParams.getUserAgent(request.getParams());
                request.addHeader(HTTP.USER_AGENT, buildUserAgentString(useragent));
            } else {
                String useragent = request.getFirstHeader(HTTP.USER_AGENT).getValue();
                request.setHeader(HTTP.USER_AGENT, buildUserAgentString(useragent));
            }
        }

    }

    private static class LMFRedirectStrategy extends DefaultRedirectStrategy {
        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            if (request == null)
                throw new IllegalArgumentException("HTTP request must not be null");
            if (response == null)
                throw new IllegalArgumentException("HTTP response must not be null");

            int statusCode = response.getStatusLine().getStatusCode();
            String method = request.getRequestLine().getMethod();
            Header locationHeader = response.getFirstHeader("location");
            switch (statusCode) {
            case HttpStatus.SC_MOVED_TEMPORARILY:
                return (method.equalsIgnoreCase(HttpGet.METHOD_NAME) || method.equalsIgnoreCase(HttpHead.METHOD_NAME))
                        && locationHeader != null;
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_TEMPORARY_REDIRECT:
                return method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpHead.METHOD_NAME);
            case HttpStatus.SC_SEE_OTHER:
                return true;
            case HttpStatus.SC_MULTIPLE_CHOICES:
                return true;
            default:
                return false;
            } // end of switch
        }
    }

    private static class LMFHttpRequestRetryHandler implements HttpRequestRetryHandler {
        /**
         * Determines if a method should be retried after an IOException occurs
         * during execution.
         * 
         * @param exception
         *            the exception that occurred
         * @param executionCount
         *            the number of times this method has been unsuccessfully
         *            executed
         * @param context
         *            the context for the request execution
         * @return <code>true</code> if the method should be retried, <code>false</code> otherwise
         */
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return false;
        }
    }

    private static class IdleConnectionMonitorThread extends Thread {

        private final ClientConnectionManager connMgr;
        private volatile boolean              shutdown;

        public IdleConnectionMonitorThread(ClientConnectionManager connMgr) {
            super("HttpClientService Idle Connection Manager");
            this.connMgr = connMgr;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                // terminate
            }
        }

        public void shutdown() {
            synchronized (this) {
                shutdown = true;
                notifyAll();
            }
        }

    }

    private class StatisticsProvider implements StatisticsModule {

        private boolean                        enabled;
        private PoolingClientConnectionManager connectionManager;

        public StatisticsProvider(PoolingClientConnectionManager cm) {
            this.connectionManager = cm;
            enabled = true;
        }

        @Override
        public void enable() {
            enabled = true;
        }

        @Override
        public void disable() {
            enabled = false;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public List<String> getPropertyNames() {
            return Collections.unmodifiableList(Arrays.asList(KEYS));
        }

        @Override
        public Map<String, String> getStatistics() {
            int i = 0;
            final Map<String, String> data = new LinkedHashMap<String, String>();
            data.put(KEYS[i++], String.valueOf(requestsExecuted.get()));
            data.put(KEYS[i++], humanReadableBytes(bytesSent.get(), false));
            data.put(KEYS[i++], humanReadableBytes(bytesReceived.get(), false));
            data.put(KEYS[i++], humanReadableBytes(bytesFromCache.get(), false));

            final PoolStats cmStats = connectionManager.getTotalStats();
            data.put(KEYS[i++], connectionManager.getClass().getSimpleName());
            data.put(KEYS[i++], String.valueOf(cmStats.getMax()));
            data.put(KEYS[i++], String.valueOf(cmStats.getAvailable()));
            data.put(KEYS[i++], String.valueOf(cmStats.getLeased()));
            data.put(KEYS[i++], String.valueOf(cmStats.getPending()));
            return data;
        }

        private String humanReadableBytes(long bytes, boolean si) {
            final int unit = si ? 1000 : 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
            return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
        }

        @Override
        public String getName() {
            return HttpClientService.class.getSimpleName();
        }

    }

    private class ReadLockHttpClient implements HttpClient {

        private HttpParams params;

        public ReadLockHttpClient() {
            this.params = new DefaultedHttpParams(new BasicHttpParams(), httpParams);
        }

        @Override
        public HttpParams getParams() {
            return params;
        }

        @Override
        public ClientConnectionManager getConnectionManager() {
            return httpClient.getConnectionManager();
        }

        @Override
        public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
            lock.readLock().lock();
            try {
                return httpClient.execute(request);
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
            lock.readLock().lock();
            try {
                return httpClient.execute(request, context);
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
            lock.readLock().lock();
            try {
                return httpClient.execute(target, request);
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
            lock.readLock().lock();
            try {
                return httpClient.execute(target, request, context);
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
            lock.readLock().lock();
            try {
                return httpClient.execute(request, responseHandler);
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException,
        ClientProtocolException {
            lock.readLock().lock();
            try {
                return httpClient.execute(request, responseHandler, context);
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException,
        ClientProtocolException {
            lock.readLock().lock();
            try {
                return httpClient.execute(target, request, responseHandler);
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
                throws IOException, ClientProtocolException {
            lock.readLock().lock();
            try {
                return httpClient.execute(target, request, responseHandler, context);
            } finally {
                lock.readLock().unlock();
            }
        }

    }

    protected class MonitoredHttpClient implements HttpClient {

        private final HttpClient delegate;

        public MonitoredHttpClient(HttpClient delegate) {
            this.delegate = delegate;
        }

        @Override
        public HttpParams getParams() {
            return delegate.getParams();
        }

        @Override
        public ClientConnectionManager getConnectionManager() {
            return delegate.getConnectionManager();
        }

        @Override
        public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
            final Task task = preProcess(request);

            final HttpResponse response;
            lock.readLock().lock();
            try {
                response = delegate.execute(request);
            } catch (ClientProtocolException cpe) {
                task.endTask();
                throw cpe;
            } catch (IOException io) {
                task.endTask();
                throw io;
            } finally {
                lock.readLock().unlock();
            }

            return postProcess(response, null, task);
        }

        @Override
        public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
            final Task task = preProcess(request);

            final HttpResponse response;
            lock.readLock().lock();
            try {
                response = delegate.execute(request, context);
            } catch (ClientProtocolException cpe) {
                task.endTask();
                throw cpe;
            } catch (IOException io) {
                task.endTask();
                throw io;
            } finally {
                lock.readLock().unlock();
            }

            return postProcess(response, context, task);
        }

        private Task preProcess(HttpRequest request) {
            final RequestLine rl = request.getRequestLine();
            final String taskName = String.format("%S %s %S", rl.getMethod(), rl.getUri(), request.getProtocolVersion());

            final Task task = taskManagerService.createSubTask(taskName, TASK_GROUP_CLIENT);
            task.updateMessage("preparing request");
            task.updateDetailMessage("method", rl.getMethod());
            task.updateDetailMessage("url", rl.getUri());
            // TODO: some more detail messages?

            if (request instanceof HttpEntityEnclosingRequest) {
                // To report upload progress, the entity is wrapped in a MonitoredHttpEntity.
                final HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
                entityRequest.setEntity(new MonitoredHttpEntity(entityRequest.getEntity(), task, bytesSent));
            }

            task.updateMessage("sending request");
            return task;
        }

        private HttpResponse postProcess(final HttpResponse response, HttpContext context, final Task task) {
            requestsExecuted.incrementAndGet();
            task.resetProgress();
            task.updateMessage("retrieving response");
            if (response.getEntity() != null) {
                boolean cachedResponse;
                if (context != null) {
                    CacheResponseStatus cacheRespStatus = (CacheResponseStatus) context.getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS);
                    // To report download progress, the entity is wrapped in a MonitoredHttpEntity.
                    cachedResponse = cacheRespStatus != null && cacheRespStatus != CacheResponseStatus.CACHE_MISS;
                } else {
                    cachedResponse = false;
                }
                response.setEntity(new MonitoredHttpEntity(response.getEntity(), task, cachedResponse ? bytesFromCache : bytesReceived));
            } else {
                task.endTask();
            }
            return response;
        }

        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
            final Task task = preProcess(request);

            final HttpResponse response;
            lock.readLock().lock();
            try {
                response = delegate.execute(target, request);
            } catch (ClientProtocolException cpe) {
                task.endTask();
                throw cpe;
            } catch (IOException io) {
                task.endTask();
                throw io;
            } finally {
                lock.readLock().unlock();
            }

            return postProcess(response, null, task);
        }

        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
            final Task task = preProcess(request);

            final HttpResponse response;
            lock.readLock().lock();
            try {
                response = delegate.execute(target, request, context);
            } catch (ClientProtocolException cpe) {
                task.endTask();
                throw cpe;
            } catch (IOException io) {
                task.endTask();
                throw io;
            } finally {
                lock.readLock().unlock();
            }

            return postProcess(response, context, task);
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
            final HttpResponse response = execute(request);

            return processResponse(responseHandler, response);
        }

        private <T> T processResponse(ResponseHandler<? extends T> responseHandler, final HttpResponse response) throws ClientProtocolException,
        IOException {
            try {
                return responseHandler.handleResponse(response);
            } finally {
                // Make sure everything is cleaned up properly
                EntityUtils.consume(response.getEntity());
            }
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException,
        ClientProtocolException {
            final HttpResponse response = execute(request, context);

            return processResponse(responseHandler, response);
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException,
        ClientProtocolException {
            final HttpResponse response = execute(target, request);

            return processResponse(responseHandler, response);
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
                throws IOException, ClientProtocolException {
            final HttpResponse response = execute(target, request, context);

            return processResponse(responseHandler, response);
        }

    }



    private static class MapHttpCacheStorage implements HttpCacheStorage {

        ConcurrentMap<String, byte[]> cache;

        private final HttpCacheEntrySerializer serializer;


        private MapHttpCacheStorage(ConcurrentMap<String, byte[]> cache) {
            this.cache      = cache;
            this.serializer = new DefaultHttpCacheEntrySerializer();
        }

        /**
         * Store a given cache entry under the given key.
         *
         * @param key   where in the cache to store the entry
         * @param entry cached response to store
         * @throws java.io.IOException
         */
        @Override
        public void putEntry(String key, HttpCacheEntry entry) throws IOException {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            serializer.writeTo(entry, bos);

            cache.put(key,bos.toByteArray());
        }

        /**
         * Retrieves the cache entry stored under the given key
         * or null if no entry exists under that key.
         *
         * @param key cache key
         * @return an {@link org.apache.http.client.cache.HttpCacheEntry} or {@code null} if no
         * entry exists
         * @throws java.io.IOException
         */
        @Override
        public HttpCacheEntry getEntry(String key) throws IOException {
            byte[] data = cache.get(key);
            if(data == null) {
                return null;
            } else {
                return serializer.readFrom(new ByteArrayInputStream(data));
            }
        }

        /**
         * Deletes/invalidates/removes any cache entries currently
         * stored under the given key.
         *
         * @param key
         * @throws java.io.IOException
         */
        @Override
        public void removeEntry(String key) throws IOException {
            cache.remove(key);
        }

        /**
         * Atomically applies the given callback to update an existing cache
         * entry under a given key.
         *
         * @param key      indicates which entry to modify
         * @param callback performs the update; see
         *                 {@link org.apache.http.client.cache.HttpCacheUpdateCallback} for details, but roughly the
         *                 callback expects to be handed the current entry and will return
         *                 the new value for the entry.
         * @throws java.io.IOException
         * @throws org.apache.http.client.cache.HttpCacheUpdateException
         */
        @Override
        public void updateEntry(String key, HttpCacheUpdateCallback callback) throws IOException, HttpCacheUpdateException {
            final byte[] oldData = cache.get(key);

            HttpCacheEntry existingEntry = null;
            if(oldData != null){
                existingEntry = serializer.readFrom(new ByteArrayInputStream(oldData));
            }

            final HttpCacheEntry updatedEntry = callback.update(existingEntry);

            if (existingEntry == null) {
                putEntry(key, updatedEntry);
                return;
            } else {
                // Attempt to do a CAS replace, if we fail then retry
                // While this operation should work fine within this instance, multiple instances
                //  could trample each others' data
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                serializer.writeTo(updatedEntry, bos);
                cache.replace(key, oldData, bos.toByteArray());
            }
        }
    }
}
