/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.commons.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A static class for generating hash-sums (MD5, SHA-1) out of strings
 * 
 * @author Sebastian Schaffert
 * 
 */
public class HashUtils {

    public static final String md5sum(String string) {
        return calcHash(string, "MD5");
    }

    public static final String md5sum(byte[] bytes) {
        return calcHash(bytes, "MD5");
    }

    public static final String sha1(String string) {
        return calcHash(string, "SHA-1");
    }

    public static final String sha1(byte[] bytes) {
        return calcHash(bytes, "SHA-1");
    }

    private static String calcHash(String string, String algorithm) {
        try {
            return calcHash(string.getBytes("UTF-8"), algorithm);
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    private static String calcHash(byte[] bytes, String algorithm) {
        try {
            MessageDigest m = MessageDigest.getInstance(algorithm);
            m.update(bytes);
            return new BigInteger(1,m.digest()).toString(16);
        } catch(NoSuchAlgorithmException ex) {
            return "";
        }
    }


}
