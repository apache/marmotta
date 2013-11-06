package org.apache.marmotta.kiwi.sparql.function;

import org.apache.marmotta.kiwi.vocabulary.FN_MARMOTTA;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;

/**
 * A SPARQL function for doing a full-text search on the content of a string. Should be implemented directly in
 * the database, as the in-memory implementation is non-functional.
 * <p/>
 * The function can be called either as:
 * <ul>
 *     <li>fn:fulltext-search(?var, 'query') - using a generic stemmer and dictionary</li>
 *     <li>
 *         fn:fulltext-search(?var, 'query', 'language') - using a language-specific stemmer and dictionary
 *         (currently only supported by PostgreSQL with the language values 'english', 'german', 'french', 'italian', 'spanish'
 *         and some other languages as supported by PostgreSQL).
 *     </li>*
 * </ul>
 * Note that for performance reasons it might be preferrable to create a full-text index for your database. Please
 * consult your database documentation on how to do this.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class FulltextSearchFunction implements Function {

    // auto-register for SPARQL environment
    static {
        if(!FunctionRegistry.getInstance().has(FN_MARMOTTA.SEARCH_FULLTEXT.toString())) {
            FunctionRegistry.getInstance().add(new FulltextSearchFunction());
        }
    }

    @Override
    public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        throw new UnsupportedOperationException("cannot evaluate in-memory, needs to be supported by the database");
    }

    @Override
    public String getURI() {
        return FN_MARMOTTA.SEARCH_FULLTEXT.toString();
    }
}
