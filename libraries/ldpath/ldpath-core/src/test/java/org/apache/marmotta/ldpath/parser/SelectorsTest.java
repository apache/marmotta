package org.apache.marmotta.ldpath.parser;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.hamcrest.CoreMatchers;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SelectorsTest {

    @Parameters(name = "{index}: {1}Selector")
    public static List<String[]> testCases() {
        return Arrays.asList(new String[][] {
                {"*", "Wildcard"},
                {"<http://www.example.com/>", "Property"},
                {"<http://foo.bar> / <http://bar.foo>", "Path"},
                {"<http://foo.bar> & <http://bar.foo>", "Intersection"},
                {"<http://foo.bar> | <http://bar.foo>", "Union"},
                {"<http://foo.bar>[<http://bar.foo>]", "Testing"},
                {"(<http://www.example.com/>[@en])", "Grouped"},
                {"(<http://www.example.com/>)*", "RecursivePath"},
                {"(<http://www.example.com/>)+", "RecursivePath"},
                {"^<http://www.example.com/>", "ReverseProperty"},
                {"fn:count(\"foo\")", "Function"},
                // Not implemented yet: {"^*", "ReverseProperty"},
                {".", "Self"},
                {"\"Hello World\"", "StringConstant"},
                });
    }
    
    @Parameter
    public String expr;
    
    @Parameter(1)
    public String name;

    private NodeSelector<String> selector;
    
    private static NodeBackend<String> backend;

    @BeforeClass
    public static void beforeClass() {
        backend = new StringTestingBackend();
    }
    
    @Before
    public void before() throws ParseException {
        LdPathParser<String> rdfPathParser = new LdPathParser<String>(backend,new StringReader(expr));
        selector = rdfPathParser.parseSelector(Collections.<String,String>emptyMap());
    }
    
    @Test
    public void testGetPathExpression() {
        Assert.assertThat(selector.getPathExpression(backend), IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expr));
    }
    
    @Test
    public void testParseSelector() {
        final String className = "org.apache.marmotta.ldpath.model.selectors." + name + "Selector";
        try {
            final Class<?> cls = Class.forName(className);
            Assert.assertThat(selector, CoreMatchers.instanceOf(cls));
        } catch (ClassNotFoundException e) {
            Assert.fail("Could not load class: " + className);
        }
        
    }
    
    
}
