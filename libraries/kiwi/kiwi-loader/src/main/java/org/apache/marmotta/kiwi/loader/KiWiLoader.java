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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.sail.Sail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link KiWiLoader} is a fastpath importer into a kiwi database. It is meant
 * for importing large datafiles into a kiwi RDF store, avoiding the overhead of
 * the full marmotta platform.
 * <p>
 * The usage is <br>
 * {@code KiWiLoader 
 *        [-h] 
 *        [-c <config>] 
 *        [-b <baseUri>] 
 *        [-C <context>] 
 *        [-d <jdbc-url>] 
 *        [-D <dialect>] 
 *        [-U <dbUser>] 
 *        [-P [<passwd>]] 
 *        [-f <mime-type>] 
 *        [-z]
 *        [-i <rdf-file> ...] 
 *        [--reasoning] 
 *        [--versioning]
 *        <rdf-file> ...
 *        }
 * <pre>
 * General Settings:
 *  -h,--help                  print this help
 *
 * Base Settings:
 *  -c,--config &lt;config&gt;       Marmotta system-config.properties file
 *  -b,--baseUri &lt;baseUri&gt;     baseUri during the import (fallback:
 *                             kiwi.context from the config file)
 *  -C,--context &lt;context&gt;     context to import into
 *
 * Database:
 *  -d,--database &lt;jdbc-url&gt;   jdbc connection string
 *  -D,--dbDialect &lt;dialect&gt;   database dialect (h2, mysql, postgres)
 *  -U,--user &lt;dbUser&gt;         database user
 *  -P,--dbPasswd &lt;passwd&gt;     database password
 *  
 * Import:
 *  -f,--format &lt;mime-type&gt;    format of rdf file (if guessing based on the
 *                             extension does not work)
 *  -z                         Input file is gzip compressed
 *  -i,--file &lt;rdf-file&gt;       input file(s) to load
 *  
 * KiWi Extra Settings:
 *     --reasoning             enable reasoning
 *     --versioning            enable versioning
 * </pre>
 * 
 * @author Jakob Frank <jakob@apache.org>
 * 
 */
public class KiWiLoader {

    private static Logger log = LoggerFactory.getLogger(KiWiLoader.class);
    protected final KiWiConfiguration kiwi;
    protected String baseUri;
    protected String context;
    protected boolean isVersioningEnabled;
    protected boolean isReasoningEnabled;
    protected SailRepository repository;

    public KiWiLoader(KiWiConfiguration kiwi, String baseUri, String context) {
        this.kiwi = kiwi;
        this.baseUri = baseUri;
        this.context = context;

        isVersioningEnabled = false;
        isReasoningEnabled = false;
    }

