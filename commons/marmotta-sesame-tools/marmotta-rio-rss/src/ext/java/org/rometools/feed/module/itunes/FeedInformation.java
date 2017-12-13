/*
 * FeedInformation.java
 *
 * Created on November 19, 2005, 10:57 PM
 *
 * This library is provided under dual licenses.
 * You may choose the terms of the Lesser General Public License or the Apache
 * License at your discretion.
 *
 *  Copyright (C) 2005  Robert Cooper, Temple of the Screaming Penguin
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
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
 *
 */
package org.rometools.feed.module.itunes;

import java.net.URL;
import java.util.List;


/**
 * This class contains information for iTunes podcast feeds that exist at the Channel level.
 * 
 * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
 * @version $Revision: 1.2 $
 */
public interface FeedInformation extends ITunes {

    /**
     * The parent categories for this feed
     * @return The parent categories for this feed
     */
    List getCategories() ;

    /**
     * The parent categories for this feed
     * @param categories The parent categories for this feed
     */
    void setCategories(List categories);
    
    /**
     * Sets the URL for the image.
     *
     * NOTE: To specification images should be in PNG or JPEG format.
     * @param image Sets the URL for the image.
     */
    void setImage(URL image);

    /**
     * Returns the URL for the image.
     *
     * NOTE: To specification images should be in PNG or JPEG format.
     * @return Returns the URL for the image.
     */
    URL getImage();

    /**
     * Sets the owner email address for the feed.
     * @param ownerEmailAddress Sets the owner email address for the feed.
     */
    void setOwnerEmailAddress(String ownerEmailAddress);

    /**
     * Returns the owner email address for the feed.
     * @return Returns the owner email address for the feed.
     */
    String getOwnerEmailAddress();

    /**
     * Sets the owner name for the feed
     * @param ownerName Sets the owner name for the feed
     */
    void setOwnerName(String ownerName);

    /**
     * Returns the owner name for the feed
     * @return  Returns the owner name for the feed
     */
    String getOwnerName();
}
