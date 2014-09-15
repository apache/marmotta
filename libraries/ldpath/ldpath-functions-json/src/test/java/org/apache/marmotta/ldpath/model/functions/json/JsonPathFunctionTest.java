package org.apache.marmotta.ldpath.model.functions.json;

import com.google.common.collect.Lists;
import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@RunWith(Parameterized.class)
public class JsonPathFunctionTest extends AbstractTestBase {

    @Parameterized.Parameters(name = "{1}")
    public static List<String[]> data() {
        ArrayList<String[]> data = new ArrayList<String[]>();
        data.add(new String[] { "$.string", "stringValue" });
        data.add(new String[] { "$.int", "0" });
        data.add(new String[] { "$..child[0]", "a" });
        data.add(new String[] { "$.array", "[{\"child\":\"a\"},{\"child\":\"b\"}]" });
        data.add(new String[] { "$..child", "[\"a\",\"b\"]" });
        return data;
    }

    @Parameterized.Parameter
    public String path;
    @Parameterized.Parameter(1)
    public String answer;

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        super.loadData("data.n3", RDFFormat.N3);
    }

    @Test
    public void testJsonPathFunction() throws ParseException {

        final URI ctx = repository.getValueFactory().createURI(NSS.get("ex") + "Quiz");

        final LdPathParser<Value> parser = createParserFromString("fn:jsonpath(\"" + path + "\", <http://www.w3.org/2011/content#chars>) :: xsd:string");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);
        final Collection<Object> values = rule.getValues(backend, ctx);

        Assert.assertEquals(1, values.size());
        Assert.assertEquals(answer, values.iterator().next());
    }
}