    /**
     * @param args CLI params
     */
    public static void main(String[] args) {
        Options options = buildOptions();

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            
            if (cmd.hasOption("help")) {
                printHelp(options);
                return;
            }

            String baseUri = endWith("http://localhost/", "/"),
                    context = null, //baseUri+"context/default", 
                    format = null;
            boolean gzip = false;

            String dbCon = null, dbUser = null, dbPasswd = null;
            KiWiDialect dialect = null;

            if (cmd.hasOption('c')) {
                // load as much as possible from the config file
                Configuration conf = new PropertiesConfiguration(cmd.getOptionValue('c'));
                context = endWith(conf.getString("kiwi.context", baseUri), "/") + "context/default";
                baseUri = endWith(conf.getString("kiwi.context", baseUri), "/") + "resource/";

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

            // database url
            if (cmd.hasOption('d')) {
                dbCon = cmd.getOptionValue('d');
            }
            // database user
            if (cmd.hasOption('U')) {
                dbUser = cmd.getOptionValue('U');
            }

            // database dialect, use config value as fallback if present.
            dialect = getDialect(cmd, dbCon, dialect);
            // database passsword; if -P is given but no password, ask the user to enter it.
            dbPasswd = getPassword(cmd, dbCon, dbUser, dbPasswd);

            // We need a db-configuration, otherwise this does not make any sense.
            if (dbCon == null || dbUser == null || dbPasswd == null || dialect == null) {
                log.error("Cannot import without database connection!\n"
                        +"Please provide the database configuration either\n"
                        +"- in a kiwi-config file (-c), and/or\n"
                        +"- as commandline parameters (-d, -U, -P, -D)");
                System.exit(3);
            }

            KiWiConfiguration kiwi = new KiWiConfiguration("kiwiLoader", dbCon, dbUser, dbPasswd, dialect);

            // set the baseUri
            if (cmd.hasOption('b')) {
                baseUri = cmd.getOptionValue('b', baseUri);
            }

            // the context to import into.
            if (cmd.hasOption('C')) {
                context = cmd.getOptionValue('C', context);
            }

            // force uncompressing the files (will try to guess if this option is not set)
            gzip = cmd.hasOption('z');

            // the format to use as fallback; will try to guess based on the filename.
            format = cmd.getOptionValue('f');
            final RDFFormat fmt;
            if (format != null) {
                fmt = RDFFormat.forMIMEType(format);
                if (fmt == null) {
                    throw new ParseException(String.format("Invalid/Unknown RDF format: %s, use one of %s", format, RDFFormat.values()));
                }
                log.debug("Format-Hint: {}", fmt);
            } else {
                fmt = null;
            }

            // get the files to import.
            final ArrayList<String> inputFiles = new ArrayList<>();
            // - provided with the -i option
            if (cmd.hasOption('i')) {
                inputFiles.addAll(Arrays.asList(cmd.getOptionValues('i')));
            }
            // - or just the filename.
            inputFiles.addAll(Arrays.asList(cmd.getArgs()));

            // we need at least one file to import
            // this could be changed...
            if (inputFiles.isEmpty()) {
                throw new ParseException("No files to import");
            }

            // initialize the KiWiLoader
            log.info("Initializing KiWiLoader for {}; user: {}, password: {}", dbCon, dbUser, String.format("%"+dbPasswd.length()+"s", "*"));
            KiWiLoader loader = new KiWiLoader(kiwi, baseUri, context);
            loader.setVersioningEnabled(cmd.hasOption("versioning"));
            loader.setReasoningEnabled(cmd.hasOption("reasoning"));
            loader.initialize();

            log.info("Starting import");
            for (String inFile: inputFiles) {
                try {
                    log.info("Importing {}", inFile);
                    final File f = new File(inFile);
                    String fName = f.getName();
                    InputStream inStream = new FileInputStream(f);

                    if (gzip || inFile.endsWith(".gz")) {
                        log.debug("{} seems to be gzipped", inFile);
                        inStream = new GZIPInputStream(inStream);
                        fName = fName.replaceFirst("\\.gz$", "");
                    }

                    long start = System.currentTimeMillis();
                    try {
                        loader.load(inStream, RDFFormat.forFileName(fName, fmt));
                    } catch (final UnsupportedRDFormatException | RDFParseException e) {
                        // try again with the fixed format
                        if (fmt != null) {
                            inStream.close();
                            // Reopen new
                            if (inStream instanceof GZIPInputStream) {
                                inStream = new GZIPInputStream(new FileInputStream(f));
                            } else {
                                inStream = new FileInputStream(f);
                            }
                            loader.load(inStream, fmt);
                        } else {
                            throw e;
                        }
                    } finally {
                        inStream.close();
                    }
                    long dur = System.currentTimeMillis() - start;
                    log.info("Import completed ({}): {}ms", inFile, dur);
                } catch (FileNotFoundException e) {
                    log.error("Could not read file {}, skipping...", inFile);
                } catch (IOException e) {
                    log.error("Error while reading {}: {}", inFile, e);
                } catch (RDFParseException e) {
                    log.error("file {} contains errors: {}\n{}", inFile, e.getMessage(), e);
                } catch (UnsupportedRDFormatException e) {
                    log.error("{}, required for {} - dependency missing?", e.getMessage(), inFile);
                }
            }

            log.info("Import complete, shutting down.");
            loader.shutdown();
            log.info("Exiting");
        } catch (ParseException e) {
            log.error(e.getMessage());
            printHelp(options);
            System.exit(1);
        } catch (ConfigurationException e) {
            log.error("Could not read system-config.properties: {}", e.getMessage());
            System.exit(1);
        } catch (RepositoryException e) {
            log.error("Sesame-Exception: {}", e.getMessage(), e);
            System.exit(2);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(KiWiLoader.class.getSimpleName(), options, true);
    }

    /**
     * Load the triples from the input stream using the provided format.
     * @param inStream the {@link InputStream} to read from
     * @param forFileName the expected {@link RDFFormat}
     * @throws RDFParseException if the stream is invalid (e.g. does not fit with the syntax) 
     * @throws IOException
     */
    public void load(InputStream inStream, RDFFormat forFileName) throws RDFParseException, IOException {
        try {
            final SailRepositoryConnection con = repository.getConnection();
            try {
                con.begin();

                final Resource[] ctx;
                if (context != null) {
                    ctx = new Resource[] { con.getValueFactory().createURI(context) };
                } else {
                    ctx = new Resource[] {};
                }

                con.add(inStream, baseUri, forFileName, ctx);

                con.commit();
            } catch (final Throwable t) {
                con.rollback();
                throw t;
            } finally {
                con.close();
            }
        } catch (RepositoryException re) {
            log.error("RepositoryException: {}", re.getMessage());
        }
    }

    /**
     * En-/Disable reasoning during load (currently not implemented)
     * @param enabled
     */
    public void setReasoningEnabled(boolean enabled) {
        this.isReasoningEnabled = enabled;
    }

    /**
     * En-/Disable versioning during load (currently not implemented)
     * @param enabled
     */
    public void setVersioningEnabled(boolean enabled) {
        this.isVersioningEnabled = enabled;
    }

    /**
     * Shutdown the Repository, close all database connections.
     * @throws RepositoryException
     */
    public synchronized void shutdown() throws RepositoryException {
        if (repository == null || !repository.isInitialized()) {
            throw new IllegalStateException("repository was not initialized!");
        }
        repository.shutDown();
        repository = null;
    }

    /**
     * Load the triples from the file using the provided format.
     * @param file the file to read from
     * @param format the expected {@link RDFFormat}
     * @param gzip force using a {@link GZIPInputStream} (will try to guess based on the filename if false)
     * @throws RDFParseException if the stream is invalid (e.g. does not fit with the syntax)
     * @throws IOException
     * @see {@link #load(InputStream, RDFFormat)}
     */
    public void load(String file, RDFFormat format, boolean gzip) throws IOException, RDFParseException {
        final File f = new File(file);
        InputStream is = new FileInputStream(f);
        if (gzip || f.getName().endsWith(".gz")) {
            is = new GZIPInputStream(is);
        }
        load(is, format);
    }

    /**
     * Initialize the KiWiStore, connect to the database.
     * @throws RepositoryException
     */
    public synchronized void initialize() throws RepositoryException {
        if (repository != null && repository.isInitialized()) {
            throw new IllegalStateException("repository already initialized");
        }
        log.debug("initializing kiwi-store: {}", kiwi);
        KiWiStore store = new KiWiStore(kiwi);

        final Sail sail;
        if (isVersioningEnabled || isReasoningEnabled) {
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
            sail = tSail;
        } else {
            // no transactional sail required here
            sail = store;
        }

        repository = new SailRepository(sail);
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

    private static KiWiDialect getDialect(CommandLine cmd, String dbCon, KiWiDialect fallback)
            throws ParseException {
        // get the dialect
        final String dbDialect = cmd.getOptionValue('D');
        if (!cmd.hasOption('D')) {
            return fallback;
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

        options.addOption("h", "help", false, "print this help");
        
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
