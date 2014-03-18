/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.kiwi.loader.csv;

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
