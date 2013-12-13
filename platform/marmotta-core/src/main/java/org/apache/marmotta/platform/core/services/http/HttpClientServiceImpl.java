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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import net.sf.ehcache.Ehcache;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.RequestLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.ehcache.EhcacheHttpCacheStorage;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.http.HttpClientService;
import org.apache.marmotta.platform.core.api.statistics.StatisticsModule;
import org.apache.marmotta.platform.core.api.statistics.StatisticsService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.qualifiers.cache.MarmottaCache;
import org.apache.marmotta.platform.core.services.http.response.LastModifiedResponseHandler;
import org.apache.marmotta.platform.core.services.http.response.StatusCodeResponseHandler;
import org.apache.marmotta.platform.core.services.http.response.StringBodyResponseHandler;
import org.slf4j.Logger;

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
    private Instance<Ehcache>            ehcache;

    private CloseableHttpClient                   httpClient;
    private IdleConnectionMonitorThread  idleConnectionMonitorThread;
//    private BasicHttpParams              httpParams;

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
     * Get a ready-to-use {@link CloseableHttpClient}.
     */
    @Override
    public CloseableHttpClient getHttpClient() {
        return new ReadLockHttpClient(httpClient);
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

            final HttpClientBuilder clientBuilder;
            if (configurationService.getBooleanConfiguration("core.http.client_cache_enable", true)) {
                CacheConfig cacheConfig = CacheConfig.custom()
                        // FIXME: Hardcoded constants - is this useful?
                        .setMaxCacheEntries(1000)
                        .setMaxObjectSize(81920)
                        .build();

                final HttpCacheStorage cacheStore = new EhcacheHttpCacheStorage(ehcache.get(), cacheConfig);
                clientBuilder = CachingHttpClientBuilder.create()
                    .setCacheConfig(cacheConfig)
                    .setHttpCacheStorage(cacheStore);
            } else {
                clientBuilder = HttpClients.custom();
            }
            String userAgentString =
                    "Apache Marmotta/" + configurationService.getStringConfiguration("kiwi.version") +
                    " (running at " + configurationService.getServerUri() + ")" +
                    " lmf-core/" + configurationService.getStringConfiguration("kiwi.version");
            clientBuilder.setUserAgent(configurationService.getStringConfiguration("core.http.user_agent", userAgentString));

            clientBuilder.setDefaultRequestConfig(RequestConfig.custom()
                    .setSocketTimeout(configurationService.getIntConfiguration("core.http.so_timeout", 60000))
                    .setConnectTimeout(configurationService.getIntConfiguration("core.http.connection_timeout", 10000))
                    .setRedirectsEnabled(true)
                    .setMaxRedirects(3)
                    .build());

            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(configurationService.getIntConfiguration("core.http.max_connections", 20));
            cm.setDefaultMaxPerRoute(configurationService.getIntConfiguration("core.http.max_connections_per_route", 10));
            clientBuilder.setConnectionManager(cm);
            
            clientBuilder.setRedirectStrategy(new LMFRedirectStrategy());
            clientBuilder.setRetryHandler(new LMFHttpRequestRetryHandler());

            this.httpClient = new MonitoredHttpClient(clientBuilder.build());
            bytesSent.set(0);
            bytesReceived.set(0);
            requestsExecuted.set(0);

            idleConnectionMonitorThread = new IdleConnectionMonitorThread(cm);
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
            try {
                httpClient.close();
            } catch (IOException e) {
                log.warn("Exception while closing HttpClient: {}", e.getMessage());
            }
        } finally {
            lock.writeLock().unlock();
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

        private final HttpClientConnectionManager connMgr;
        private volatile boolean              shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
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
        private PoolingHttpClientConnectionManager connectionManager;

        public StatisticsProvider(PoolingHttpClientConnectionManager cm) {
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

    private class ReadLockHttpClient extends CloseableHttpClient {

        private final CloseableHttpClient delegate;

        public ReadLockHttpClient(CloseableHttpClient delegate) {
            this.delegate = delegate;
        }

        @Override
        @Deprecated
        public HttpParams getParams() {
            return delegate.getParams();
        }

        @Override
        @Deprecated
        public ClientConnectionManager getConnectionManager() {
            return delegate.getConnectionManager();
        }

        @Override
        public void close() throws IOException {
            // Nop; this Client is read only!
        }

        @Override
        protected CloseableHttpResponse doExecute(HttpHost target,
                HttpRequest request, HttpContext context) throws IOException,
                ClientProtocolException {
            lock.readLock().lock();
            try {
                return delegate.execute(target, request, context);
            } finally {
                lock.readLock().lock();
            }
        }


    }

    protected class MonitoredHttpClient extends CloseableHttpClient {

        private final CloseableHttpClient delegate;

        public MonitoredHttpClient(CloseableHttpClient delegate) {
            this.delegate = delegate;
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

        private CloseableHttpResponse postProcess(final CloseableHttpResponse response, HttpContext context, final Task task) {
            requestsExecuted.incrementAndGet();
            task.resetProgress();
            task.updateMessage("retrieving response");
            if (response.getEntity() != null) {
                boolean cachedResponse;
                if (context != null && context instanceof HttpCacheContext) {
                    HttpCacheContext cacheContext = (HttpCacheContext) context;
                    CacheResponseStatus cacheRespStatus = cacheContext.getCacheResponseStatus();
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
        @Deprecated
        public HttpParams getParams() {
            return delegate.getParams();
        }

        @Override
        @Deprecated
        public ClientConnectionManager getConnectionManager() {
            return delegate.getConnectionManager();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
            final Task task = preProcess(request);

            lock.readLock().lock();
            try {
                return postProcess(delegate.execute(target, request, context), context, task);
            } catch (ClientProtocolException cpe) {
                task.endTask();
                throw cpe;
            } catch (IOException io) {
                task.endTask();
                throw io;
            } finally {
                lock.readLock().unlock();
            }
        }

    }
}
