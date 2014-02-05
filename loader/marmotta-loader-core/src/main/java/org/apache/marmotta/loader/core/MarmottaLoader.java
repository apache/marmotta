package org.apache.marmotta.loader.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.cli.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.loader.api.LoaderBackend;
import org.apache.marmotta.loader.api.LoaderHandler;
import org.apache.marmotta.loader.api.LoaderOptions;
import org.apache.marmotta.loader.context.ContextHandler;
import org.apache.marmotta.loader.functions.BackendIdentifierFunction;
import org.apache.marmotta.loader.rio.GeonamesFormat;
import org.apache.marmotta.loader.statistics.StatisticsHandler;
import org.apache.marmotta.loader.util.DirectoryFilter;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MarmottaLoader {

    private static ServiceLoader<LoaderBackend> backends = ServiceLoader.load(LoaderBackend.class);


    private static Logger log = LoggerFactory.getLogger(MarmottaLoader.class);

    static {
        RDFFormat.register(GeonamesFormat.FORMAT);
    }


    private Configuration configuration;

    public MarmottaLoader(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Load data according to the specification given in the configuration from all directories and files specified
     * in the configuration. Returns the handler used for importing (in case further operations should be performed).
     */
    public LoaderHandler load() throws RDFHandlerException {
        LoaderHandler handler = getLoader();

        if(configuration.containsKey(LoaderOptions.CONTEXT)) {
            handler = new ContextHandler(handler, new URIImpl(configuration.getString(LoaderOptions.CONTEXT)));
        }

        if(configuration.containsKey(LoaderOptions.STATISTICS_ENABLED)) {
            handler = new StatisticsHandler(handler, configuration);
        }

        handler.initialise();

        if(configuration.containsKey(LoaderOptions.DIRS)) {
            for(String dirname : configuration.getStringArray(LoaderOptions.DIRS)) {
                File dir = new File(dirname);

                try {
                    loadDirectory(dir, handler, getRDFFormat(configuration.getString(LoaderOptions.FORMAT)), configuration.getString(LoaderOptions.COMPRESSION));
                } catch (RDFParseException | IOException e) {
                    log.warn("error importing directory {}: {}", dir, e.getMessage());
                }
            }
        }

        if(configuration.containsKey(LoaderOptions.ARCHIVES)) {
            for(String archiveName : configuration.getStringArray(LoaderOptions.ARCHIVES)) {
                File archive = new File(archiveName);

                try {
                    loadArchive(archive, handler, getRDFFormat(configuration.getString(LoaderOptions.FORMAT)));
                } catch (RDFParseException | IOException | ArchiveException e) {
                    log.warn("error importing directory {}: {}", archive, e.getMessage());
                }
            }
        }


        if(configuration.containsKey(LoaderOptions.FILES)) {
            for(String fname : configuration.getStringArray(LoaderOptions.FILES)) {
                File f = new File(fname);

                try {
                    loadFile(f, handler, getRDFFormat(configuration.getString(LoaderOptions.FORMAT)), configuration.getString(LoaderOptions.COMPRESSION));
                } catch (RDFParseException | IOException e) {
                    log.warn("error importing file {}: {}", f, e.getMessage());
                }
            }
        }

        handler.shutdown();

        return handler;
    }

    /**
     * Load data from the inputstream given as first argument into the handler given as second argument.
     *
     * @param inStream input byte stream to read the data from; must be plain data in the format given as argument
     * @param handler  handler to add the data to
     * @param format   format to use for creating the parser
     * @throws RDFParseException
     * @throws IOException
     */
    public void load(InputStream inStream, LoaderHandler handler, RDFFormat format) throws RDFParseException, IOException {
        try {

            RDFParser parser = createParser(format);
            parser.setRDFHandler(handler);
            parser.parse(inStream,configuration.getString(LoaderOptions.BASE_URI, "http://localhost/"));

        } catch (RDFHandlerException e) {
            log.error("error loading stream data in format {}: {}", format, e.getMessage());
        }
    }

    /**
     * Load data from the reader given as first argument into the handler given as second argument.
     *
     * @param reader   character stream to read the data from; must be plain data in the format given as argument
     * @param handler  handler to add the data to
     * @param format   format to use for creating the parser
     * @throws RDFParseException
     * @throws IOException
     */
    public void load(Reader reader, LoaderHandler handler, RDFFormat format) throws RDFParseException, IOException {
        try {

            RDFParser parser = createParser(format);
            parser.setRDFHandler(handler);
            parser.parse(reader,configuration.getString(LoaderOptions.BASE_URI, "http://localhost/"));

        } catch (RDFHandlerException e) {
            log.error("error loading stream data in format {}: {}", format, e.getMessage());
        }
    }




    /**
     * Load data from the reader given as first argument into the handler given as second argument.
     *
     * @param file     file to read the data from; in case a compression format is not explicitly given, the method will
     *                 try to decide from the file name if the file is in a compressed format
     * @param handler  handler to add the data to
     * @param format   format to use for creating the parser or null for auto-detection
     * @param compression compression format to use, or null for auto-detection (see formats in org.apache.commons.compress.compressors.CompressorStreamFactory)
     * @throws RDFParseException
     * @throws IOException
     */
    public void loadFile(File file, LoaderHandler handler, RDFFormat format, String compression) throws RDFParseException, IOException {
        log.info("loading file {} ...", file);

        CompressorStreamFactory cf = new CompressorStreamFactory();
        cf.setDecompressConcatenated(true);

        // detect the file compression
        String detectedCompression = detectCompression(file);
        if(compression == null) {
            if(detectedCompression != null) {
                log.info("using auto-detected compression ({})", detectedCompression);
                compression = detectedCompression;
            }
        } else {
            if(detectedCompression != null && !compression.equals(detectedCompression)) {
                log.warn("user-specified compression ({}) overrides auto-detected compression ({})", compression, detectedCompression);
            } else {
                log.info("using user-specified compression ({})", compression);
            }
        }


        // detect the file format
        RDFFormat detectedFormat = RDFFormat.forFileName(uncompressedName(file));
        if(format == null) {
            if(detectedFormat != null) {
                log.info("using auto-detected format ({})", detectedFormat.getName());
                format = detectedFormat;
            } else {
                throw new RDFParseException("could not detect input format of file "+ file);
            }
        } else {
            if(detectedFormat != null && !format.equals(detectedFormat)) {
                log.warn("user-specified format ({}) overrides auto-detected format ({})", format.getName(), detectedFormat.getName());
            }
        }

        // create input stream from file and wrap in compressor stream
        InputStream in;
        InputStream fin = new BufferedInputStream(new FileInputStream(file));
        try {
            if(compression != null) {
                if (CompressorStreamFactory.GZIP.equalsIgnoreCase(compression)) {
                    in = new GzipCompressorInputStream(fin,true);
                } else if (CompressorStreamFactory.BZIP2.equalsIgnoreCase(compression)) {
                    in = new BZip2CompressorInputStream(fin, true);
                } else {
                    // does not honour decompressConcatenated
                    in = cf.createCompressorInputStream(compression, fin);
                }
            } else {
                in = cf.createCompressorInputStream(fin);
            }
        } catch (CompressorException ex) {
            log.info("no compression detected, using plain input stream");
            in = fin;
        }

        // load using the input stream
        load(in, handler, format);
    }


    /**
     * Load data from the reader given as first argument into the handler given as second argument.
     *
     * @param directory directory to read the data from; in case a compression format is not explicitly given, the method will
     *                 try to decide from the file name if the file is in a compressed format
     * @param handler  handler to add the data to
     * @param format   format to use for creating the parser or null for auto-detection
     * @param compression compression format to use, or null for auto-detection (see formats in org.apache.commons.compress.compressors.CompressorStreamFactory)
     * @throws RDFParseException
     * @throws IOException
     */
    public void loadDirectory(File directory, LoaderHandler handler, RDFFormat format, String compression) throws RDFParseException, IOException {
        log.info("loading files in directory {} ...", directory);
        if(directory.exists() && directory.isDirectory()) {
            for(File f : directory.listFiles(new DirectoryFilter())) {
                try {
                    loadFile(f, handler,format,compression);
                } catch (RDFParseException | IOException e) {
                    log.warn("error importing file {}: {}", f, e.getMessage());
                }
            }
        } else {
            throw new RDFParseException("could not load files from directory "+directory+": it does not exist or is not a directory");
        }
    }


    public void loadArchive(File archive, LoaderHandler handler, RDFFormat format) throws RDFParseException, IOException, ArchiveException {
        log.info("loading files in archive {} ...", archive);

        if(archive.exists() && archive.canRead()) {

            if(archive.getName().endsWith("7z")) {
                log.info("auto-detected archive format: 7Z");

                final SevenZFile sevenZFile = new SevenZFile(archive);

                try {
                    SevenZArchiveEntry entry;
                    while( (entry = sevenZFile.getNextEntry()) != null) {

                        if(! entry.isDirectory()) {
                            log.info("loading entry {} ...", entry.getName());

                            // detect the file format
                            RDFFormat detectedFormat = RDFFormat.forFileName(entry.getName());
                            if(format == null) {
                                if(detectedFormat != null) {
                                    log.info("auto-detected entry format: {}", detectedFormat.getName());
                                    format = detectedFormat;
                                } else {
                                    throw new RDFParseException("could not detect input format of entry "+ entry.getName());
                                }
                            } else {
                                if(detectedFormat != null && !format.equals(detectedFormat)) {
                                    log.warn("user-specified entry format ({}) overrides auto-detected format ({})", format.getName(), detectedFormat.getName());
                                } else {
                                    log.info("user-specified entry format: {}", format.getName());
                                }
                            }


                            load(new InputStream() {
                                @Override
                                public int read() throws IOException {
                                    return sevenZFile.read();
                                }

                                @Override
                                public int read(byte[] b) throws IOException {
                                    return sevenZFile.read(b);
                                }

                                @Override
                                public int read(byte[] b, int off, int len) throws IOException {
                                    return sevenZFile.read(b, off, len);
                                }
                            },handler,format);
                        }
                    }
                } finally {
                    sevenZFile.close();
                }

            } else {
                InputStream in;

                String archiveCompression = detectCompression(archive);
                InputStream fin = new BufferedInputStream(new FileInputStream(archive));
                if(archiveCompression != null) {
                    if (CompressorStreamFactory.GZIP.equalsIgnoreCase(archiveCompression)) {
                        log.info("auto-detected archive compression: GZIP");
                        in = new GzipCompressorInputStream(fin,true);
                    } else if (CompressorStreamFactory.BZIP2.equalsIgnoreCase(archiveCompression)) {
                        log.info("auto-detected archive compression: BZIP2");
                        in = new BZip2CompressorInputStream(fin, true);
                    } else {
                        in = fin;
                    }
                } else {
                    in = fin;
                }

                ArchiveInputStream zipStream = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(in));
                logArchiveType(zipStream);

                ArchiveEntry entry;
                while( (entry = zipStream.getNextEntry()) != null) {

                    if(! entry.isDirectory()) {
                        log.info("loading entry {} ...", entry.getName());

                        // detect the file format
                        RDFFormat detectedFormat = RDFFormat.forFileName(entry.getName());
                        if(format == null) {
                            if(detectedFormat != null) {
                                log.info("auto-detected entry format: {}", detectedFormat.getName());
                                format = detectedFormat;
                            } else {
                                throw new RDFParseException("could not detect input format of entry "+ entry.getName());
                            }
                        } else {
                            if(detectedFormat != null && !format.equals(detectedFormat)) {
                                log.warn("user-specified entry format ({}) overrides auto-detected format ({})", format.getName(), detectedFormat.getName());
                            } else {
                                log.info("user-specified entry format: {}", format.getName());
                            }
                        }


                        load(zipStream,handler,format);
                    }
                }
            }

        } else {
            throw new RDFParseException("could not load files from archive "+archive+": it does not exist or is not readable");
        }

    }


    private void logArchiveType(ArchiveInputStream stream) {
        if(log.isInfoEnabled()) {
            if(stream instanceof ZipArchiveInputStream) {
                log.info("auto-detected archive format: ZIP");
            } else if (stream instanceof TarArchiveInputStream) {
                log.info("auto-detected archive format: TAR");
            } else if (stream instanceof CpioArchiveInputStream) {
                log.info("auto-detected archive format: CPIO");
            } else if (stream instanceof CpioArchiveInputStream) {
                log.info("auto-detected archive format: CPIO");
            } else {
                log.info("unknown archive format, relying on commons-compress");
            }
        }
    }

    /**
     * Detect the compression format from the filename, or null in case auto-detection failed.
     * @param file
     * @return
     */
    private String detectCompression(File file) {
        if(BZip2Utils.isCompressedFilename(file.getName())) {
            return CompressorStreamFactory.BZIP2;
        } else if(GzipUtils.isCompressedFilename(file.getName())) {
            return CompressorStreamFactory.GZIP;
        } else {
            return null;
        }
    }

    private String uncompressedName(File file) {
        if(BZip2Utils.isCompressedFilename(file.getAbsolutePath())) {
            return BZip2Utils.getUncompressedFilename(file.getName());
        } else if(GzipUtils.isCompressedFilename(file.getAbsolutePath())) {
            return GzipUtils.getUncompressedFilename(file.getName());
        } else {
            return file.getName();
        }
    }

    /**
     * Create a parser for the given format, turning off some of the stricter configuration settings so we
     * can handle more messy data without errors.
     * @param format
     * @return
     */
    private RDFParser createParser(RDFFormat format) {
        RDFParser parser = Rio.createParser(format);
        parser.getParserConfig().addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
        parser.getParserConfig().addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
        parser.getParserConfig().addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);

        return parser;
    }


    /**
     * Determine loader from configuration options.
     * @return
     */
    private LoaderHandler getLoader() {
        if(configuration.containsKey(LoaderOptions.BACKEND)) {
            for(LoaderBackend backend : backends) {
                if(StringUtils.equalsIgnoreCase(backend.getIdentifier(), configuration.getString(LoaderOptions.BACKEND))) {
                    return backend.createLoader(configuration);
                }
            }
        }

        List<LoaderBackend> backendSet = Lists.newArrayList(backends);
        if(backendSet.size() == 1) {
            return backendSet.get(0).createLoader(configuration);
        }

        throw new IllegalStateException("there are multiple backends available, please select one explicitly using -B");

    }

    /**
     * Get the RDF format from a user specfication (either mime type or short cut)
     * @param spec
     * @return
     */
    private static RDFFormat getRDFFormat(String spec) {
        if(StringUtils.equalsIgnoreCase(spec,"turtle")) {
            return RDFFormat.TURTLE;
        } else if(StringUtils.equalsIgnoreCase(spec,"n3")) {
            return RDFFormat.N3;
        } else if(StringUtils.equalsIgnoreCase(spec,"rdf")) {
            return RDFFormat.RDFXML;
        } else if(StringUtils.equalsIgnoreCase(spec,"xml")) {
            return RDFFormat.RDFXML;
        } else if(StringUtils.equalsIgnoreCase(spec,"geonames")) {
            return GeonamesFormat.FORMAT;
        } else if(spec != null) {
            return RDFFormat.forMIMEType(spec);
        } else {
            return null;
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(MarmottaLoader.class.getSimpleName(), options, true);
    }

    /**
     * Build command line options. Base options are:
     * <ul>
     *     <li>-h | --help: show help</li>
     *     <li>-B | --backend: backend to use (kiwi, hbase, berkeleydb)</li>
     *     <li>-z | --gzip: input is GZIP encoded</li>
     *     <li>-j | --bzip2: input is BZIP2 encoded</li>
     *     <li>-c | --context: URI of a context to add the statement to</li>
     *     <li>-b | --base: URI to use as base for resolving relative URIs</li>
     *     <li>-f | --file: input file to use for loading</li>
     *     <li>-d | --dir:  input directory containing files to use for loading</li>
     *     <li>-t | --type: input format to use for parsing (MIME type)</li>
     *     <li>-s | --statistics: collect statistics and write a graph into the file given</li>
     * </ul>
     *
     * In addition, loader backends can provide their own additional command line options.
     *
     * @return
     */
    private static Options buildOptions() {
        final Options options = new Options();

        options.addOption("h", "help", false, "print this help");

        OptionGroup compression = new OptionGroup();
        compression.addOption(new Option("z", "gzip", false, "input is gzip compressed"));
        compression.addOption(new Option("j", "bzip2", false, "input is bzip2 compressed"));
        options.addOptionGroup(compression);

        final Option backend =
                OptionBuilder.withArgName("backend")
                        .hasArgs(1)
                        .withDescription("backend to use (" + StringUtils.join(Iterators.transform(backends.iterator(), new BackendIdentifierFunction()), ", ") + ")")
                        .withLongOpt("backend")
                        .create('B');
        options.addOption(backend);

        final Option base =
                OptionBuilder.withArgName("base")
                        .hasArgs(1)
                        .withDescription("base URI to use for resolving relative URIs")
                        .withLongOpt("base")
                        .create('b');
        options.addOption(base);

        final Option context =
                OptionBuilder.withArgName("context")
                        .hasArgs(1)
                        .withDescription("URI of a context to add the statement to")
                        .withLongOpt("context")
                        .create('c');
        options.addOption(context);


        final Option format =
                OptionBuilder.withArgName("type")
                        .hasArgs(1)
                        .withDescription("input format to use for parsing (MIME type) in case auto-guessing does not work")
                        .withLongOpt("type")
                        .create('t');
        options.addOption(format);

        // input options
        OptionGroup input = new OptionGroup();
        input.setRequired(true);
        final Option file =
                OptionBuilder.withArgName("file")
                        .hasArgs(Option.UNLIMITED_VALUES)
                        .withDescription("input file(s) to load")
                        .withLongOpt("file")
                        .create('f');
        input.addOption(file);

        final Option directories =
                OptionBuilder.withArgName("dir")
                        .hasArgs(Option.UNLIMITED_VALUES)
                        .withDescription("input directories(s) to load")
                        .withLongOpt("dir")
                        .create('d');
        input.addOption(directories);

        final Option archives =
                OptionBuilder.withArgName("archive")
                        .hasArgs(Option.UNLIMITED_VALUES)
                        .withDescription("input archives(s) to load (zip, tar.gz)")
                        .withLongOpt("archive")
                        .create('a');
        input.addOption(archives);
        options.addOptionGroup(input);


        final Option statistics =
                OptionBuilder.withArgName("statistics")
                        .withDescription("collect statistics and write a graph into the file given")
                        .withLongOpt("statistics")
                        .hasOptionalArg()
                        .create('s');
        options.addOption(statistics);

        final Option property  =
                OptionBuilder.withArgName("property=value")
                        .hasArgs(2)
                        .withValueSeparator()
                        .withDescription("set configuration property to value")
                        .create("D");
        options.addOption(property);

        for(LoaderBackend b : backends) {
            for(Option o : b.getOptions()) {
                options.addOption(o);
            }
        }

        return options;
    }


    public static Configuration parseOptions(String[] args) throws ParseException {
        Options options = buildOptions();

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        Configuration result = new MapConfiguration(new HashMap<String,Object>());

        if(cmd.hasOption('B')) {
            // check backends
            Set<String> existing = Sets.newHashSet(Iterators.transform (backends.iterator(), new BackendIdentifierFunction()));
            if(!existing.contains(cmd.getOptionValue('B'))) {
                throw new ParseException("the backend " + cmd.getOptionValue('B') + " does not exist");
            }

            result.setProperty(LoaderOptions.BACKEND, cmd.getOptionValue('B'));
        }

        if(cmd.hasOption('b')) {
            result.setProperty(LoaderOptions.BASE_URI, cmd.getOptionValue('b'));
        }

        if(cmd.hasOption('z')) {
            result.setProperty(LoaderOptions.COMPRESSION, CompressorStreamFactory.GZIP);
        }

        if(cmd.hasOption('j')) {
            result.setProperty(LoaderOptions.COMPRESSION, CompressorStreamFactory.BZIP2);
        }

        if(cmd.hasOption('c')) {
            result.setProperty(LoaderOptions.CONTEXT, cmd.getOptionValue('c'));
        }

        if(cmd.hasOption('t')) {
            RDFFormat fmt = getRDFFormat(cmd.getOptionValue('t'));
            if(fmt == null) {
                throw new ParseException("unrecognized MIME type: " + cmd.getOptionValue('t'));
            }

            result.setProperty(LoaderOptions.FORMAT, fmt.getDefaultMIMEType());
        }

        if(cmd.hasOption('f')) {
            result.setProperty(LoaderOptions.FILES, Arrays.asList(cmd.getOptionValues('f')));
        }

        if(cmd.hasOption('d')) {
            result.setProperty(LoaderOptions.DIRS, Arrays.asList(cmd.getOptionValues('d')));
        }

        if(cmd.hasOption('a')) {
            result.setProperty(LoaderOptions.ARCHIVES, Arrays.asList(cmd.getOptionValues('a')));
        }


        if(cmd.hasOption('s')) {
            result.setProperty(LoaderOptions.STATISTICS_ENABLED, true);
            result.setProperty(LoaderOptions.STATISTICS_GRAPH,   cmd.getOptionValue('s'));
        }

        if(cmd.hasOption('D')) {
            for(Map.Entry e : cmd.getOptionProperties("D").entrySet()) {
                result.setProperty(e.getKey().toString(), e.getValue());
            }
        }

        for(LoaderBackend b : backends) {
            for(Option option : b.getOptions()) {
                if(cmd.hasOption(option.getOpt())) {
                    String key = String.format("backend.%s.%s", b.getIdentifier(), option.getLongOpt() != null ? option.getLongOpt() : option.getOpt());
                    if(option.hasArg()) {
                        if(option.hasArgs()) {
                            result.setProperty(key, Arrays.asList(cmd.getOptionValues(option.getOpt())));
                        } else {
                            result.setProperty(key, cmd.getOptionValue(option.getOpt()));
                        }
                    } else {
                        result.setProperty(key, true);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Parse command line arguments and start import
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            Configuration cfg = parseOptions(args);

            MarmottaLoader loader = new MarmottaLoader(cfg);
            loader.load();

        } catch (Exception e) {
            System.err.println("error parsing command line options: "+e.getMessage());
            log.warn("Exception Details:",e);
            printHelp(buildOptions());
            System.exit(1);
        }

    }
}
