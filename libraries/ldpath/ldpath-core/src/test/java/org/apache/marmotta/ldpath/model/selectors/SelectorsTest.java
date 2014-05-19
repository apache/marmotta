package org.apache.marmotta.ldpath.model.selectors;

import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertThat;

public class SelectorsTest  extends AbstractTestBase {

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        loadData("/ldpath/test-data.n3", RDFFormat.N3);
    }

    @Test
    public void testNullCoalescingSelectorLHSMatch() throws ParseException {

        LdPathParser<Value> parser = createParserFromString("foo:title || foo:subtitle :: xsd:string; ");
        final URI context = repository.getValueFactory().createURI("http://www.example.com/1");

        final FieldMapping<Object, Value> field = parser.parseRule(NSS);
        final Collection<Object> result = field.getValues(backend, context);

        assertThat(result, CoreMatchers.hasItem("One"));
    }

    @Test
    public void testNullCoalescingSelectorRHSMatch() throws ParseException {

        LdPathParser<Value> parser = createParserFromString("foo:main_title_doesnt_exist || foo:subtitle :: xsd:string; ");
        final URI context = repository.getValueFactory().createURI("http://www.example.com/1");

        final FieldMapping<Object, Value> field = parser.parseRule(NSS);
        final Collection<Object> result = field.getValues(backend, context);

        assertThat(result, CoreMatchers.hasItem("SubOne"));
    }

    @Test
    public void testNullCoalescingSelectorChainedMatch() throws ParseException {

        LdPathParser<Value> parser = createParserFromString("foo:main_title_doesnt_exist || foo:subtitle || foo:title :: xsd:string; ");
        final URI context = repository.getValueFactory().createURI("http://www.example.com/1");

        final FieldMapping<Object, Value> field = parser.parseRule(NSS);
        final Collection<Object> result = field.getValues(backend, context);

        assertThat(result, CoreMatchers.hasItem("SubOne"));
    }

}
