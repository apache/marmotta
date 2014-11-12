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
package org.apache.marmotta.commons.sesame.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.util.DateUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.util.Date;
import java.util.Locale;

/**
 * Test the functions of the literal commons
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LiteralCommonsTest {

    /**
     * Test the creation of the cache keys for literals. All methods should return the same cache key for the
     * same input data.
     *
     * @throws Exception
     */
    @Test
    public void testCreateCacheKey() throws Exception {
        Assert.assertNotEquals(LiteralCommons.createCacheKey("abc",(String)null,(String)null), LiteralCommons.createCacheKey("ABC",(String)null,(String)null));
        Assert.assertNotEquals(LiteralCommons.createCacheKey("abc",null,(URI)null), LiteralCommons.createCacheKey("ABC",null,(URI)null));

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        ValueFactory vf = repository.getValueFactory();

        // create a string literal without language and datatype and test if the hash key is correct between
        // the different methods
        String value1 = RandomStringUtils.random(128);
        Literal literal1 = vf.createLiteral(value1);

        Assert.assertEquals(LiteralCommons.createCacheKey(value1,(String)null,(String)null),LiteralCommons.createCacheKey(literal1));
        Assert.assertEquals(LiteralCommons.createCacheKey(value1,null,(URI)null),LiteralCommons.createCacheKey(literal1));

        // create a string literal with language and without datatype and test if the hash key is correct between
        // the different methods
        String value2 = RandomStringUtils.random(128);
        Locale locale2 = Locale.getDefault();
        Literal literal2 = vf.createLiteral(value2,locale2.getLanguage().toLowerCase());

        Assert.assertEquals(LiteralCommons.createCacheKey(value2,locale2,(String)null),LiteralCommons.createCacheKey(literal2));
        Assert.assertEquals(LiteralCommons.createCacheKey(value2,locale2,(URI)null),LiteralCommons.createCacheKey(literal2));

        // create a string literal with datatype and without language and test if the hash key is correct between
        // the different methods
        String value3 = RandomStringUtils.randomNumeric(3);
        String datatype3 = Namespaces.NS_XSD + "integer";
        Literal literal3 = vf.createLiteral(value3, vf.createURI(datatype3));

        Assert.assertEquals(LiteralCommons.createCacheKey(value3,(String)null,datatype3),LiteralCommons.createCacheKey(literal3));


        // create a Date literal and test whether the hash key is correct between the different methods
        DateTime value4 = DateTime.now();
        String datatype4 = LiteralCommons.getXSDType(value4.getClass());
        Literal literal4 = vf.createLiteral(DateUtils.getXMLCalendar(value4));

        Assert.assertEquals(datatype4,literal4.getDatatype().stringValue());
        Assert.assertEquals(LiteralCommons.createCacheKey(value4,datatype4), LiteralCommons.createCacheKey(literal4));

        repository.shutDown();
    }


    /**
     * Test if the getXSDType() method returns the correct datatype for various Java types
     */
    @Test
    public void testGetXSDType() {

        // String
        Assert.assertEquals(XMLSchema.STRING.stringValue() , LiteralCommons.getXSDType(String.class));

        // int and Integer
        Assert.assertEquals(XMLSchema.INTEGER.stringValue(), LiteralCommons.getXSDType(int.class));
        Assert.assertEquals(XMLSchema.INTEGER.stringValue(), LiteralCommons.getXSDType(Integer.class));


        // long and Long
        Assert.assertEquals(XMLSchema.LONG.stringValue(), LiteralCommons.getXSDType(long.class));
        Assert.assertEquals(XMLSchema.LONG.stringValue(), LiteralCommons.getXSDType(Long.class));

        // double and Double
        Assert.assertEquals(XMLSchema.DOUBLE.stringValue(), LiteralCommons.getXSDType(double.class));
        Assert.assertEquals(XMLSchema.DOUBLE.stringValue(), LiteralCommons.getXSDType(Double.class));

        // float and Float
        Assert.assertEquals(XMLSchema.FLOAT.stringValue(), LiteralCommons.getXSDType(float.class));
        Assert.assertEquals(XMLSchema.FLOAT.stringValue(), LiteralCommons.getXSDType(Float.class));


        // boolean and Boolean
        Assert.assertEquals(XMLSchema.BOOLEAN.stringValue(), LiteralCommons.getXSDType(boolean.class));
        Assert.assertEquals(XMLSchema.BOOLEAN.stringValue(), LiteralCommons.getXSDType(Boolean.class));


        // Date
        Assert.assertEquals(XMLSchema.DATETIME.stringValue(), LiteralCommons.getXSDType(Date.class));

    }
    
    @Test
    public void testGetRDFLangStringType() throws Exception {
    	Assert.assertEquals(RDF.LANGSTRING.stringValue(), LiteralCommons.getRDFLangStringType());
	}
}
