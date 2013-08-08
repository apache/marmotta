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
package org.apache.marmotta.ldpath.model.functions.math;

import java.util.Collection;
import java.util.Random;

import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;



public class MathFunctionTest extends AbstractTestBase {

    private int[] iData;
    private long[] lData;
    private float[] fData;
    private double[] dData;
    private URI subject;
    private URI iProp, lProp, fProp, dProp;

    @Before
    public void createData() throws RepositoryException {
        final Random rnd = new Random();
        final int tSize = 30;

        // We test int, long, float and double
        iData = new int[tSize];
        lData = new long[tSize];
        fData = new float[tSize];
        dData = new double[tSize];

        for (int i = 0; i < tSize; i++) {
            iData[i] = rnd.nextInt(tSize);
            lData[i] = rnd.nextInt();
            fData[i] = rnd.nextInt(1000000) * 10e-4f;
            dData[i] = rnd.nextInt() * 10e-8f;
        }

        subject = repository.getValueFactory().createURI(NSS.get("ex") + rnd.nextInt());
        iProp = repository.getValueFactory().createURI(NSS.get("foo") + "integer");
        lProp = repository.getValueFactory().createURI(NSS.get("foo") + "long");
        fProp = repository.getValueFactory().createURI(NSS.get("foo") + "float");
        dProp = repository.getValueFactory().createURI(NSS.get("foo") + "double");

        final SailRepositoryConnection con = repository.getConnection();
        try {
            final ValueFactory vF = con.getValueFactory();

            for (int i = 0; i < tSize; i++) {
                con.add(vF.createStatement(subject, iProp, vF.createLiteral(iData[i])));
                con.add(vF.createStatement(subject, lProp, vF.createLiteral(lData[i])));
                con.add(vF.createStatement(subject, fProp, vF.createLiteral(fData[i])));
                con.add(vF.createStatement(subject, dProp, vF.createLiteral(dData[i])));
            }

            con.commit();
        } finally {
            con.close();
        }
    }

    @Test
    public void testMin() throws ParseException {
        int iMin = Integer.MAX_VALUE;
        long lMin = Long.MAX_VALUE;
        float fMin = Float.MAX_VALUE;
        double dMin = Double.MAX_VALUE;
        for (int i = 0; i < iData.length; i++) {
            iMin = Math.min(iMin, iData[i]);
            lMin = Math.min(lMin, lData[i]);
            fMin = Math.min(fMin, fData[i]);
            dMin = Math.min(dMin, dData[i]);
        }

        final LdPathParser<Value> intParser = createParserFromString("fn:min(<" + iProp.stringValue() + ">) :: xsd:int");
        final FieldMapping<Object, Value> intRule = intParser.parseRule(NSS);
        final Collection<Object> intResult = intRule.getValues(backend, subject);

        Assert.assertEquals("Integer", 1, intResult.size());
        final Object intNext = intResult.iterator().next();
        Assert.assertTrue("Integer (type)", intNext instanceof Integer);
        Assert.assertEquals("Integer (result)", iMin, intNext);

        final LdPathParser<Value> longParser = createParserFromString("fn:min(<" + lProp.stringValue() + ">) :: xsd:long");
        final FieldMapping<Object, Value> longRule = longParser.parseRule(NSS);
        final Collection<Object> longResult = longRule.getValues(backend, subject);

        Assert.assertEquals("Long", 1, longResult.size());
        final Object longNext = longResult.iterator().next();
        Assert.assertTrue("Long (type)", longNext instanceof Long);
        Assert.assertEquals("Long (result)", lMin, longNext);

        final LdPathParser<Value> floatParser = createParserFromString("fn:min(<" + fProp.stringValue() + ">) :: xsd:float");
        final FieldMapping<Object, Value> floatRule = floatParser.parseRule(NSS);
        final Collection<Object> floatResult = floatRule.getValues(backend, subject);

        Assert.assertEquals("Float", 1, floatResult.size());
        final Object floatNext = floatResult.iterator().next();
        Assert.assertTrue("Float (type)", floatNext instanceof Float);
        Assert.assertEquals("Float (result)", fMin, floatNext);

        final LdPathParser<Value> doubleParser = createParserFromString("fn:min(<" + dProp.stringValue() + ">) :: xsd:double");
        final FieldMapping<Object, Value> doubleRule = doubleParser.parseRule(NSS);
        final Collection<Object> doubleResult = doubleRule.getValues(backend, subject);

        Assert.assertEquals("Double", 1, doubleResult.size());
        final Object doubleNext = doubleResult.iterator().next();
        Assert.assertTrue("Double (type)", doubleNext instanceof Double);
        Assert.assertEquals("Double (result)", dMin, doubleNext);
    }

