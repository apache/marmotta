/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.maven.plugins.repochecker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.codehaus.plexus.util.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

@Mojo(name = "matrix", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class RepositoryCheckerMojo extends AbstractMojo {

	private static final String META_XML = "maven-metadata.xml";

	public enum Result {
		FOUND, NOT_FOUND, IGNORED, ERROR
	}

	/**
	 * The Maven project.
	 */
	@Component
	private MavenProject project;

	/**
	 * The dependency tree builder to use.
	 */
	@Component(hint = "default")
	private DependencyGraphBuilder dependencyGraphBuilder;

	@Parameter(property = "repositories")
	private String[] repositories;

	@Parameter(property = "checkSnapshots", defaultValue = "true")
	private boolean checkSnapshots;

	@Parameter(property = "depth", defaultValue = "1")
	private int depth;

	@Parameter(property = "breakOnMissing", defaultValue = "false")
	private boolean breakOnMissing;

	@Parameter(property = "silent", defaultValue = "false")
	private boolean silent;

	private final ResponseHandler<Boolean> fileExistsHandler;

	public RepositoryCheckerMojo() {
		fileExistsHandler = new ResponseHandler<Boolean>() {
			public Boolean handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				return response.getStatusLine().getStatusCode() < 300;
			}
		};
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		ClientConnectionManager manager = new PoolingClientConnectionManager();
		final DefaultHttpClient client = new DefaultHttpClient(manager);

		final MatrixPrinter printer;
		if (silent) {
			printer = new SilentMatrixPrinter();
		} else
			printer = new LogMatrixPrinter(getLog(), 1);

		try {
			final Log log = getLog();
			final List<ArtifactRepository> reps;
			if (repositories != null && repositories.length > 0) {
				@SuppressWarnings("unchecked")
				final LinkedList<ArtifactRepository> _tmp = new LinkedList<ArtifactRepository>(
						project.getRemoteArtifactRepositories());
				reps = new LinkedList<ArtifactRepository>();

				for (String rid : repositories) {
					ArtifactRepository r = null;
					for (ArtifactRepository ar : _tmp) {
						if (rid.equals(ar.getId())) {
							r = ar;
							break;
						}
					}
					if (r != null)
						reps.add(r);
					else
						log.warn("Could not find artifact repository '" + rid
								+ "'");
				}

				if (reps.size() == 0) {
					log.warn("No artifact repositories provided, skipping.");
					return;
				}
			} else {
				@SuppressWarnings("unchecked")
				final LinkedList<ArtifactRepository> _tmp = new LinkedList<ArtifactRepository>(
						project.getRemoteArtifactRepositories());
				reps = _tmp;
			}

			printer.printHeader(reps);

			final DependencyNode rootNode = dependencyGraphBuilder
					.buildDependencyGraph(project, null);
			Set<Artifact> missingArtifacts = checkDepNode(rootNode, reps, 0, client, printer);

			printer.printFooter(reps);
			
			if (missingArtifacts.size() > 0) {
				log.warn("unresolved dependencies:");
				for (Artifact missing : missingArtifacts) {
					log.warn("  " + missing.getId());
				}
			}
		} catch (DependencyGraphBuilderException e) {
			throw new MojoExecutionException(
					"Cannot build project dependency graph", e);
		} finally {
			client.getConnectionManager().shutdown();
		}
	}

	private Set<Artifact> checkDepNode(final DependencyNode rootNode,
			final List<ArtifactRepository> repositories, final int level,
			DefaultHttpClient client, MatrixPrinter printer)
			throws MojoFailureException {
		if (!(level < depth))
			return Collections.emptySet();

		HashSet<Artifact> missingArts = new HashSet<Artifact>();
		for (DependencyNode dep : rootNode.getChildren()) {
			Artifact artifact = dep.getArtifact();

			if (!checkSnapshots && artifact.isSnapshot()) {
				printer.printResult(artifact, level, Collections.nCopies(repositories.size(), Result.IGNORED));
			} else {
				final LinkedList<Result> results = new LinkedList<RepositoryCheckerMojo.Result>();
				for (ArtifactRepository repo : repositories) {
					Result result = lookupArtifact(artifact, repo, client);
					results.add(result);
				}

				if (!results.contains(Result.FOUND)) {
					missingArts.add(artifact);
					if (breakOnMissing) {
						throw new MojoFailureException(
								String.format(
										"did not find artifact %s in any of the available repositories",
										artifact.getId()));
					}
				}

				printer.printResult(artifact, level, results);
				missingArts.addAll(checkDepNode(dep, repositories, level + 1, client, printer));
			}
		}
		
		return missingArts;
	}

	private Result lookupArtifact(Artifact artifact, ArtifactRepository rep,
			DefaultHttpClient client) {

		if (artifact.isSnapshot() && !rep.getSnapshots().isEnabled()) {
			return Result.NOT_FOUND;
		}
		if (artifact.isRelease() && !rep.getReleases().isEnabled()) {
			return Result.NOT_FOUND;
		}

		try {
			final String baseUrl = rep.getUrl().replaceAll("/$", "") + "/";

			if (client.execute(new HttpHead(baseUrl + buildRelUrl(artifact)),
					fileExistsHandler)) {
				return Result.FOUND;
			}
			if (artifact.isSnapshot()) {
				// now check for a timestamp version
				final String fName = client.execute(new HttpGet(baseUrl
						+ buildArtifactDir(artifact) + META_XML),
						createTimestampHandler(artifact));
				if (fName != null
						&& client.execute(new HttpHead(baseUrl
								+ buildArtifactDir(artifact) + fName),
								fileExistsHandler)) {
					return Result.FOUND;
				} else {
					return Result.NOT_FOUND;
				}
			} else {
				return Result.NOT_FOUND;
			}
		} catch (IOException e) {
			return Result.ERROR;
		}
	}

	private ResponseHandler<String> createTimestampHandler(
			final Artifact artifact) {
		return new ResponseHandler<String>() {

			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				if (response.getStatusLine().getStatusCode() >= 300) {
					return null;
				}
				if (response.getEntity() == null)
					return null;
				InputStream content = response.getEntity().getContent();
				if (content == null)
					return null;

				try {
					Document doc = new SAXBuilder().build(content);

					Element meta = doc.getRootElement();
					if (!"metadata".equals(meta.getName()))
						throw new IOException();
					Element vers = meta.getChild("versioning");
					if (vers == null)
						throw new IOException();
					Element sVers = vers.getChild("snapshotVersions");
					if (sVers == null)
						throw new IOException();

					for (Element sv : sVers.getChildren("snapshotVersion")) {
						// if there is a classifier, check if it's the right one
						if (artifact.hasClassifier()) {
							if (!artifact.getClassifier().equals(
									sv.getChildText("classifier"))) {
								continue;
							}
						}
						if (!artifact.getType().equals(
								sv.getChildText("extension"))) {
							continue;
						}

						// If we reach this, then it's the right snapshotVersion
						StringBuilder sb = new StringBuilder(
								artifact.getArtifactId());
						sb.append("-").append(sv.getChildText("value"));
						if (artifact.hasClassifier())
							sb.append("-")
									.append(sv.getChildText("classifier"));
						sb.append(".").append(sv.getChildText("extension"));

						return sb.toString();
					}
					return null;
				} catch (JDOMException e) {
					throw new IOException(e);
				}
			}
		};
	}

	private String buildRelUrl(Artifact artifact) {
		StringBuilder sb = new StringBuilder(buildArtifactDir(artifact));
		sb.append(buildArtifactFileName(artifact));
		return sb.toString();
	}

	private String buildArtifactFileName(Artifact artifact) {
		StringBuilder sb = new StringBuilder();

		sb.append(artifact.getArtifactId());
		sb.append("-").append(artifact.getVersion());

		if (!StringUtils.isBlank(artifact.getClassifier()))
			sb.append("-").append(artifact.getClassifier());

		sb.append(".").append(artifact.getType());

		return sb.toString();
	}

	private String buildArtifactDir(Artifact artifact) {
		StringBuilder sb = new StringBuilder(artifact.getGroupId().replaceAll(
				"\\.", "/"));

		sb.append("/").append(artifact.getArtifactId());
		sb.append("/").append(artifact.getVersion());
		sb.append("/");

		return sb.toString();
	}

}
