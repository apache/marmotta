/*
 * SimpleListExtension.java
 *
 * Created on April 27, 2006, 6:45 PM
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
package org.rometools.feed.module.sle;

import com.sun.syndication.feed.module.Module;
import org.rometools.feed.module.sle.types.Group;
import org.rometools.feed.module.sle.types.Sort;


/** This is the primary module interface for the 
 * <a href="http://msdn.microsoft.com/xml/rss/sle/">MS Simple List Extensions</a>.
 *
 * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
 */
public interface SimpleListExtension extends Module {
    /**
     * "http://www.microsoft.com/schemas/rss/core/2005"
     */
    String URI = "http://www.microsoft.com/schemas/rss/core/2005";

    /**
     * The cf:group element is intended to inform the client that the property to which it refers is one that is “groupable” – that is, that the client should provide a user interface that allows the user to group or filter on the values of that property. Groupable properties should contain a small set of discrete values (e.g. book genres are perfect for groups).
     * @param groupFields Array of types.Group objects.
     */
    void setGroupFields(Group[] groupFields);

    /**
     * The cf:group element is intended to inform the client that the property to which it refers is one that is “groupable” – that is, that the client should provide a user interface that allows the user to group or filter on the values of that property. Groupable properties should contain a small set of discrete values (e.g. book genres are perfect for groups).
     * @return Array of types.Group objects.
     */
    Group[] getGroupFields();

    /**
     * The cf:sort element is intended to inform the client that the property to which it refers is one that is “sortable” – that is, that the client should provide a user interface that allows the user to sort on that property.
     * @param sortFields Array of types.Sort objects
     */
    void setSortFields(Sort[] sortFields);

    /**
     * The cf:sort element is intended to inform the client that the property to which it refers is one that is “sortable” – that is, that the client should provide a user interface that allows the user to sort on that property.
     * @return Array of types.Sort objects
     */
    Sort[] getSortFields();

    /**
     * This XML element allows the publisher of a feed document to indicate to the consumers of the feed that the feed is intended to be consumed as a list.
     * (defaults to "list" )
     * @param value treatAs value
     */
    void setTreatAs(String value);

    /**
     * This XML element allows the publisher of a feed document to indicate to the consumers of the feed that the feed is intended to be consumed as a list.
     * @return treatAs value.
     */
    String getTreatAs();
}
