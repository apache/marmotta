/*
 * EntryValue.java
 *
 * Created on April 29, 2006, 4:58 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rometools.feed.module.sle.types;

import org.jdom2.Namespace;

import java.io.Serializable;


/**
 * An interface that parents data types for sorting and grouping.
 * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
 */
public interface EntryValue extends Serializable, Cloneable {
    /**
     * Returns the name of the element.
     * @return Returns the name of the element.
     */
    String getElement();

    /**
     * Returns a label for the element.
     * @return Returns a label for the element.
     */
    String getLabel();

    /**
     * Returns the value of the element.
     * @return Returns the value of the element.
     */
    Comparable getValue();
    
    /** 
     * Returns the namespace of the element.
     * @return Returns the namespace of the element.
     */
    Namespace getNamespace();
}
