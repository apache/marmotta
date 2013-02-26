package org.apache.marmotta.maven.plugins.repochecker;

import java.util.List;

import org.apache.marmotta.maven.plugins.repochecker.RepositoryCheckerMojo.Result;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

public class LogMatrixPrinter implements MatrixPrinter {

	private char found = '+', notFound = '-', error = 'e', ignored = 'i';
	private final String sep;
	private Log log;

	public LogMatrixPrinter(Log logger, int indent) {
		this.log = logger;
		sep = StringUtils.repeat(" ", indent);
	}

	public void printHeader(List<ArtifactRepository> repositories) {
		log.info("dependencies, and where they are available:");

		final int repCount = repositories.size();
		for (int i = 0; i < repCount; i++) {
			final ArtifactRepository rep = repositories.get(i);
			log.info(String.format("%s%s (%s)",
					StringUtils.repeat("|" + sep, i), rep.getId(), rep.getUrl()));
		}
		log.info(StringUtils.repeat("|" + sep, repCount));
	}

	public void printResult(Artifact artifact, int level, List<Result> results) {

		StringBuilder sb = new StringBuilder();
		for (Result result : results) {
			switch (result) {
			case FOUND:
				sb.append(found);
				break;
			case NOT_FOUND:
				sb.append(notFound);
				break;
			case IGNORED:
				sb.append(ignored);
				break;
			default:
				sb.append(error);
			}
			sb.append(sep);
		}
		sb.append(StringUtils.repeat('|'+sep, level));
		sb.append(artifact.getId());

		log.info(sb.toString());
	}

	public void printFooter(List<ArtifactRepository> repositories) {
		final int repCount = repositories.size();

		log.info(StringUtils.repeat("|" + sep, repCount));
		for (int i = repCount - 1; i >= 0; i--) {
			final ArtifactRepository rep = repositories.get(i);
			log.info(String.format("%s%s (%s)",
					StringUtils.repeat("|" + sep, i), rep.getId(), rep.getUrl()));
		}
	}

}
