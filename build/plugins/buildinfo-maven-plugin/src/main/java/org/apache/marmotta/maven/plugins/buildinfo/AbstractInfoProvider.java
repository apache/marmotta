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
package org.apache.marmotta.maven.plugins.buildinfo;

import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Date: 2/6/12
 * Time: 10:31 AM
 *
 * @author <a href="http://www.rodiontsev.com">Dmitry Rodiontsev</a>
 */
public abstract class AbstractInfoProvider implements InfoProvider {

    private File lookupDirectory(MavenProject project, String child) throws FileNotFoundException {
        File dir, vcs;

        // walk up the directory structure looking for the .git or .hg directory
        dir = project.getBasedir();

        while(dir != null) {
            vcs = new File(dir, child);
            if (vcs.exists() && vcs.isDirectory()) {
                return dir;
            }
            dir = dir.getParentFile();
        }


        //Walk up the project parent hierarchy seeking the .hg directory
/*
        MavenProject mavenProject = project;
        while (mavenProject != null) {
            dir = new File(mavenProject.getBasedir(), child);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
            // If we've reached the top-level parent and not found the .git directory, look one level further up
            if (mavenProject.getParent() == null && mavenProject.getBasedir() != null) {
                dir = new File(mavenProject.getBasedir().getParentFile(), child);
                if (dir.exists() && dir.isDirectory()) {
                    return dir;
                }
            }
            mavenProject = mavenProject.getParent();
        }
*/

        throw new FileNotFoundException("Could not find " + child + " directory");
    }

    protected boolean isActive(MavenProject project, String child) {
        boolean result = false;
        try {
            File dir = lookupDirectory(project, child);
            result = (dir.exists() && dir.isDirectory()); //redundant check
        } catch (FileNotFoundException e) {
            //do nothing
        }
        return result;
    }

}
