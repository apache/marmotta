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
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 5/17/11
 * Time: 3:08 PM
 *
 * @author <a href="http://www.rodiontsev.com">Dmitry Rodiontsev</a>
 */
public class MercurialInfoProvider extends AbstractInfoProvider {
    private static final String DOT_HG = ".hg";
    private static final Pattern HG_OUTPUT_PATTERN = Pattern.compile("^(\\S+)\\s(\\S+)\\s(.+)$");

    public boolean isActive(MavenProject project) {
        return isActive(project, DOT_HG);
    }

    public Map<String, String> getInfo(MavenProject project) {
        ScmLogger logger = new DefaultLog();
        HgLogConsumer consumer = new HgLogConsumer(logger);
        ScmResult result = null;
        try {
            result = HgUtils.execute(
                    consumer,
                    logger,
                    project.getBasedir(),
                    new String[]{HgCommandConstants.REVNO_CMD, "-n", "-i", "-b"});
        } catch (ScmException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage());
            }
        }

        Map<String, String> info = new LinkedHashMap<String, String>();
        if (result != null) {
            if (result.isSuccess()) {
                String output = result.getCommandOutput();
                if (output != null) {
                    Matcher matcher  = HG_OUTPUT_PATTERN.matcher(output);
                    if (matcher.find() && matcher.groupCount() == 3) {
                        StringBuilder changeset = new StringBuilder();
                        changeset.append("r").append(matcher.group(2)).append(":").append(matcher.group(1));
                        info.put("build.changeset", changeset.toString());
                        info.put("build.branch", matcher.group(3));
                        info.put("build.revision", matcher.group(2));
                        info.put("build.revhash", matcher.group(1));
                    } else {
                        info.put("hg.error", "The command returned incorrect number of arguments");
                    }
                }
            } else {
                info.put("hg.error", result.getProviderMessage());
            }
        }
        return info;
    }
}

class HgLogConsumer extends HgConsumer {
    private final StringBuilder out = new StringBuilder();

    public HgLogConsumer(ScmLogger logger) {
        super(logger);
    }

    @Override
    public void consumeLine(String line) {
        out.append(line).append("\n");
    }

    @Override
    public String getStdErr() {
        return out.toString();
    }
}
