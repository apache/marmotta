/*
 * Article.java
 *
 * Created on November 16, 2005, 1:01 PM
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
 */
package org.rometools.feed.module.base;

import java.util.Date;


/**
 * This is an interface for the GoogleBase plug in that exposes methods used for
 * Article or News entry types.
 * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
 * @version $Revision: 1.2 $
 */
public interface Article extends GlobalInterface {
    /**
     * Array of Author Names. Limit 10.
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     *
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="author"></a>author</b></font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *
     *  <td><font size="-1">
     *  Author of the item.</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:author&gt;John Steinbeck&lt;/g:author&gt;</font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *  Reference Items, News and Articles</font></td>
     * </tr>
     *
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *  string</font></td>
     * </tr>
     * </tbody></table>
     * @param authors Array of Author Names. Limit 10.
     */
    void setAuthors(String[] authors);

    /**
     * Array of Author Names. Limit 10.
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     *
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="author"></a>author</b></font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *
     *  <td><font size="-1">
     *  Author of the item.</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:author&gt;John Steinbeck&lt;/g:author&gt;</font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *  Reference Items, News and Articles</font></td>
     * </tr>
     *
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *  string</font></td>
     * </tr>
     * </tbody></table>
     * @return Array of author names.
     */
    String[] getAuthors();

    /**
     * Source for this article.
     *
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *
     *  <td colspan="2" bgcolor="#dddddd"><font size="-1">
     *  <b><a name="news_source"></a>news_source</b></font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *
     *  <td><font size="-1">
     *  The source of news content.</font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:news_source&gt;Journal&lt;/g:news_source&gt;</font></td>
     *
     * </tr>
     *
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *  News and Articles</font></td>
     * </tr>
     *
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *  string</font></td>
     * </tr>
     * </tbody></table>
     * @param newsSource Source for this article
     */
    void setNewsSource(String newsSource);

    /**
     * Source for this article.
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *
     *  <td colspan="2" bgcolor="#dddddd"><font size="-1">
     *  <b><a name="news_source"></a>news_source</b></font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *
     *  <td><font size="-1">
     *  The source of news content.</font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:news_source&gt;Journal&lt;/g:news_source&gt;</font></td>
     *
     * </tr>
     *
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *  News and Articles</font></td>
     * </tr>
     *
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *  string</font></td>
     * </tr>
     * </tbody></table>
     * @return source for this article.
     */
    String getNewsSource();

    /**
     * Number of pages in the article.
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     *
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="pages"></a>pages</b></font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *
     *  The number of pages in the publication.</font></td>
     * </tr>
     *
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:pages&gt;18&lt;/g:pages&gt;</font></td>
     *
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *  Reference Items, News and Articles</font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *  integer</font></td>
     * </tr>
     * </tbody></table>
     * @param pages Number of pages in the article
     */
    void setPages(Integer pages);

    /**
     * Number of pages in the article.
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     *
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="pages"></a>pages</b></font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *
     *  The number of pages in the publication.</font></td>
     * </tr>
     *
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:pages&gt;18&lt;/g:pages&gt;</font></td>
     *
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *  Reference Items, News and Articles</font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *  integer</font></td>
     * </tr>
     * </tbody></table>
     * @return Number of pages in the article
     */
    Integer getPages();

    /**
     * Date article was published.
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd"><font size="-1">
     *  <b><a name="publish_date"></a>publish_date</b></font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *  Date the item was published in <a href="http://www.iso.org/iso/en/prods-services/popstds/datesandtime.html">ISO 8601</a> format:
     *              YYYY-MM-DD</font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:publish_date&gt;2005-12-20&lt;/g:publish_date&gt;</font></td>
     * </tr>
     *
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *
     *  <td><font size="-1">
     *  Reference Items</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *
     *  date</font></td>
     * </tr>
     * </tbody></table>
     * @param publishDate Date article was published
     */
    void setPublishDate(Date publishDate);

    /**
     *  Date article was published.
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd"><font size="-1">
     *  <b><a name="publish_date"></a>publish_date</b></font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *  Date the item was published in <a href="http://www.iso.org/iso/en/prods-services/popstds/datesandtime.html">ISO 8601</a> format:
     *              YYYY-MM-DD</font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:publish_date&gt;2005-12-20&lt;/g:publish_date&gt;</font></td>
     * </tr>
     *
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *
     *  <td><font size="-1">
     *  Reference Items</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *
     *  date</font></td>
     * </tr>
     * </tbody></table>
     * @return Date article was published
     */
    Date getPublishDate();
}
