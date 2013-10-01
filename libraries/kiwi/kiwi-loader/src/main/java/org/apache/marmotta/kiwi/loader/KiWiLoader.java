/*
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
package org.apache.marmotta.kiwi.loader;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.reasoner.sail.KiWiReasoningSail;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.transactions.api.TransactionalSail;
import org.apache.marmotta.kiwi.transactions.sail.KiWiTransactionalSail;
import org.apache.marmotta.kiwi.versioning.sail.KiWiVersioningSail;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jakob Frank <jakob@apache.org>
 * 
 */
public class KiWiLoader {

    private static Logger log = LoggerFactory.getLogger(KiWiLoader.class);
    private final KiWiConfiguration kiwi;
    private String baseUri;
    private String context;
    private boolean isVersioningEnabled;
    private boolean isReasoningEnabled;
    private SailRepository repository;
    
    public KiWiLoader(KiWiConfiguration kiwi, String baseUri, String context) {
        this.kiwi = kiwi;
        this.baseUri = baseUri;
        this.context = context;
        
        isVersioningEnabled = false;
        isReasoningEnabled = false;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Options options = buildOptions();

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            final String dbCon, dbUser, dbPasswd;
            dbCon = cmd.getOptionValue('d');
            dbUser = cmd.getOptionValue('U');
            
            final KiWiDialect dialect = getDialect(cmd, dbCon);
            dbPasswd = getPassword(cmd, dbCon, dbUser);
            
            KiWiConfiguration kiwi = new KiWiConfiguration("kiwiLoader", dbCon, dbUser, dbPasswd, dialect);
            
            
            String baseUri = endWith("http://localhost/", "/"),
                   context = baseUri+"context/default", 
                   format = null;
            boolean gzip = false;
            
            if (cmd.hasOption('c')) {
                Configuration conf = new PropertiesConfiguration(cmd.getOptionValue('c'));
                baseUri = endWith(conf.getString("kiwi.context", baseUri), "/");
                context = conf.getString("kiwi.context", baseUri) + "context/default";
            }
            
            if (cmd.hasOption('b')) {
                baseUri = endWith(cmd.getOptionValue('b', baseUri), "/");
                context = cmd.getOptionValue('b', baseUri) + "context/default";
            }
            
            if (cmd.hasOption('C')) {
                context = cmd.getOptionValue('C', context);
            }

            format = cmd.getOptionValue('f');
            gzip = cmd.hasOption('z');
            
            ArrayList<String> inputFiles = new ArrayList<>();
            if (cmd.hasOption('i')) {
                inputFiles.addAll(Arrays.asList(cmd.getOptionValues('i')));
            }
            inputFiles.addAll(Arrays.asList(cmd.getArgs()));
            
            if (inputFiles.isEmpty()) {
                throw new ParseException("No files to import");
            }
            
            KiWiLoader loader = new KiWiLoader(kiwi, baseUri, context);
            loader.setVersioningEnabled(cmd.hasOption("versioning"));
            loader.setReasoningEnabled(cmd.hasOption("reasoning"));
            loader.initialize();
            
            for (String inFile: inputFiles) {
                loader.load(inFile, format, gzip);
            }
            
            loader.shutdown();
        } catch (ParseException e) {
            log.error(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(KiWiLoader.class.getSimpleName(), options, true);
        } catch (ConfigurationException e) {
            log.error("Could not read system-config.properties: {}", e.getMessage());
        } catch (RepositoryException e) {
            log.error("Sesame-Exception: {}", e.getMessage(), e);
        }
    }

    public void setReasoningEnabled(boolean enabled) {
        this.isReasoningEnabled = enabled;
    }

    public void setVersioningEnabled(boolean enabled) {
        this.isVersioningEnabled = enabled;
    }

    public synchronized void shutdown() throws RepositoryException {
        if (repository == null || !repository.isInitialized()) {
            throw new IllegalStateException("repository was not initialized!");
        }
        repository.shutDown();
        repository = null;
    }

    public void load(String file, String format, boolean gzip) {
        // TODO Auto-generated method stub
        
    }

