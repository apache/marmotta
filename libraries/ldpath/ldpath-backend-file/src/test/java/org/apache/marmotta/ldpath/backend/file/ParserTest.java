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
package org.apache.marmotta.ldpath.backend.file;

import java.io.StringReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.apache.marmotta.ldpath.backend.sesame.SesameValueBackend;
import org.apache.marmotta.ldpath.model.programs.Program;
import org.apache.marmotta.ldpath.model.selectors.PathSelector;
import org.apache.marmotta.ldpath.model.selectors.PropertySelector;
import org.apache.marmotta.ldpath.model.selectors.TestingSelector;
import org.apache.marmotta.ldpath.model.selectors.UnionSelector;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;

import com.google.common.collect.ImmutableMap;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ParserTest {

    private static SesameValueBackend backend;

    @BeforeClass
    public static void setupRepository() throws RepositoryException {
        backend = new SesameValueBackend();
    }



    @Test
    public void testParsePath() throws Exception {
        String path1 = "rdfs:label";

        NodeSelector<Value> s1 = parseSelector(path1, null);
        Assert.assertThat(s1, CoreMatchers.instanceOf(PropertySelector.class));
        Assert.assertEquals("<http://www.w3.org/2000/01/rdf-schema#label>",s1.getPathExpression(backend));


        Map<String,String> namespaces2 = ImmutableMap.of(
                "dct","http://purl.org/dc/terms/",
                "dbp-ont","http://dbpedia.org/ontology/"
        );
        String path2 = "(*[rdf:type is dbp-ont:Person]) | (dct:subject/^dct:subject[rdf:type is dbp-ont:Person]) | (dct:subject/^skos:broader/^dct:subject[rdf:type is dbp-ont:Person])";
        NodeSelector<Value> s2 = parseSelector(path2,namespaces2);
        Assert.assertThat(s2, CoreMatchers.instanceOf(UnionSelector.class));

        String path3 = "*[rdf:type is dbp-ont:Person] | dct:subject/^dct:subject[rdf:type is dbp-ont:Person] | dct:subject/^skos:broader/^dct:subject[rdf:type is dbp-ont:Person]";
        NodeSelector<Value> s3 = parseSelector(path3,namespaces2);
        Assert.assertThat(s3, CoreMatchers.instanceOf(UnionSelector.class));
        
        String path4 = "(* | dct:subject/^dct:subject | dct:subject/^skos:broader/^dct:subject)[rdf:type is dbp-ont:Person]";
        NodeSelector<Value> s4 = parseSelector(path4,namespaces2);
        Assert.assertThat(s4, CoreMatchers.instanceOf(TestingSelector.class));
    }

    private NodeSelector<Value> parseSelector(String selector, Map<String,String> namespaces) throws ParseException {
        return new LdPathParser<Value>(backend,new StringReader(selector)).parseSelector(namespaces);
    }

    @Test
    public void testParseProgram() throws Exception {

        Program<Value> p1 = parseProgram(IOUtils.toString(ParserTest.class.getResource("stanbol.search")));
        Assert.assertEquals(12,p1.getFields().size());
        Assert.assertNull(p1.getBooster());
        Assert.assertNotNull(p1.getFilter());
        Assert.assertEquals(5,p1.getNamespaces().size());


        Program<Value> p2 = parseProgram(IOUtils.toString(ParserTest.class.getResource("sn.search")));
        Assert.assertEquals(11,p2.getFields().size());
        Assert.assertNotNull(p2.getBooster());
        Assert.assertNotNull(p2.getFilter());
        Assert.assertEquals(8,p2.getNamespaces().size());

        Program<Value> p3 = parseProgram(IOUtils.toString(ParserTest.class.getResource("orf.search")));
        Assert.assertEquals(18,p3.getFields().size());
        Assert.assertNull(p3.getBooster());
        Assert.assertNotNull(p3.getFilter());
        Assert.assertEquals(5, p3.getNamespaces().size());
        Assert.assertNotNull(p3.getField("person"));
        Assert.assertThat(p3.getField("person").getSelector() , CoreMatchers.instanceOf(PathSelector.class));
        Assert.assertThat(p3.getField("person").getTransformer(), CoreMatchers.instanceOf( StringTransformer.class));

    }

    private Program<Value> parseProgram(String selector) throws ParseException {
        return new LdPathParser<Value>(backend,new StringReader(selector)).parseProgram();
    }


}
