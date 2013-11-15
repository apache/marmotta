package org.apache.marmotta.kiwi.loader.pgsql.csv;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

import java.util.Locale;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LanguageProcessor extends CellProcessorAdaptor implements CellProcessor {

    /**
     * Constructor used by CellProcessors to indicate that they are the last processor in the chain.
     */
    public LanguageProcessor() {
    }

    /**
     * Constructor used by CellProcessors that require <tt>CellProcessor</tt> chaining (further processing is required).
     *
     * @param next the next <tt>CellProcessor</tt> in the chain
     * @throws NullPointerException if next is null
     */
    public LanguageProcessor(CellProcessor next) {
        super(next);
    }

    /**
     * This method is invoked by the framework when the processor needs to process data or check constraints.
     *
     * @since 1.0
     */
    @Override
    public Object execute(Object value, CsvContext context) {
        validateInputNotNull(value, context);

        if( !(value instanceof Locale) ) {
            throw new SuperCsvCellProcessorException(Locale.class, value, context, this);
        }

        return ((Locale)value).getLanguage();
    }
}
