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
package org.apache.marmotta.ldpath.model.functions.text;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;


@RunWith(Parameterized.class)
public class TextFunctionsTest extends AbstractTestBase {

    private static final int LOOP_STEP = 5;

    @Parameters(name = "Case-{index}")
    public static List<String[]> data() {
        return Arrays.asList(
                new String[] { "LD Path is a simple path-based query language similar to XPath or SPARQL Property Paths that is particularly well-suited for querying and retrieving resources from the Linked Data Cloud by following RDF links between resources and servers." },
                new String[] { "The LDPath project is a collection of generic libraries that are independent of the underlying RDF implementation."},
                new String[] { "Currently, there are backends for sesame, for RDF files, and for Linked Data. You can easily implement your own backends by implementing a straightforward interface (RDFBackend)." }
                );
    }

    @Parameter
    public String text;

    private URI subject, predicate;
    final private Random rnd = new Random();

    @Before
    public void setUp() throws RepositoryException {

        subject = repository.getValueFactory().createURI(ns("foo", UUID.randomUUID().toString()));
        predicate = repository.getValueFactory().createURI(ns("foo", UUID.randomUUID().toString()));

        final SailRepositoryConnection con = repository.getConnection();
        try {
            final ValueFactory vf = con.getValueFactory();

            con.add(vf.createStatement(subject, predicate, vf.createLiteral(text)));

            con.commit();
        } finally {
            con.close();
        }
    }

    @Test
    public void testReplace() throws ParseException {
        final String regex = "RDF", replace = "Linked Data";
        final String ldPath = String.format("fn:replace(<%s>, \"%s\", \"%s\") :: xsd:string", predicate.stringValue(), regex, replace);
        final String expected = text.replaceAll(regex, replace);

        final Collection<Object> values = evaluateRule(ldPath, subject);

        Assert.assertEquals(1, values.size());
        Assert.assertEquals(expected, values.iterator().next().toString());
    }

    @Test
    public void testStrLen() throws ParseException {
        final String ldPath = String.format("fn:strlen(<%s>) :: xsd:int", predicate.stringValue());
        final Collection<Object> values = evaluateRule(ldPath, subject);

        Assert.assertEquals(1, values.size());
        Assert.assertEquals(text.length(), values.iterator().next());
    }

    @Test
    public void testWordCount() throws ParseException {
        final String ldPath = String.format("fn:wc(<%s>) :: xsd:int", predicate.stringValue());
        final Collection<Object> values = evaluateRule(ldPath, subject);

        boolean isWordChar = false;
        int wc = 0;
        for (int i = 0; i < text.length(); i++) {
            final boolean isWC = Character.isLetterOrDigit(text.codePointAt(i));
            if (!isWordChar && isWC) {
                wc++;
            }
            isWordChar = isWC;
        }

        Assert.assertEquals(1, values.size());
        Assert.assertEquals(wc, values.iterator().next());
    }

    @Test
    public void testStrLeft() throws ParseException {
        for (int len = -1; len <= text.length() + 2 * LOOP_STEP; len += rnd.nextInt(2 * LOOP_STEP) + 1) {
            final String ldPath = String.format("fn:strLeft(<%s>, \"%d\") :: xsd:string", predicate.stringValue(), len);
            final Collection<Object> values = evaluateRule(ldPath, subject);

            Assert.assertEquals("strLeft(<>, " + len + ")", 1, values.size());
            final Object val = values.iterator().next();

            int expSize = Math.min(Math.max(0, len), text.length());

            Assert.assertEquals("strLeft(<>, " + len + ")", expSize, val.toString().length());
            Assert.assertEquals("strLeft(<>, " + len + ")", text.substring(0, expSize), val);
        }
    }

    @Test
    public void testStrRight() throws ParseException {
        for (int len = -1; len <= text.length() + 2 * LOOP_STEP; len += rnd.nextInt(2 * LOOP_STEP) + 1) {
            final String ldPath = String.format("fn:strRight(<%s>, \"%d\") :: xsd:string", predicate.stringValue(), len);
            final Collection<Object> values = evaluateRule(ldPath, subject);

            Assert.assertEquals("strRight(<>, " + len + ")", 1, values.size());
            final Object val = values.iterator().next();

            int expSize = Math.min(Math.max(0, len), text.length());

            Assert.assertEquals("strRight(<>, " + len + ")", expSize, val.toString().length());
            Assert.assertEquals("strRight(<>, " + len + ")", text.substring(text.length() - expSize), val);
        }
    }

