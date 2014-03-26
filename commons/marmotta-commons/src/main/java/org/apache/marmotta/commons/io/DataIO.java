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

package org.apache.marmotta.commons.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class DataIO {


    public static void writeString(DataOutput out, String s) throws IOException {
        if(s != null) {
            out.writeInt(s.length());
            for(int i=0; i<s.length(); i++) {
                out.writeChar(s.charAt(i));
            }
        } else {
            out.writeInt(-1);
        }
    }


    public static String readString(DataInput in) throws IOException {
        int len = in.readInt();

        if(len >= 0) {
            char[] result = new char[len];

            for(int i=0; i<len; i++) {
                result[i] = in.readChar();
            }
            return new String(result);
        } else {
            return null;
        }
    }


    public static void writeDate(DataOutput out, Date date) throws IOException {
        out.writeLong(date.getTime());
    }


    public static Date readDate(DataInput in) throws IOException {
        long time = in.readLong();
        return new Date(time);
    }
}
