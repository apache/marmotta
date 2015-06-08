package org.apache.marmotta.commons.sesame.rio.rdfhdt;

import info.aduna.iteration.Iterations;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRDFHDTParser {
	
	private static Logger log = LoggerFactory.getLogger(TestRDFHDTParser.class);
	
    @Test
    public void runTest() throws Exception {


//        InputStream turtle = this.getClass().getResourceAsStream("test.ttl");
        
        String baseURI = "http://example.org/triples/";
        String rdfInput = "example";
        String inputType = "rdfxml";
        String hdtOutput = "test";
 
//        for (int i=2 ;i<=20 ;i++){
//        // Create HDT from RDF file
//        String number="";
//        if(i<10){
//        	number +="0";
//        }
//        System.out.println(rdfInput+number+i+".rdf");
//        HDT hdt = HDTManager.generateHDT(
//                            rdfInput+number+i+".rdf",         // Input RDF File
//                            baseURI,          // Base URI
//                            RDFNotation.parse(inputType), // Input Type
//                            new HDTSpecification(),   // HDT Options
//                            null              // Progress Listener
//                );
//
//        hdt.saveToHDT(hdtOutput+number+i+".hdt", null);
//        }
//        System.out.println("Size: " + hdt.getTriples().size() );
//        
//        IteratorTripleString it = hdt.search("", "", "");
//        while(it.hasNext()) {
//            TripleString ts = it.next();
//            System.out.println(ts);
//        }
        
//        InputStream sparql = this.getClass().getResourceAsStream(fileName+".sparql");
//        assumeThat("Could not load testfiles", asList(turtle), everyItem(notNullValue(InputStream.class)));
//
        for (int i=2 ;i<=20 ;i++){
        	String number="";
        	if(i<10){
        		number +="0";
        	}
        	System.out.println("-----" +hdtOutput+number+i+".hdt");
        InputStream input = this.getClass().getResourceAsStream(hdtOutput+number+i+".hdt");
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        RepositoryConnection connection = repository.getConnection();
        try {
            connection.add(input,"http://example.org/", RDFHDTFormat.FORMAT);
            connection.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            TestCase.fail("parsing failed!");
        }
        TestCase.assertTrue(connection.size() > 0);

        List<Statement> statements = Iterations.asList(connection.getStatements(null, null, null, false));
        
        TestCase.assertTrue(statements.size() > 0);
        
        for(Statement statement: statements){
        	System.out.println(statement.toString());
        }

        

        connection.close();
        repository.shutDown();
        }
    }

}
