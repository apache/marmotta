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
package org.apache.marmotta.client.model.classification;

import org.apache.marmotta.client.model.rdf.URI;


/**
 * A classification result of the classifier; for each category, provides a probability value indicating how much
 * the text fits to a category.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class Classification implements Comparable<Classification> {


    private URI category;
    private double probability;

    public Classification(URI category, double probability) {
        this.category = category;
        this.probability = probability;
    }

    /**
     * The category for which the classifier computed a probability
     *
     * @return
     */
    public URI getCategory() {
        return category;
    }

    /**
     * The probability the text fits to this category.
     *
     * @return
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p/>
     * Classifiers with higher probability take precedence (are smaller) than classifiers with lower probability.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(Classification o) {
        if(getProbability() > o.getProbability()) {
            return -1;
        } else if(getProbability() < o.getProbability()) {
            return 1;
        } else {
            return 0;
        }
    }
}
