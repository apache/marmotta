package org.apache.marmotta.kiwi.loader.csv;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.BoolCellProcessor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLBooleanProcessor extends CellProcessorAdaptor implements BoolCellProcessor {

    /**
     * Constructor used by CellProcessors to indicate that they are the last processor in the chain.
     */
    public SQLBooleanProcessor() {
    }

    /**
     * Constructor used by CellProcessors that require <tt>CellProcessor</tt> chaining (further processing is required).
     *
     * @param next the next <tt>CellProcessor</tt> in the chain
     * @throws NullPointerException if next is null
     */
    public SQLBooleanProcessor(CellProcessor next) {
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

        if( !(value instanceof Boolean) ) {
            throw new SuperCsvCellProcessorException(Boolean.class, value, context, this);
        }

        if( ((Boolean)value).booleanValue()) {
            return "t";
        } else {
            return "f";
        }
    }
}