    @Test
    public void testMax() throws ParseException {
        int iMax = Integer.MIN_VALUE;
        long lMax = Long.MIN_VALUE;
        float fMax = Float.MIN_VALUE;
        double dMax = Double.MIN_VALUE;
        for (int i = 0; i < iData.length; i++) {
            iMax = Math.max(iMax, iData[i]);
            lMax = Math.max(lMax, lData[i]);
            fMax = Math.max(fMax, fData[i]);
            dMax = Math.max(dMax, dData[i]);
        }

        final LdPathParser<Value> intParser = createParserFromString("fn:max(<" + iProp.stringValue() + ">) :: xsd:int");
        final FieldMapping<Object, Value> intRule = intParser.parseRule(NSS);
        final Collection<Object> intResult = intRule.getValues(backend, subject);

        Assert.assertEquals("Integer", 1, intResult.size());
        final Object intNext = intResult.iterator().next();
        Assert.assertTrue("Integer (type)", intNext instanceof Integer);
        Assert.assertEquals("Integer (result)", iMax, intNext);

        final LdPathParser<Value> longParser = createParserFromString("fn:max(<" + lProp.stringValue() + ">) :: xsd:long");
        final FieldMapping<Object, Value> longRule = longParser.parseRule(NSS);
        final Collection<Object> longResult = longRule.getValues(backend, subject);

        Assert.assertEquals("Long", 1, longResult.size());
        final Object longNext = longResult.iterator().next();
        Assert.assertTrue("Long (type)", longNext instanceof Long);
        Assert.assertEquals("Long (result)", lMax, longNext);

        final LdPathParser<Value> floatParser = createParserFromString("fn:max(<" + fProp.stringValue() + ">) :: xsd:float");
        final FieldMapping<Object, Value> floatRule = floatParser.parseRule(NSS);
        final Collection<Object> floatResult = floatRule.getValues(backend, subject);

        Assert.assertEquals("Float", 1, floatResult.size());
        final Object floatNext = floatResult.iterator().next();
        Assert.assertTrue("Float (type)", floatNext instanceof Float);
        Assert.assertEquals("Float (result)", fMax, floatNext);

        final LdPathParser<Value> doubleParser = createParserFromString("fn:max(<" + dProp.stringValue() + ">) :: xsd:double");
        final FieldMapping<Object, Value> doubleRule = doubleParser.parseRule(NSS);
        final Collection<Object> doubleResult = doubleRule.getValues(backend, subject);

        Assert.assertEquals("Double", 1, doubleResult.size());
        final Object doubleNext = doubleResult.iterator().next();
        Assert.assertTrue("Double (type)", doubleNext instanceof Double);
        Assert.assertEquals("Double (result)", dMax, doubleNext);
    }

    @Test
    public void testRound() throws ParseException {
        float fMin = Float.MAX_VALUE;
        double dMin = Double.MAX_VALUE;
        for (int i = 0; i < fData.length; i++) {
            fMin = Math.min(fMin, fData[i]);
            dMin = Math.min(dMin, dData[i]);
        }

        final LdPathParser<Value> floatParser = createParserFromString("fn:round(<" + fProp.stringValue() + ">) :: xsd:int");
        final FieldMapping<Object, Value> floatRule = floatParser.parseRule(NSS);
        final Collection<Object> floatResult = floatRule.getValues(backend, subject);

        Assert.assertEquals("round[Float]", fData.length, floatResult.size());
        for (float element : fData) {
            Assert.assertThat("round[Float] (result)", floatResult, CoreMatchers.hasItem(Math.round(element)));
        }

        final LdPathParser<Value> doubleParser = createParserFromString("fn:round(<" + dProp.stringValue() + ">) :: xsd:long");
        final FieldMapping<Object, Value> doubleRule = doubleParser.parseRule(NSS);
        final Collection<Object> doubleResult = doubleRule.getValues(backend, subject);

        Assert.assertEquals("round[Double]", dData.length, doubleResult.size());
        for (double element : dData) {
            Assert.assertThat("round[Double] (result)", doubleResult, CoreMatchers.hasItem(Math.round(element)));
        }

    }

    @Test
    @Ignore("fn:sum has serious design issues")
    public void testSum() throws ParseException {
        int iSum = 0;
        long lSum = 0l;
        float fSum = 0f;
        double dSum = 0d;
        for (int i = 0; i < iData.length; i++) {
            iSum += iData[i];
            lSum += lData[i];
            fSum += fData[i];
            dSum += dData[i];
        }

        final LdPathParser<Value> intParser = createParserFromString("fn:sum(<" + iProp.stringValue() + ">) :: xsd:int");
        final FieldMapping<Object, Value> intRule = intParser.parseRule(NSS);
        final Collection<Object> intResult = intRule.getValues(backend, subject);

        Assert.assertEquals("Integer", 1, intResult.size());
        final Object intNext = intResult.iterator().next();
        Assert.assertTrue("Integer (type)", intNext instanceof Integer);
        Assert.assertEquals("Integer (result)", iSum, intNext);

        final LdPathParser<Value> longParser = createParserFromString("fn:sum(<" + lProp.stringValue() + ">) :: xsd:long");
        final FieldMapping<Object, Value> longRule = longParser.parseRule(NSS);
        final Collection<Object> longResult = longRule.getValues(backend, subject);

        Assert.assertEquals("Long", 1, longResult.size());
        final Object longNext = longResult.iterator().next();
        Assert.assertTrue("Long (type)", longNext instanceof Long);
        Assert.assertEquals("Long (result)", lSum, longNext);

        final LdPathParser<Value> floatParser = createParserFromString("fn:sum(<" + fProp.stringValue() + ">) :: xsd:float");
        final FieldMapping<Object, Value> floatRule = floatParser.parseRule(NSS);
        final Collection<Object> floatResult = floatRule.getValues(backend, subject);

        Assert.assertEquals("Float", 1, floatResult.size());
        final Object floatNext = floatResult.iterator().next();
        Assert.assertTrue("Float (type)", floatNext instanceof Float);
        Assert.assertEquals("Float (result)", fSum, floatNext);

        final LdPathParser<Value> doubleParser = createParserFromString("fn:sum(<" + dProp.stringValue() + ">) :: xsd:double");
        final FieldMapping<Object, Value> doubleRule = doubleParser.parseRule(NSS);
        final Collection<Object> doubleResult = doubleRule.getValues(backend, subject);

        Assert.assertEquals("Double", 1, doubleResult.size());
        final Object doubleNext = doubleResult.iterator().next();
        Assert.assertTrue("Double (type)", doubleNext instanceof Double);
        Assert.assertEquals("Double (result)", dSum, doubleNext);

    }

}
