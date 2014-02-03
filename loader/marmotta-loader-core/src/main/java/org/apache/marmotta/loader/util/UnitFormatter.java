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

package org.apache.marmotta.loader.util;

/**
 * Provides helper methods for formatting values in different units (currently only allows to format sizes by turning
 * them into Giga, Mega, Kilo...)
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class UnitFormatter {


    public static String formatSize(double value) {
        if(value == Double.NaN) {
            return "unknown";
        } else if(value < 1000 * 10) {
            return String.format("%,d", (int)value);
        } else {
            int exp = (int) (Math.log(value) / Math.log(1000));
            if(exp < 1) {
                return String.format("%,d", (int)value);
            } else {
                char pre = "KMGTPE".charAt(exp-1);
                return String.format("%.1f %s", value / Math.pow(1000, exp), pre);
            }
        }
    }
}
