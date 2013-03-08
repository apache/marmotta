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
package org.apache.marmotta.ldpath.model.transformers;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.transformers.NodeTransformer;

import java.util.Map;


/**
 * Transforms (and validates) the lexical value of a node to
 * {@link Duration}.
 * @author Rupert Westenthaler
 * @param <Node> the generic node type
 */
public class DurationTransformer<Node> implements NodeTransformer<Duration,Node> {

    /**
     * Lazy initialisation to avoid Exceptions if {@link DatatypeConfigurationException}
     * is thrown during initialisation of the Utility class.<p>
     * Do not access directly! Use {@link #getXmlDataTypeFactory()} instead.
     */
    private static DatatypeFactory xmlDatatypeFactory;
    /**
     * Inits the {@link #xmlDatatypeFactory} if not already done.<p>
     * @return the XML datatype factory
     * @throws IllegalStateException if a {@link DatatypeConfigurationException}
     * is encountered during {@link DatatypeFactory#newInstance()}
     */
    private static DatatypeFactory getXmlDataTypeFactory() throws IllegalStateException {
        if(xmlDatatypeFactory == null){
            try {
                xmlDatatypeFactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new IllegalStateException("Unable to instantiate XML Datatype Factory!",e);
            }
        }
        return xmlDatatypeFactory;
    }
    @Override
    public Duration transform(RDFBackend<Node> backend, Node node, Map<String, String> configuration) throws IllegalArgumentException {
        if(backend.isLiteral(node)) {
            return toDuration(backend.stringValue(node), false);
        } else {
            throw new IllegalArgumentException("cannot transform node of type "+
                node.getClass().getCanonicalName()+" to byte");
        }
    }
    
    private static Duration toDuration(String value, boolean nullAsZeroDuration) throws IllegalArgumentException,IllegalStateException{
        if(value == null){
            if(nullAsZeroDuration){
                return getXmlDataTypeFactory().newDuration(0);
            } else {
                throw new IllegalArgumentException("The parsed value MUST NOT be NULL. Parse \"boolean nullAsZeroDuration=true\" to enable creation of zero lenght durations for NULL values!");
            }
        } else {
            return getXmlDataTypeFactory().newDuration(value);
        }
    }

}
