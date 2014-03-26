/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.hashing;

import com.google.common.hash.PrimitiveSink;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Wrapper implementing the java.io.DataOutput so that operations write into a Guava primitive sink for hasing.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class PrimitiveSinkOutput implements DataOutput {

    PrimitiveSink sink;

    public PrimitiveSinkOutput(PrimitiveSink sink) {
        this.sink = sink;
    }

    @Override
    public void write(int i) throws IOException {
        sink.putInt(i);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        sink.putBytes(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i2) throws IOException {
        sink.putBytes(bytes,i,i2);
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        sink.putBoolean(b);
    }

    @Override
    public void writeByte(int i) throws IOException {
        sink.putByte((byte)i);
    }

    @Override
    public void writeShort(int i) throws IOException {
        sink.putShort((short)i);
    }

    @Override
    public void writeChar(int i) throws IOException {
        sink.putChar((char)i);
    }

    @Override
    public void writeInt(int i) throws IOException {
        sink.putInt(i);
    }

    @Override
    public void writeLong(long l) throws IOException {
        sink.putLong(l);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        sink.putFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        sink.putDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        sink.putString(s, Charset.defaultCharset());
    }

    @Override
    public void writeChars(String s) throws IOException {
        sink.putString(s, Charset.defaultCharset());
    }

    @Override
    public void writeUTF(String s) throws IOException {
        sink.putString(s, Charset.defaultCharset());
    }
}