    @Test
    public void testSubstr() throws ParseException {
        for (int start = -1; start <= text.length() + 2 * LOOP_STEP; start += rnd.nextInt(2 * LOOP_STEP) + 1) {
            // 2-Arg usage
            final String ldPath_2Arg = String.format("fn:substr(<%s>, \"%d\") :: xsd:string", predicate.stringValue(), start);
            final Collection<Object> values_2Arg = evaluateRule(ldPath_2Arg, subject);

            Assert.assertEquals("substr(<>, " + start + ")", 1, values_2Arg.size());
            final Object val_2Arg = values_2Arg.iterator().next();

            int expStart = Math.min(Math.max(0, start), text.length());

            Assert.assertEquals("substr(<>, " + start + ")", text.length() - expStart, val_2Arg.toString().length());
            Assert.assertEquals("substr(<>, " + start + ")", text.substring(expStart), val_2Arg);

            // 3-Arg usage
            for (int end = start; end <= text.length() + 2 * LOOP_STEP; end += rnd.nextInt(2 * LOOP_STEP) + 1) {
                final String ldPath_3Arg = String.format("fn:substr(<%s>, \"%d\", \"%d\") :: xsd:string", predicate.stringValue(), start, end);
                final Collection<Object> values_3Arg = evaluateRule(ldPath_3Arg, subject);

                Assert.assertEquals("substr(<>, " + start + ", " + end + ")", 1, values_3Arg.size());
                final Object val_3Arg = values_3Arg.iterator().next();

                int expEnd = Math.min(Math.max(0, end), text.length());

                Assert.assertEquals("substr(<>, " + start + ", " + end + ")", expEnd - expStart, val_3Arg.toString().length());
                Assert.assertEquals("substr(<>, " + start + ", " + end + ")", text.substring(expStart, expEnd), val_3Arg);
            }
        }
    }
    
    @Test
    public void testStrJoin() throws ParseException, RepositoryException {
        /* unique test, sets up it's own data */
        String[] lits = new String[] {UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()};
        
        SailRepositoryConnection con = repository.getConnection();
        con.begin();
        con.remove(subject, predicate, null);
        for (int i = 0; i < lits.length; i++) {
            con.add(con.getValueFactory().createStatement(subject, predicate, con.getValueFactory().createLiteral(lits[i])));
        }
        con.commit();
        con.close();
        
        String[] seps = {"###", ", "};
        String[] pres = {"", ":start:"};
        String[] sufs = {"", ":eol:"};
        
        for (String suf : sufs) {
            for (String pre : pres) {
                for (String sep : seps) {
                    final String join = String.format("fn:strJoin(<%s>, \"%s\", \"%s\", \"%s\") :: xsd:string", predicate, sep, pre, suf);
                    Collection<Object> result = evaluateRule(join, subject);
                    
                    Assert.assertEquals(1, result.size());
                    String joined = result.iterator().next().toString();
                    
                    Assert.assertThat(joined, CoreMatchers.startsWith(pre));
                    Assert.assertThat(joined, CoreMatchers.endsWith(suf));
                    for(String lit: lits) {
                        Assert.assertThat(joined, new RegexMatcher(Pattern.compile(
                                Pattern.quote(lit) + "((" + Pattern.quote(sep) + ")|(" + Pattern.quote(suf) + "$))"
                                )));
                        Assert.assertThat(joined, new RegexMatcher(Pattern.compile(
                                "((^" + Pattern.quote(pre) + ")|(" + Pattern.quote(sep) + "))" + Pattern.quote(lit)
                                )));
                    }
                }
            }
        }
    }
    
    private static class RegexMatcher extends BaseMatcher<String> {
        
        private final Pattern regex;

        public RegexMatcher(Pattern regex) {
            this.regex = regex;
        }

        @Override
        public boolean matches(Object item) {
            return item != null && regex.matcher(item.toString()).find();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a string containing the regular expression ").appendValue(regex.pattern());
        }
        
    }

}
