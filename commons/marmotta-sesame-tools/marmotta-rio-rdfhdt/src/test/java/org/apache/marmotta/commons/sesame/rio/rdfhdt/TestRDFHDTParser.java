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

		String hdtOutput = "test";

		for (int i = 2; i <= 20; i++) {
			String number = "";
			if (i < 10) {
				number += "0";
			}
			System.out.println("-----" + hdtOutput + number + i + ".hdt");
			InputStream input = this.getClass().getResourceAsStream(
					hdtOutput + number + i + ".hdt");
			Repository repository = new SailRepository(new MemoryStore());
			repository.initialize();

			RepositoryConnection connection = repository.getConnection();
			try {
				connection.add(input, "http://example.org/",
						RDFHDTFormat.FORMAT);
				connection.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
				TestCase.fail("parsing failed!");
			}
			TestCase.assertTrue(connection.size() > 0);

			List<Statement> statements = Iterations.asList(connection
					.getStatements(null, null, null, false));

			TestCase.assertTrue(statements.size() > 0);

			for (Statement statement : statements) {
				System.out.println(statement.toString());
			}

			connection.close();
			repository.shutDown();
		}
	}

}
