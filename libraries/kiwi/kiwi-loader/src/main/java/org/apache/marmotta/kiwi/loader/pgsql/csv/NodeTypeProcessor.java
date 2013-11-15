package org.apache.marmotta.kiwi.loader.pgsql.csv;

import org.apache.marmotta.kiwi.model.rdf.KiWiAnonResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiBooleanLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDateLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDoubleLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiIntLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiStringLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

/**
 * convert KiWiNode subclasses into their proper nodetype
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class NodeTypeProcessor extends CellProcessorAdaptor implements CellProcessor {

    /**
     * Constructor used by CellProcessors to indicate that they are the last processor in the chain.
     */
    public NodeTypeProcessor() {
    }

    /**
     * Constructor used by CellProcessors that require <tt>CellProcessor</tt> chaining (further processing is required).
     *
     * @param next the next <tt>CellProcessor</tt> in the chain
     * @throws NullPointerException if next is null
     */
    public NodeTypeProcessor(CellProcessor next) {
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

        if( !(value instanceof Class) ) {
            throw new SuperCsvCellProcessorException(Class.class, value, context, this);
        }


        if(KiWiUriResource.class.equals(value)) {
            return "uri";
        } else if(KiWiAnonResource.class.equals(value)) {
            return "bnode";
        } else if(KiWiStringLiteral.class.equals(value)) {
            return "string";
        } else if(KiWiIntLiteral.class.equals(value)) {
            return "int";
        } else if(KiWiDoubleLiteral.class.equals(value)) {
            return "double";
        } else if(KiWiDateLiteral.class.equals(value)) {
            return "date";
        } else if(KiWiBooleanLiteral.class.equals(value)) {
            return "boolean";
        } else {
            return "string";
        }
    }
}
