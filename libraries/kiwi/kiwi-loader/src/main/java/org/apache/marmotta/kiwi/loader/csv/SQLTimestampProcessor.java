package org.apache.marmotta.kiwi.loader.csv;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.DateCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLTimestampProcessor extends CellProcessorAdaptor implements DateCellProcessor {

    /**
     * This method is invoked by the framework when the processor needs to process data or check constraints.
     *
     * @since 1.0
     */
    @Override
    public Object execute(Object value, CsvContext context) {
        if(value == null) {
            return null;
        }

        if( !(value instanceof Date) ) {
            throw new SuperCsvCellProcessorException(Date.class, value, context, this);
        }

        Timestamp date = new Timestamp(((Date)value).getTime());
        return date.toString();
    }
}
