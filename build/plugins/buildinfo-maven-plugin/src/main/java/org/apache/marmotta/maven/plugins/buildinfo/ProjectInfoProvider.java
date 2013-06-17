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

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.maven.project.MavenProject;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Date: 22.06.11
 * Time: 23:21
 *
 * @author <a href="http://www.rodiontsev.com">Dmitry Rodiontsev</a>
 */
public class ProjectInfoProvider implements InfoProvider {

    public boolean isActive(MavenProject project) {
        return true;
    }

    public Map<String, String> getInfo(MavenProject project) {
        Date date = new Date();
        Map<String, String> info = new LinkedHashMap<String, String>();
        info.put("project.name", project.getName());
        info.put("build.module", project.getArtifactId());
        info.put("build.version", project.getVersion());
        info.put("build.timestamp", DateFormatUtils.format(date, "EEE, dd MMM yyyy HH:mm:ss z"));
        return info;
    }

}
