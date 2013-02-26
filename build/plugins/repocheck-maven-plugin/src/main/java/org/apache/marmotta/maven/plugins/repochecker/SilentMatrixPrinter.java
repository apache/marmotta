package org.apache.marmotta.maven.plugins.repochecker;

import java.util.List;

import org.apache.marmotta.maven.plugins.repochecker.RepositoryCheckerMojo.Result;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;

public class SilentMatrixPrinter implements MatrixPrinter {

	public void printHeader(List<ArtifactRepository> repositories) {
		// nop;
	}

	public void printResult(Artifact artifact, int level, List<Result> results) {
		// nop;
	}

	public void printFooter(List<ArtifactRepository> repositories) {
		// nop;
	}

}
