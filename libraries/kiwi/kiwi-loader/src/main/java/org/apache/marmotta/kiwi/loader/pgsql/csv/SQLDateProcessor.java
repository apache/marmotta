package org.apache.marmotta.kiwi.loader.pgsql.csv;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.DateCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

import java.util.Date;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLDateProcessor extends CellProcessorAdaptor implements DateCellProcessor {


    /**
     * Constructor used by CellProcessors to indicate that they are the last processor in the chain.
     */
    public SQLDateProcessor() {
    }

    /**
     * Constructor used by CellProcessors that require <tt>CellProcessor</tt> chaining (further processing is required).
     *
     * @param next the next <tt>CellProcessor</tt> in the chain
     * @throws NullPointerException if next is null
     */
    public SQLDateProcessor(CellProcessor next) {
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

        if( !(value instanceof Date) ) {
            throw new SuperCsvCellProcessorException(Date.class, value, context, this);
        }

        java.sql.Date date = new java.sql.Date(((Date)value).getTime());
        return date.toString();
    }
}
