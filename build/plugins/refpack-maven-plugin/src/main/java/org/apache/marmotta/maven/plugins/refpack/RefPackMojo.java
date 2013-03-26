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
package org.apache.marmotta.maven.plugins.refpack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * Goal which touches a timestamp file.
 *
 * @requiresDependencyResolution compile
 * @goal generate
 *
 * @phase validate
 */
public class RefPackMojo extends AbstractMojo {
    
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The Group Identifier of the artifacts that should be considered local modules. When walking
     * the dependency tree, the process will break at each module of this group id and instead add
     * a dependency to the other module to the refpacks.
     *
     * @parameter expression="${refpack.moduleGroupId}" default-value="org.apache.marmotta"
     * @required
     * @readonly
     */
    private String moduleGroupId;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

    /**
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession session;

    /**
     * The project's remote repositories to use for the resolution of project dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    private List<RemoteRepository> projectRepos;

    /**
     * @component
     */
    private ProjectBuilder projectBuilder;


    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * The required modules of the refpack
     *
     * @parameter expression="${refpack.requiredModules}"
     * @readonly
     */
    private List<String> requiredModules;

    // we collect here the library dependencies of each module, so we can identify which of the dependencies are already
    // covered by another module the current module depends on
    private HashMap<Artifact,Set<Artifact>> moduleLibraries;

    // and here we collect the module dependencies of each module
    private HashMap<Artifact,Set<Artifact>> moduleDependencies;

    public void execute() throws MojoExecutionException {
        moduleLibraries = new HashMap<Artifact, Set<Artifact>>();
        moduleDependencies = new HashMap<Artifact, Set<Artifact>>();

        getLog().info("generating reference packs for group id "+moduleGroupId);

        for(org.apache.maven.artifact.Artifact artifact : (Set<org.apache.maven.artifact.Artifact>)project.getArtifacts()) {
            if(artifact.getGroupId().equals(moduleGroupId)) {

                DefaultArtifact aetherArtifact = new DefaultArtifact(artifact.getGroupId(),artifact.getArtifactId(), artifact.getType(), artifact.getVersion());
                Dependency rootDependency = new Dependency(aetherArtifact, "runtime");

                try {
                    CollectRequest collectRequest = new CollectRequest();
                    collectRequest.setRoot(rootDependency);
                    collectRequest.setRepositories(projectRepos);

                    DependencyNode rootNode = repoSystem.collectDependencies( session, collectRequest ).getRoot();

                    DependencyRequest request = new DependencyRequest(rootNode,null);
                    DependencyResult result = repoSystem.resolveDependencies(session,request);


                    getLog().info("Artifact: " + aetherArtifact);
                    for(DependencyNode child : result.getRoot().getChildren()) {
                        if(child.getDependency().getArtifact().getGroupId().equals(moduleGroupId)) {
                            processModule(child);
                        }
                    }
                    processModule(result.getRoot());
                    /*
                    deps = aether.resolve(aetherArtifact, JavaScopes.RUNTIME);

                    getLog().info("Artifact: "+aetherArtifact);
                    for(Artifact dep : deps) {
                        getLog().info("- dependency "+dep.getFile().getAbsolutePath());
                    }
                    */
                } catch (DependencyResolutionException e) {
                    getLog().warn("could not resolve dependencies for artifact "+aetherArtifact,e);
                } catch (DependencyCollectionException e) {
                    getLog().warn("could not resolve dependencies for artifact "+aetherArtifact,e);
                }
            }

        }

    }

    /**
     * Collect the dependencies to other libraries that are not already collected by modules the current
     * module depends on.
     *
     * @param node
     * @param currentModule
     */
    private void collectLibraryDependencies(DependencyNode node, Artifact currentModule) {
		String groupId = node.getDependency().getArtifact().getGroupId();
        String artifactId = node.getDependency().getArtifact().getArtifactId();
        if(!groupId.equals(moduleGroupId) || !artifactId.startsWith("marmotta-") || artifactId.equals("marmotta-commons") || artifactId.equals("marmotta-client-js")) {
            // first check if the current artifact is already covered by a module the current module depends on
            for(Artifact dependentArtifact : moduleDependencies.get(currentModule)) {
                if(moduleLibraries.containsKey(dependentArtifact) &&
                        moduleLibraries.get(dependentArtifact).contains(node.getDependency().getArtifact())) {
                    return;
                }
            }

            // collect the current dependency for the module
            moduleLibraries.get(currentModule).add(node.getDependency().getArtifact());

            for(DependencyNode child : node.getChildren()) {
                collectLibraryDependencies(child, currentModule);
            }
        }
    }

    /**
     * Collect the dependencies to other modules inside the same project
     * @param node
     * @param currentModule
     */
    private void collectModuleDependencies(DependencyNode node, Artifact currentModule) {
		String groupId = node.getDependency().getArtifact().getGroupId();
        String artifactId = node.getDependency().getArtifact().getArtifactId();
		if(groupId.equals(moduleGroupId) && artifactId.startsWith("marmotta-") && !artifactId.equals("marmotta-commons") && !artifactId.equals("marmotta-client-js")) {
            moduleDependencies.get(currentModule).add(node.getDependency().getArtifact());
        }
    }

