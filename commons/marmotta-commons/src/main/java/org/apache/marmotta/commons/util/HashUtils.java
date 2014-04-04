/*
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
package org.apache.marmotta.commons.util;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A static class for generating hash-sums (MD5, SHA-1, SHA-512) out of various sources.
 * 
 * @author Sebastian Schaffert
 * 
 */
public class HashUtils {
    
    private HashUtils() {
        // avoid instantiation of util-class.
    }

    public static final String md5sum(String string) {
        return calcHash(string, "MD5");
    }

    public static final String md5sum(File file) throws FileNotFoundException, IOException {
        return calcHash(new FileInputStream(file), "MD5");
    }

    public static final String md5sum(Path file) throws FileNotFoundException, IOException {
        return md5sum(file.toFile());
    }

    public static final String md5sum(InputStream inStream) throws IOException {
        return calcHash(inStream, "MD5");
    }

    public static final String md5sum(byte[] bytes) {
        return calcHash(bytes, "MD5");
    }

    public static final String sha1(String string) {
        return calcHash(string, "SHA-1");
    }

    public static final String sha1(File file) throws FileNotFoundException, IOException {
        return calcHash(new FileInputStream(file), "SHA-1");
    }

    public static final String sha1(Path file) throws FileNotFoundException, IOException {
        return sha1(file.toFile());
    }

    public static final String sha1(InputStream inStream) throws IOException {
        return calcHash(inStream, "SHA-1");
    }

    public static final String sha1(byte[] bytes) {
        return calcHash(bytes, "SHA-1");
    }

    public static final String sha512(String string) {
        return calcHash(string, "SHA-512");
    }

    public static final String sha512(File file) throws FileNotFoundException, IOException {
        return calcHash(new FileInputStream(file), "SHA-512");
    }

    public static final String sha512(Path file) throws FileNotFoundException, IOException {
        return sha512(file.toFile());
    }

    public static final String sha512(InputStream inStream) throws IOException {
        return calcHash(inStream, "SHA-512");
    }

    public static final String sha512(byte[] bytes) {
        return calcHash(bytes, "SHA-512");
    }

    private static String calcHash(String string, String algorithm) {
        try {
            return calcHash(string.getBytes("UTF-8"), algorithm);
        } catch (UnsupportedEncodingException e) {
            return calcHash(string.getBytes(), algorithm);
        }
    }

    private static String calcHash(byte[] bytes, String algorithm) {
        try {
            MessageDigest m = MessageDigest.getInstance(algorithm);
            m.update(bytes);
            return new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            // this should not happen
            return "";
        }
    }

    private static String calcHash(InputStream input, String algorithm) throws IOException {
        try {
            final MessageDigest md = MessageDigest.getInstance(algorithm);
            final DigestInputStream dis = new DigestInputStream(input, md);
            try {
                byte[] buff = new byte[4096];
                // just read to get the Digest filled...
                while (dis.read(buff) > 0) {
                    // nop
                }
                return new BigInteger(1, md.digest()).toString(16);
            } finally {
                dis.close();
            }
        } catch (NoSuchAlgorithmException e) {
            // this should not happen
            return "";
        }
    }
    
}
