/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.maven.plugins.buildinfo;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Date: 13.05.2011
 * Time: 12:00:00
 *
 * @author <a href="http://www.rodiontsev.com">Dmitry Rodiontsev</a>
 *
 * @goal extract
 * @phase prepare-package
 */
public class BuildInfoMojo extends AbstractMojo {
    private static final String BUILD_INFO_FILE_NAME = "build.info";
    private static final String DEFAULT_VALUE = "";

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;


    private static Map<String,String> lmfCommiters = new HashMap<String, String>();
    static {
        lmfCommiters.put("sschaffe", "Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>");
        lmfCommiters.put("tkurz", "Thomas Kurz <thomas.kurz@salzburgresearch.at>");
        lmfCommiters.put("dglachs", "Dietmar Glachs <dietmar.glachs@salzburgresearch.at>");
        lmfCommiters.put("jfrank", "Jakob Frank <jakob.frank@salzburgresearch.at>");
        lmfCommiters.put("jakob","Jakob Frank <jakob.frank@salzburgresearch.at>");
        lmfCommiters.put("sfernandez","Sergio Fernández <sergio.fernandez@salzburgresearch.at>");
    }


    /**
     * @parameter
     */
    private List<String> systemProperties;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Map<String, String> map = new LinkedHashMap<String, String>();

        for (InfoProvider provider : ServiceLoader.load(InfoProvider.class)) {
            if (provider.isActive(project)) {
                map.putAll(provider.getInfo(project));
            }
        }

        if (systemProperties != null) {
            for (String property : systemProperties) {
                map.put(property, System.getProperty(property, DEFAULT_VALUE));
            }
        }

        // operating system
        map.put("build.os", System.getProperty("os.name") + " " + System.getProperty("os.version") + "/" + System.getProperty("os.arch"));

        // host name
        try {
            map.put("build.host",java.net.InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
        }

        // user name
        if(lmfCommiters.get(System.getProperty("user.name")) != null) {
            map.put("build.user", lmfCommiters.get(System.getProperty("user.name")));
        } else {
            map.put("build.user", System.getProperty("user.name"));
        }

        Build build = project.getBuild();
        StringBuilder filename = new StringBuilder();
        filename.append(build.getOutputDirectory()).append(File.separator).append(BUILD_INFO_FILE_NAME);

        getLog().info("Writing to file " + filename.toString());

        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename.toString()), "UTF-8"));
            for (Map.Entry<String, String> entry : map.entrySet()) {
                out.write(entry.getKey());
                out.write(" = ");
                out.write(entry.getValue());
                out.write("\n");
            }
            out.flush();
        } catch (IOException ioe) {
            getLog().warn(ioe.getMessage());
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

}
