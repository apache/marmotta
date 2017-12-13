/*
 * CourseInformation.java
 *
 * Created on November 16, 2005, 11:12 AM
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

import org.rometools.feed.module.base.types.DateTimeRange;


/**
 * This is an interface for the GoogleBase plug in that exposes methods used for
 * Class or Course information entry types.
 * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
 * @version $Revision: 1.2 $
 */
public interface Course extends GlobalInterface {
    /**
     * The timeframe a course is running.
     *
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     *
     * <tbody><tr valign="top">
     *
     *  <td colspan="2" bgcolor="#dddddd"><font size="-1">
     *  <b><a name="course_date_range"></a>course_date_range</b></font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *  <td><font size="-1">Date and time range a class is in session, in <a href="http://www.iso.org/iso/en/prods-services/popstds/datesandtime.html">ISO 8601</a>.  Two  sub-attributes
     *  are included in course_date_range attribute.<ul type="disc">
     *
     *  <li>start = Start date and time of a trip in
     *  format YYYY-MM-DDThh:mm:ss</li>
     *  <li>end = End date and time of a trip in
     *  format YYYY-MM-DDThh:mm:ss</li></ul></font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:course_date_range&gt; <br>
     *
     * &lt;g:start&gt;2005-12-20T09:30:01&lt;/g:start&gt;
     *  <br>
     * &lt;g:end&gt;2005-12-29T10:30:59&lt;/g:end&gt;<br>
     * &lt;/g:course_date_range&gt;</font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *  Course schedules</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Content type</b></font></td>
     *
     *  <td><font size="-1">
     * dateTimeRange</font></td>
     * </tr>
     * </tbody></table>
     * @param courseDateRange The timeframe a course is running
     */
    void setCourseDateRange(DateTimeRange courseDateRange);

    /**
     * The timeframe a course is running.
     *
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     *
     * <tbody><tr valign="top">
     *
     *  <td colspan="2" bgcolor="#dddddd"><font size="-1">
     *  <b><a name="course_date_range"></a>course_date_range</b></font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *  <td><font size="-1">Date and time range a class is in session, in <a href="http://www.iso.org/iso/en/prods-services/popstds/datesandtime.html">ISO 8601</a>.  Two  sub-attributes
     *  are included in course_date_range attribute.<ul type="disc">
     *
     *  <li>start = Start date and time of a trip in
     *  format YYYY-MM-DDThh:mm:ss</li>
     *  <li>end = End date and time of a trip in
     *  format YYYY-MM-DDThh:mm:ss</li></ul></font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:course_date_range&gt; <br>
     *
     * &lt;g:start&gt;2005-12-20T09:30:01&lt;/g:start&gt;
     *  <br>
     * &lt;g:end&gt;2005-12-29T10:30:59&lt;/g:end&gt;<br>
     * &lt;/g:course_date_range&gt;</font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *  Course schedules</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Content type</b></font></td>
     *
     *  <td><font size="-1">
     * dateTimeRange</font></td>
     * </tr>
     * </tbody></table>
     * @return The timeframe a course is running
     */
    DateTimeRange getCourseDateRange();

    /**
     * ID code associated with a course.
     *
     *
     *        <table border="1" cellpadding="5" cellspacing="0" width="640">
     *          <tbody><tr valign="top">
     *            <td colspan="2" bgcolor="#dddddd" valign="top"> <font size="-1"><b><a name="course_number"></a>course_number</b></font></td>
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Details</b></font></td>
     *
     *            <td> <font size="-1">ID code associated with a course</font></td>
     *
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Example</b></font></td>
     *            <td> <font size="-1">&lt;g:course_number&gt;HIST-90A&lt;/g:course_number&gt;</font></td>
     *
     *          </tr>
     *          <tr valign="top">
     *
     *            <td width="120"> <font size="-1"><b>Attribute
     *                of</b></font></td>
     *            <td> <font size="-1">Course schedules</font></td>
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Content
     *                type</b></font></td>
     *
     *            <td> <font size="-1">string</font></td>
     *
     *          </tr>
     *        </tbody></table>
     * @param courseNumber ID code associated with a course
     */
    void setCourseNumber(String courseNumber);

    /**
     * ID code associated with a course.
     *
     *
     *
     *        <table border="1" cellpadding="5" cellspacing="0" width="640">
     *          <tbody><tr valign="top">
     *            <td colspan="2" bgcolor="#dddddd" valign="top"> <font size="-1"><b><a name="course_number"></a>course_number</b></font></td>
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Details</b></font></td>
     *
     *            <td> <font size="-1">ID code associated with a course</font></td>
     *
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Example</b></font></td>
     *            <td> <font size="-1">&lt;g:course_number&gt;HIST-90A&lt;/g:course_number&gt;</font></td>
     *
     *          </tr>
     *          <tr valign="top">
     *
     *            <td width="120"> <font size="-1"><b>Attribute
     *                of</b></font></td>
     *            <td> <font size="-1">Course schedules</font></td>
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Content
     *                type</b></font></td>
     *
     *            <td> <font size="-1">string</font></td>
     *
     *          </tr>
     *        </tbody></table>
     * @return ID code associated with a course
     */
    String getCourseNumber();

