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
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.transactions.api.TransactionalSail;
import org.apache.marmotta.kiwi.transactions.sail.KiWiTransactionalSail;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
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
            
            String baseUri = endWith("http://localhost/", "/"),
                   context = baseUri+"context/default", 
                   format = null;
            boolean gzip = false;
            
            String dbCon = null, dbUser = null, dbPasswd = null;
            KiWiDialect dialect = null;
            
            if (cmd.hasOption('c')) {
                Configuration conf = new PropertiesConfiguration(cmd.getOptionValue('c'));
                baseUri = endWith(conf.getString("kiwi.context", baseUri), "/");
                context = conf.getString("kiwi.context", baseUri) + "context/default";
                
                dbCon = conf.getString("database.url");
                dbUser = conf.getString("database.user");
                dbPasswd = conf.getString("database.password");
                
                String dbType = conf.getString("database.type");
                if ("h2".equalsIgnoreCase(dbType)) {
                    dialect = new H2Dialect();
                } else if ("mysql".equalsIgnoreCase(dbType)) {
                    dialect = new MySQLDialect();
                } else if ("postgres".equalsIgnoreCase(dbType)) {
                    dialect = new PostgreSQLDialect();
                }
            }
            
            if (cmd.hasOption('d')) {
                dbCon = cmd.getOptionValue('d');
            }
            if (cmd.hasOption('U')) {
                dbUser = cmd.getOptionValue('U');
            }
            
            dialect = getDialect(cmd, dbCon, dialect);
            dbPasswd = getPassword(cmd, dbCon, dbUser, dbPasswd);
            
            if (dbCon == null || dbUser == null || dbPasswd == null || dialect == null) {
                log.error("Cannot import without database connection!\n"
                        +"Please provide the database configuration either\n"
                        +"- in a kiwi-config file (-c), and/or\n"
                        +"- as commandline parameters (-d, -U, -P, -D");
                System.exit(3);
            }
            
            KiWiConfiguration kiwi = new KiWiConfiguration("kiwiLoader", dbCon, dbUser, dbPasswd, dialect);
            
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
            
            log.info("Initializing KiWiLoader for {}; user: {}, password: {}", dbCon, dbUser, String.format("%"+dbPasswd.length()+"s", "*"));
            KiWiLoader loader = new KiWiLoader(kiwi, baseUri, context);
            loader.setVersioningEnabled(cmd.hasOption("versioning"));
            loader.setReasoningEnabled(cmd.hasOption("reasoning"));
            loader.initialize();
            
            log.info("Starting import");
            for (String inFile: inputFiles) {
                log.info("Importing {}", inFile);
                long start = System.currentTimeMillis();
                loader.load(inFile, format, gzip);
                long dur = System.currentTimeMillis() - start;
                log.info("Import completed ({}): {}", inFile, dur);
            }
            
            log.info("Import complete, shutting down.");
            loader.shutdown();
            log.info("Exiting");
        } catch (ParseException e) {
            log.error(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(KiWiLoader.class.getSimpleName(), options, true);
            System.exit(1);
        } catch (ConfigurationException e) {
            log.error("Could not read system-config.properties: {}", e.getMessage());
            System.exit(1);
        } catch (RepositoryException e) {
            log.error("Sesame-Exception: {}", e.getMessage(), e);
            System.exit(2);
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
        try {
            final SailRepositoryConnection con = repository.getConnection();
            try {
                con.begin();
                // TODO Auto-generated method stub
                con.commit();
            } finally {
                con.close();
            }
        } catch (RepositoryException re) {
            log.error("RepositoryException: {}", re.getMessage());
        }
    }

    public synchronized void initialize() throws RepositoryException {
        if (repository != null && repository.isInitialized()) {
            throw new IllegalStateException("repository already initialized");
        }
        log.debug("initializing kiwi-store: {}", kiwi);
        KiWiStore store = new KiWiStore(kiwi);
        
        TransactionalSail tSail = new KiWiTransactionalSail(store);
        if (isVersioningEnabled) {
            log.debug("enabling versioning...");
            // TODO: Add Versioning
            // tSail = new KiWiVersioningSail(tSail);
            log.warn("versioning not yet supported/implemented");
        }

        if (isReasoningEnabled) {
            log.debug("enabling reasoner...");
            // TODO: Add Reasoning
            // tSail = new KiWiReasoningSail(tSail, null);
            log.warn("reasoning not yet supported/implemented");
        }
        
        repository = new SailRepository(tSail);
        repository.initialize();
    }

    private static String getPassword(CommandLine cmd, String jdbc, String user, String password) throws ParseException {
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
            return password;
        }
    }

    private static KiWiDialect getDialect(CommandLine cmd, String dbCon, KiWiDialect dialect)
            throws ParseException {
        // get the dialect
        final String dbDialect = cmd.getOptionValue('D');
        if (!cmd.hasOption('D')) {
            return dialect;
        } else if (dbDialect == null) {
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
        context.setOptionalArg(false);
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
        dbUrl.setArgName("jdbc-url");
        dbUrl.setArgs(1);
        options.addOption(dbUrl);
        
        final Option dbUser = new Option("U", "user", true, "database user");
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