    public synchronized void initialize() throws RepositoryException {
        if (repository != null && repository.isInitialized()) {
            throw new IllegalStateException("repository already initialized");
        }
        KiWiStore store = new KiWiStore((kiwi));
        
        TransactionalSail tSail = new KiWiTransactionalSail(store);
        if (isVersioningEnabled) {
            // TODO: Add Versioning
            // tSail = new KiWiVersioningSail(tSail);
            log.warn("versioning not yet supported/implemented");
        }

        if (isReasoningEnabled) {
            // TODO: Add Reasoning
            // tSail = new KiWiReasoningSail(tSail, null);
            log.warn("reasoning not yet supported/implemented");
        }
        
        repository = new SailRepository(tSail);
        repository.initialize();
    }

    private static String getPassword(CommandLine cmd, String jdbc, String user) throws ParseException {
        if (cmd.hasOption('P')) {
            if (cmd.getOptionValue('P') != null) {
                return cmd.getOptionValue('P');
            } else {
                Console console = System.console();
                if (console == null) {
                    throw new ParseException("Could not aquire console, please provide password as parameter");
                }

                char passwordArray[] = console.readPassword("Password for %s on %s: ", jdbc, user);
                return new String(passwordArray);
            }
        } else {
            return "";
        }
    }

    private static KiWiDialect getDialect(CommandLine cmd, final String dbCon)
            throws ParseException {
        // get the dialect
        final String dbDialect = cmd.getOptionValue('D');
        if (dbDialect == null) {
            log.info("No dialect provded, trying to guess based on dbUrl: {}", dbCon);
            // try guessing
            if (dbCon.startsWith("jdbc:h2:")) {
                return new H2Dialect();
            } else if (dbCon.startsWith("jdbc:mysql:")) {
                return new MySQLDialect();
            } else if (dbCon.startsWith("jdbc:postgresql:")) {
                return new PostgreSQLDialect();
            } else {
                throw new ParseException("could not guess dialect from url, use the -D option");
            }
        } else if ("h2".equalsIgnoreCase(dbDialect)) {
            return new H2Dialect();
        } else if ("mysql".equalsIgnoreCase(dbDialect)) {
            return new MySQLDialect();
        } else if ("postgresql".equalsIgnoreCase(dbDialect)) {
            return new PostgreSQLDialect();
        } else {
            throw new ParseException("Unknown dialect: " + dbDialect);
        }
    }

    private static Options buildOptions() {
        final Options options = new Options();

        final Option c = new Option("c", "config", true, "Marmotta system-config.properties file");
        c.setArgName("config");
        c.setArgs(1);
        c.setOptionalArg(false);
        options.addOption(c);

        final Option b = new Option("b", "baseUri", true, "baseUri during the import (fallback: kiwi.context from the config file)");
        b.setArgName("baseUri");
        b.setArgs(1);
        options.addOption(b);

        final Option context = new Option("C", "context", true, "context to import into");
        context.setArgName("context");
        context.setArgs(1);
        options.addOption(context);
        
        options.addOption("z", false, "Input file is gzip compressed");
       
        final Option format = new Option("f", "format", true, "format of rdf file (if guessing based on the extension does not work)");
        format.setArgName("mime-type");
        format.setArgs(1);
        options.addOption(format);
        
        final Option input = new Option("i", "file", true, "input file(s) to load");
        input.setArgName("rdf-file");
        input.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(input);

        final Option dbUrl = new Option("d", "database", true, "jdbc connection string");
        dbUrl.setRequired(true);
        dbUrl.setArgName("jdbc-url");
        dbUrl.setArgs(1);
        options.addOption(dbUrl);
        
        final Option dbUser = new Option("U", "user", true, "database user");
        dbUser.setRequired(true);
        dbUser.setArgName("dbUser");
        options.addOption(dbUser);
        
        final Option dbPasswd = new Option("P", "dbPasswd", true, "database password");
        dbPasswd.setArgName("passwd");
        dbPasswd.setArgs(1);
        dbPasswd.setOptionalArg(true);
        options.addOption(dbPasswd);
        
        final Option dbDialect = new Option("D", "dbDialect", true, "database dialect (h2, mysql, postgres)");
        dbDialect.setArgName("dialect");
        options.addOption(dbDialect);
        
        options.addOption(null, "reasoning", false, "enable reasoning");
        options.addOption(null, "versioning", false, "enable versioning");
        
        return options;
    }
    
    private static String endWith(String string, String suffix) {
        return string.replaceFirst("("+Pattern.quote(suffix)+")?$", suffix);
    }

}
