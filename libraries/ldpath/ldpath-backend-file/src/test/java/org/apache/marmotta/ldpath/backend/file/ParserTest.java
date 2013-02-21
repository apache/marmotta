package org.apache.marmotta.ldpath.backend.file;
/*
 * Copyright (c) 2011 Salzburg Research.
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

import java.io.StringReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.apache.marmotta.ldpath.backend.sesame.SesameRepositoryBackend;
import org.apache.marmotta.ldpath.model.programs.Program;
import org.apache.marmotta.ldpath.model.selectors.PathSelector;
import org.apache.marmotta.ldpath.model.selectors.PropertySelector;
import org.apache.marmotta.ldpath.model.selectors.TestingSelector;
import org.apache.marmotta.ldpath.model.selectors.UnionSelector;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.RdfPathParser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


import com.google.common.collect.ImmutableMap;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ParserTest {

    private static SesameRepositoryBackend backend;

    @BeforeClass
    public static void setupRepository() throws RepositoryException {
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        backend = new SesameRepositoryBackend(repository);
    }



    @Test
    public void testParsePath() throws Exception {
        String path1 = "rdfs:label";

        NodeSelector<Value> s1 = parseSelector(path1, null);
        Assert.assertTrue(s1 instanceof PropertySelector);
        Assert.assertEquals("<http://www.w3.org/2000/01/rdf-schema#label>",s1.getPathExpression(backend));


        Map<String,String> namespaces2 = ImmutableMap.of(
                "dct","http://purl.org/dc/terms/",
                "dbp-ont","http://dbpedia.org/ontology/"
        );
        String path2 = "(*[rdf:type is dbp-ont:Person]) | (dct:subject/^dct:subject[rdf:type is dbp-ont:Person]) | (dct:subject/^skos:broader/^dct:subject[rdf:type is dbp-ont:Person])";
        NodeSelector<Value> s2 = parseSelector(path2,namespaces2);
        Assert.assertTrue(s2 instanceof UnionSelector);

        String path3 = "*[rdf:type is dbp-ont:Person] | dct:subject/^dct:subject[rdf:type is dbp-ont:Person] | dct:subject/^skos:broader/^dct:subject[rdf:type is dbp-ont:Person]";
        NodeSelector<Value> s3 = parseSelector(path3,namespaces2);
        Assert.assertTrue(s3 instanceof UnionSelector);
        
        Assert.assertEquals(s2,s3);

        String path4 = "(* | dct:subject/^dct:subject | dct:subject/^skos:broader/^dct:subject)[rdf:type is dbp-ont:Person]";
        NodeSelector<Value> s4 = parseSelector(path4,namespaces2);
        Assert.assertTrue(s4 instanceof TestingSelector);
    }

    private NodeSelector<Value> parseSelector(String selector, Map<String,String> namespaces) throws ParseException {
        return new RdfPathParser<Value>(backend,new StringReader(selector)).parseSelector(namespaces);
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
        Assert.assertTrue(p3.getField("person").getSelector() instanceof PathSelector);
        Assert.assertTrue(p3.getField("person").getTransformer() instanceof StringTransformer);

    }

    private Program<Value> parseProgram(String selector) throws ParseException {
        return new RdfPathParser<Value>(backend,new StringReader(selector)).parseProgram();
    }


}
