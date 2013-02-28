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

import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of {@link HttpEntity} that reports write/read operations to a {@link Task}.
 * 
 */
@NotThreadSafe
class MonitoredHttpEntity extends HttpEntityWrapper {

    private MonitoredInputStream foo = null;
    private final Task           monitor;
    private final AtomicLong     bytesReceived;

    /**
     * Create a {@link MonitoredHttpEntity} based on the provided {@link HttpEntity} reporting
     * progress to the {@link Task}.
     * 
     * @param delegate the {@link HttpEntity} to wrap.
     * @param monitor the {@link Task} to report the progress to.
     * @param bytesReceived {@link AtomicLong} to add transfered bytes to.
     */
    public MonitoredHttpEntity(HttpEntity delegate, Task monitor, AtomicLong bytesReceived) {
        super(delegate);
        this.monitor = monitor;
        this.bytesReceived = bytesReceived;

        monitorHeader(wrappedEntity.getContentType());
        monitorHeader(wrappedEntity.getContentEncoding());
    }

    /**
     * Create a {@link MonitoredHttpEntity} based on the provided {@link HttpEntity} reporting
     * progress to the {@link Task}.
     * 
     * @param delegate the {@link HttpEntity} to wrap.
     * @param monitor the {@link Task} to report the progress to.
     */
    public MonitoredHttpEntity(HttpEntity delegate, Task monitor) {
        this(delegate, monitor, null);
    }

    @Override
    public InputStream getContent() throws IOException {
        if (foo == null) {
            foo = new MonitoredInputStream(super.getContent());
            if (monitor != null && getContentLength() > 0) {
                monitor.updateTotalSteps(getContentLength());
            }
        }
        return foo;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        if (monitor != null && getContentLength() > 0) {
            monitor.updateTotalSteps(getContentLength());
        }
        super.writeTo(new MonitoredOutputStream(outstream));
        if (monitor != null) {
            monitor.updateMessage("waiting for response");
        }
    }

    private void monitorHeader(final Header ct) {
        if (monitor != null && ct != null) {
            monitor.updateDetailMessage(ct.getName(), ct.getValue());
        }
    }

    private void updateMonitor(long cPos, long delta) {
        if (monitor != null) {
            monitor.updateProgress(cPos);
        }
        if (bytesReceived != null) {
            bytesReceived.addAndGet(delta);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        monitor.endTask();
        super.finalize();
    }

    /**
     * OutputStream that reports the progress on every write operation.
     * 
     */
    protected class MonitoredOutputStream extends FilterOutputStream {

        private long cPos;

        public MonitoredOutputStream(OutputStream out) {
            super(out);
            cPos = 0;
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            updateMonitor(++cPos, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            /* copied from FilterOutputStream.write(byte[], int, int) */
            if ((off | len | b.length - (len + off) | off + len) < 0)
                throw new IndexOutOfBoundsException();

            for (int i = 0; i < len; i++) {
                super.write(b[off + i]);
            }
            updateMonitor(cPos += len, len);
        }

        @Override
        public void close() throws IOException {
            super.close();
        }

    }

    /**
     * InputStream that reports the progress on every read operation.
     */
    protected class MonitoredInputStream extends FilterInputStream {

        private long cPos, markPos;

        protected MonitoredInputStream(InputStream in) {
            super(in);
            cPos = 0;
        }

        @Override
        public int read() throws IOException {
            final int i = super.read();
            if (i >= 0) {
                updateMonitor(++cPos, 1);
            }
            return i;
        }

        @Override
        public int read(byte[] b) throws IOException {
            final int i = super.read(b);
            if (i > 0) {
                updateMonitor(cPos += i, i);
            }
            return i;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            final int i = super.read(b, off, len);
            if (i > 0) {
                updateMonitor(cPos += i, i);
            }
            return i;
        }

        @Override
        public long skip(long n) throws IOException {
            final long i = super.skip(n);
            if (i > 0) {
                updateMonitor(cPos += i, i);
            }
            return i;
        }

        @Override
        public synchronized void mark(int readlimit) {
            super.mark(readlimit);
            markPos = cPos;
        }

        @Override
        public synchronized void reset() throws IOException {
            super.reset();
            updateMonitor(cPos = markPos, 0);
        }

        @Override
        public void close() throws IOException {
            monitor.endTask();
            super.close();
        }
    }
}
