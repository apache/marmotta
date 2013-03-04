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
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 2/6/12
 * Time: 4:58 AM
 *
 * @author <a href="http://www.rodiontsev.com">Dmitry Rodiontsev</a>
 */
public class GitInfoProvider extends AbstractInfoProvider {
    private static final String DOT_GIT = ".git";

    public boolean isActive(MavenProject project) {
        return isActive(project, DOT_GIT);
    }

    public Map<String, String> getInfo(MavenProject project) {
        File basedir = project.getBasedir();

        InfoScmResult result = null;

        ScmLogger logger = new DefaultLog();

        GitCommand command = new GitExeScmProvider().getInfoCommand();
        command.setLogger(logger);
        try {
            ScmProviderRepository repository = new GitScmProviderRepository(basedir.getAbsolutePath());
            ScmFileSet fileSet = new ScmFileSet(basedir);
            CommandParameters parameters = new CommandParameters();
            result = (InfoScmResult) command.execute(repository, fileSet, parameters);
        } catch (ScmException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage());
            }
        }

        Map<String, String> info = new LinkedHashMap<String, String>();
        if (result != null) {
            if (result.isSuccess()) {
                List<InfoItem> items = result.getInfoItems();
                if ((items != null) && (items.size() == 1)) {
                    info.put("git.revision", items.get(0).getRevision());
                    info.put("git.author", items.get(0).getLastChangedAuthor());
                    info.put("git.repository", items.get(0).getRepositoryUUID());
                    info.put("git.date", items.get(0).getLastChangedDate());
                } else {
                    info.put("git.error", "The command returned incorrect number of arguments");
                }
            } else {
                info.put("git.error", result.getProviderMessage());
            }

        }
        return info;
    }

}
