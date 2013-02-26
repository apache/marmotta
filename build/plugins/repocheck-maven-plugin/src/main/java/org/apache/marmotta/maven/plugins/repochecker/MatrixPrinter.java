package org.apache.marmotta.maven.plugins.repochecker;

import java.util.List;

import org.apache.marmotta.maven.plugins.repochecker.RepositoryCheckerMojo.Result;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;

public interface MatrixPrinter {

	public void printHeader(List<ArtifactRepository> repositories);
	
	public void printResult(Artifact artifact, int level, List<Result> results);
	
	public void printFooter(List<ArtifactRepository> repositories);
	
}
