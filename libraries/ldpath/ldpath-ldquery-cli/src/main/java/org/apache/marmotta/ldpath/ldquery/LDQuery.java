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
package org.apache.marmotta.ldpath.ldquery;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.jr.ob.JSON;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.backend.linkeddata.LDCacheBackend;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Command line application for querying input from the LinkedDataCloud.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDQuery {

    private static final Logger log = LoggerFactory.getLogger(LDQuery.class);

    public static void main(String[] args) {
        Options options = buildOptions();

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse( options, args);

            Level logLevel = Level.WARN;

            if(cmd.hasOption("loglevel")) {
                String logLevelName = cmd.getOptionValue("loglevel");
                if("DEBUG".equals(logLevelName.toUpperCase())) {
                    logLevel = Level.DEBUG;
                } else if("INFO".equals(logLevelName.toUpperCase())) {
                    logLevel = Level.INFO;
                } else if("WARN".equals(logLevelName.toUpperCase())) {
                    logLevel = Level.WARN;
                } else if("ERROR".equals(logLevelName.toUpperCase())) {
                    logLevel = Level.ERROR;
                } else {
                    log.error("unsupported log level: {}",logLevelName);
                }
            }

            if(logLevel != null) {
                for(String logname : new String [] {"at","org","net","com"}) {

                    ch.qos.logback.classic.Logger logger =
                            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(logname);
                    logger.setLevel(logLevel);
                }
            }


            File tmpDir = null;
            LDCacheBackend backend = new LDCacheBackend();

            Resource context = null;
            if(cmd.hasOption("context")) {
                context = backend.createURI(cmd.getOptionValue("context"));
            }

            if(backend != null && context != null) {
                LDPath<Value> ldpath = new LDPath<Value>(backend);

                if(cmd.hasOption("path")) {
                    String path = cmd.getOptionValue("path");

                    for(Value v : ldpath.pathQuery(context,path,null)) {
                        System.out.println(v.stringValue());
                    }
                } else if(cmd.hasOption("program")) {
                    File file = new File(cmd.getOptionValue("program"));

                    Map<String,Collection<?>> result = ldpath.programQuery(context,new FileReader(file));

                    if (cmd.hasOption("format")) {
                        final String format = cmd.getOptionValue("format");
                        if (format.equals("json")) {
                            // Jackson.jr closes the output stream.
                            final ProxyOutputStream proxyOutputStream = new ProxyOutputStream(System.out) {
                                @Override
                                public void close() throws IOException {
                                     flush();
                                }
                            };
                            JSON.std.write(result, proxyOutputStream);
                            System.out.println("");
                        } else {
                            System.err.println("Unknown format: " + format);
                        System.exit(1);
                        }
                    } else {
                        for (String field : result.keySet()) {
                            StringBuilder line = new StringBuilder();
                            line.append(field);
                            line.append(" = ");
                            line.append("{");
                            for (Iterator<?> it = result.get(field).iterator(); it.hasNext(); ) {
                                line.append(it.next().toString());
                                if (it.hasNext()) {
                                    line.append(", ");
                                }
                            }
                            line.append("}");
                            System.out.println(line);

                        }
                    }
                }
            }


            if(tmpDir != null) {
                FileUtils.deleteDirectory(tmpDir);
            }


        } catch (ParseException e) {
            System.err.println("invalid arguments");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "LDQuery", options, true );
        } catch (LDPathParseException e) {
            System.err.println("path or program could not be parsed");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println("file or program could not be found");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("LDQuery", options, true);
        } catch (IOException e) {
            System.err.println("could not access cache data directory");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("LDQuery", options, true);
        }


    }

    @SuppressWarnings("static-access")
    private static Options buildOptions() {
        Options result = new Options();

        OptionGroup query = new OptionGroup();
        Option path = OptionBuilder.withArgName("path").hasArg().withDescription("LD Path to evaluate on the file starting from the context").create("path");
        Option program = OptionBuilder.withArgName("file").hasArg().withDescription("LD Path program to evaluate on the file starting from the context").create("program");
        query.addOption(path);
        query.addOption(program);
        query.setRequired(true);
        result.addOptionGroup(query);

        Option context = OptionBuilder.withArgName("uri").hasArg().withDescription("URI of the context node to start from").create("context");
        context.setRequired(true);
        result.addOption(context);

        Option loglevel = OptionBuilder.withArgName("level").hasArg().withDescription("set the log level; default is 'warn'").create("loglevel");
        result.addOption(loglevel);

        Option store = OptionBuilder.withArgName("dir").hasArg().withDescription("cache the retrieved data in this directory").create("store");
        result.addOption(store);

        Option format = OptionBuilder.withArgName("format").hasArg().withDescription("output format").create("format");
        result.addOption(format);

        return result;
    }

}