    private void processModule(DependencyNode moduleNode) {
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(moduleNode.getDependency());
        collectRequest.setRepositories(projectRepos);
        try {
            // collect all the dependency graph for the module, and print it until we reach a dependency to a local module
            DependencyNode rootNode = repoSystem.collectDependencies( session, collectRequest ).getRoot();

            DependencyRequest request = new DependencyRequest(rootNode,null);
            DependencyResult result = repoSystem.resolveDependencies(session,request);

            // add entry to module dependencies
            moduleLibraries.put(moduleNode.getDependency().getArtifact(), new HashSet<Artifact>());
            moduleDependencies.put(moduleNode.getDependency().getArtifact(), new HashSet<Artifact>());

            getLog().info("processing module "+moduleNode.getDependency().getArtifact().getArtifactId()+":");
            for(DependencyNode child : result.getRoot().getChildren()) {
                collectModuleDependencies(child,moduleNode.getDependency().getArtifact());
            }
            for(DependencyNode child : result.getRoot().getChildren()) {
                collectLibraryDependencies(child, moduleNode.getDependency().getArtifact());
            }

            // information output
            /*
            for(Artifact otherModule : moduleDependencies.get(moduleNode.getDependency().getArtifact())) {
                getLog().info(" - depending on module "+otherModule.getArtifactId());
            }
            for(Artifact library : moduleLibraries.get(moduleNode.getDependency().getArtifact())) {
                getLog().info(" - depending on library "+library);
            }
            */

            File destination = new File(outputDirectory, moduleNode.getDependency().getArtifact().getArtifactId() + ".xml");
            if (!destination.getParentFile().exists()) { 
            	destination.getParentFile().mkdirs();
            }
        	if (!destination.exists()) {
        		destination.createNewFile();
        	}

            getLog().info("writing refpack to " + destination.getAbsolutePath());

            // write to output directory
            writeModuleXML(moduleNode.getDependency().getArtifact(), new FileOutputStream(destination));

        } catch (DependencyCollectionException e) {
            getLog().error("error while collecting dependencies for module " + moduleNode.getDependency().getArtifact(), e);
        } catch (DependencyResolutionException e) {
            getLog().error("error while resolving dependencies for module " + moduleNode.getDependency().getArtifact(), e);
        } catch (IOException e) {
            getLog().error("I/O error while writing refpack for module " + moduleNode.getDependency().getArtifact(), e);
        }

    }

    private void writeModuleXML(Artifact module, OutputStream out) throws IOException {
        Element installation = new Element("installation");
        installation.setAttribute("version","1.0");

        Element packs = new Element("packs");
        installation.addContent(packs);

        Element pack = new Element("pack");
        packs.addContent(pack);

        // get the model for the artifact, we read name and description from it

        Model pom = getArtifactModel(module);

        // set name of pack from artifact
        if(pom != null && pom.getName() != null) {
            pack.setAttribute("name",pom.getName());
        } else {
            pack.setAttribute("name",module.getArtifactId());
        }

        if(pom != null && pom.getDescription() != null) {
            Element description = new Element("description");
            description.addContent(pom.getDescription());
            pack.addContent(description);
        }

        // add a file entry for the module itself
        if(!module.getExtension().equals("war")) {
            Element mainFile = new Element("file");
            pack.addContent(mainFile);
            mainFile.setAttribute("src",module.getFile().getAbsolutePath());
            mainFile.setAttribute("targetdir","$INSTALL_PATH/apache-tomcat-$TOMCAT_VERSION/webapps/marmotta/WEB-INF/lib");
        }

        // add a file entry for each library of the artifact
        for(Artifact library : moduleLibraries.get(module)) {
            Element file = new Element("file");
            pack.addContent(file);
            file.setAttribute("src",library.getFile().getAbsolutePath());
            file.setAttribute("targetdir","$INSTALL_PATH/apache-tomcat-$TOMCAT_VERSION/webapps/marmotta/WEB-INF/lib");
        }

        // add a depends name for each module the current one depends on  (in case the project is not the webapp)
        if(!module.getExtension().equals("war")) {
            if(requiredModules.contains(module.getArtifactId())) {
                pack.setAttribute("required","yes");
            } else {
                pack.setAttribute("required","no");
            }

            for(Artifact dependency : moduleDependencies.get(module)) {
                Element depends = new Element("depends");
                pack.addContent(depends);

                // get the model for the artifact, we read name and description from it
                Model pom2 = getArtifactModel(dependency);

                // set name of pack from artifact
                if(pom2 != null && pom2.getName() != null) {
                    depends.setAttribute("packname", pom2.getName());
                } else {
                    depends.setAttribute("packname",module.getArtifactId());
                }
            }
        } else {
            pack.setAttribute("required","yes");

            // add webapp directory from installer configuration
            Element appDir = new Element("fileset");
            appDir.setAttribute("dir",outputDirectory+"/../webapp/");
            appDir.setAttribute("targetdir","$INSTALL_PATH/apache-tomcat-$TOMCAT_VERSION/webapps/marmotta/");
            appDir.setAttribute("includes","**");

            pack.addContent(appDir);

            Element logDir = new Element("fileset");
            logDir.setAttribute("dir",outputDirectory+"/../log/");

            logDir.setAttribute("targetdir","$INSTALL_PATH/apache-tomcat-$TOMCAT_VERSION/logs/");
            logDir.setAttribute("includes","**");

            pack.addContent(logDir);
        }

        XMLOutputter writer = new XMLOutputter(Format.getPrettyFormat());
        writer.output(installation,out);

    }

    private Model getArtifactModel(Artifact artifact) {
        org.apache.maven.artifact.Artifact mavenArtifact = artifactFactory.createArtifact(artifact.getGroupId(),artifact.getArtifactId(),artifact.getVersion(),"runtime",artifact.getExtension());

        DefaultProjectBuildingRequest req = new DefaultProjectBuildingRequest();
        req.setRepositorySession(session);
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_STRICT);

        try {
            ProjectBuildingResult res = projectBuilder.build(mavenArtifact, req);

            return res.getProject().getModel();
        } catch (ProjectBuildingException e) {
            getLog().warn("error building artifact model for artifact "+artifact,e);
            return null;
        }

    }
}
