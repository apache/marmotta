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

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.apache.marmotta.kiwi.io.KiWiIO;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A Guava Funnel implementation based on the serialization of the triple.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class TripleFunnel implements Funnel<KiWiTriple> {
    private static Logger log = LoggerFactory.getLogger(TripleFunnel.class);

    private static TripleFunnel instance;

    private TripleFunnel() {
    }


    public synchronized static TripleFunnel getInstance() {
        if(instance == null) {
            instance = new TripleFunnel();
        }
        return instance;
    }


    @Override
    public void funnel(KiWiTriple kiWiTriple, PrimitiveSink primitiveSink) {
        try {
            KiWiIO.writeTriple(new PrimitiveSinkOutput(primitiveSink), kiWiTriple);
        } catch (IOException e) {
            log.error("I/O error while writing data to sink (cannot happen)");
        }

    }
}
