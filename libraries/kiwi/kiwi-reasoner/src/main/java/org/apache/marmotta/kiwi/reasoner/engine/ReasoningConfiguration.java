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
package org.apache.marmotta.kiwi.reasoner.engine;

/**
 * Hold the configuration of the reasoning engine.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class ReasoningConfiguration {

    /**
     * Let the reasoner commit a sail transaction (for added triples) after so many triple additions.
     */
    private int batchSize = 1000;

    /**
     * Eliminate duplicate justifications; can require additional time but reduces database overhead
     */
    private boolean removeDuplicateJustifications = true;

    /**
     * Number of parallel workers for processing reasoning rules.
     */
    private int workers = 4;

    public ReasoningConfiguration() {
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isRemoveDuplicateJustifications() {
        return removeDuplicateJustifications;
    }

    public void setRemoveDuplicateJustifications(boolean removeDuplicateJustifications) {
        this.removeDuplicateJustifications = removeDuplicateJustifications;
    }

}