    /**
     * Time a class is in session.
     *
     *
     *  <table border="1" cellpadding="5" cellspacing="0" width="640">
     *          <tbody><tr valign="top">
     *            <td colspan="2" bgcolor="#dddddd" valign="top"> <font size="-1"><b><a name="course_times"></a>course_times</b></font></td>
     *
     *          </tr>
     *
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Details</b></font></td>
     *            <td> <font size="-1">Time a class is in session.</font></td>
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Example</b></font></td>
     *
     *            <td> <font size="-1">&lt;g:course_times&gt;MWF 08:30 - 09:45&lt;/g:course_times&gt;</font></td>
     *
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Attribute
     *                of</b></font></td>
     *            <td> <font size="-1">Course schedules</font></td>
     *
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Content
     *                type</b></font></td>
     *
     *            <td> <font size="-1">string</font></td>
     *          </tr>
     *        </tbody></table>
     * @param courseTimes Time a class is in session
     */
    void setCourseTimes(String courseTimes);

    /**
     * Time a class is in session.
     *
     *
     *
     *  <table border="1" cellpadding="5" cellspacing="0" width="640">
     *          <tbody><tr valign="top">
     *            <td colspan="2" bgcolor="#dddddd" valign="top"> <font size="-1"><b><a name="course_times"></a>course_times</b></font></td>
     *
     *          </tr>
     *
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Details</b></font></td>
     *            <td> <font size="-1">Time a class is in session.</font></td>
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Example</b></font></td>
     *
     *            <td> <font size="-1">&lt;g:course_times&gt;MWF 08:30 - 09:45&lt;/g:course_times&gt;</font></td>
     *
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Attribute
     *                of</b></font></td>
     *            <td> <font size="-1">Course schedules</font></td>
     *
     *          </tr>
     *          <tr valign="top">
     *            <td width="120"> <font size="-1"><b>Content
     *                type</b></font></td>
     *
     *            <td> <font size="-1">string</font></td>
     *          </tr>
     *        </tbody></table>
     * @return Time a class is in session
     */
    String getCourseTimes();

    /**
     * Salary for this position.
     *
     *
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="salary"></a>salary</b></font></td>
     * </tr>
     *
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *  Salary for this position. Non-numeric values such as "$" symbols are not acceptable. </font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Example</b></font></td>
     *  <td><font size="-1">
     * &lt;g:salary&gt;55000&lt;/g:salary&gt;</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Attribute of</b></font></td>
     *
     *  <td><font size="-1">
     *  Jobs</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Content type</b></font></td>
     *
     *  <td><font size="-1">
     *
     * float</font></td>
     * </tr>
     * </tbody></table>
     * @param salary Salary for this position
     */
    void setSalary(Float salary);

    /**
     * Salary for this position.
     *
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="salary"></a>salary</b></font></td>
     * </tr>
     *
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *  Salary for this position. Non-numeric values such as "$" symbols are not acceptable. </font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Example</b></font></td>
     *  <td><font size="-1">
     * &lt;g:salary&gt;55000&lt;/g:salary&gt;</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *
     *  <b>Attribute of</b></font></td>
     *
     *  <td><font size="-1">
     *  Jobs</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Content type</b></font></td>
     *
     *  <td><font size="-1">
     *
     * float</font></td>
     * </tr>
     * </tbody></table>
     * @return Salary for this position
     */
    Float getSalary();

    /**
     * Topics of study for a course.
     *
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="subject"></a>subject</b></font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *  Topic of study for a course.</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *
     *  <td><font size="-1"> &lt;g:subject&gt;Trigonometry&lt;/g:subject&gt;</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *
     *  Course schedules</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *  string</font></td>
     *
     * </tr>
     * </tbody></table>
     * @param subject Topics of study for a course
     */
    void setSubjects(String[] subject);

    /**
     * Topics of study for a course.
     *
     *
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="subject"></a>subject</b></font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *  Topic of study for a course.</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *
     *  <td><font size="-1"> &lt;g:subject&gt;Trigonometry&lt;/g:subject&gt;</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     *
     *  Course schedules</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Content type</b></font></td>
     *  <td><font size="-1">
     *  string</font></td>
     *
     * </tr>
     * </tbody></table>
     * @return Topics of study for a course
     */
    String[] getSubjects();

    /**
     * Name of the school at which a class is offered.
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="university"></a>university</b></font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *  Name of the school a class is offered at. </font></td>
     * </tr>
     *
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:university&gt;Stanford&lt;/g:university&gt;</font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     * Course schedules</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Content type</b></font></td>
     *
     *  <td><font size="-1">
     *
     *  string</font></td>
     * </tr>
     * </tbody></table>
     * @param university Name of the school at which a class is offered.
     */
    void setUniversity(String university);

    /**
     * Name of the school at which a class is offered.
     * <table border="1" cellpadding="5" cellspacing="0" width="640">
     * <tbody><tr valign="top">
     *  <td colspan="2" bgcolor="#dddddd" valign="top"><font size="-1">
     *  <b><a name="university"></a>university</b></font></td>
     *
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Details</b></font></td>
     *  <td><font size="-1">
     *  Name of the school a class is offered at. </font></td>
     * </tr>
     *
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *  <b>Example</b></font></td>
     *  <td><font size="-1"> &lt;g:university&gt;Stanford&lt;/g:university&gt;</font></td>
     * </tr>
     * <tr valign="top">
     *
     *  <td width="120"><font size="-1">
     *
     *  <b>Attribute of</b></font></td>
     *  <td><font size="-1">
     * Course schedules</font></td>
     * </tr>
     * <tr valign="top">
     *  <td width="120"><font size="-1">
     *  <b>Content type</b></font></td>
     *
     *  <td><font size="-1">
     *
     *  string</font></td>
     * </tr>
     * </tbody></table>
     * @return Name of the school at which a class is offered.
     */
    String getUniversity();
}
