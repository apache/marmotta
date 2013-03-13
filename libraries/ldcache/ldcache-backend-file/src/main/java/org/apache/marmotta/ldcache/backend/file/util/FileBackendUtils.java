package org.apache.marmotta.ldcache.backend.file.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.marmotta.commons.util.HashUtils;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

public class FileBackendUtils {

	private static final String DATA_EXT = ".ttl";
	private static final String META_EXT = ".meta";

	private FileBackendUtils() {
		// static access only
	}

	public static File getMetaFile(URI resource, File baseDir) {
		return getMetaFile(resource.stringValue(), baseDir);
	}

	public static File getMetaFile(String resourceURI, File baseDir) {
		File meta = getResourceFile(baseDir, resourceURI, META_EXT);

		return meta;
	}

	private static File getResourceFile(File baseDir, String resourceURI, String ext) {
		final int depth = 5, width = 3;
		final String fName = HashUtils.sha1(resourceURI);
		final StringBuilder path = new StringBuilder();
		for (int i = 0; i < depth * width; i += width) {
			path.append(fName.substring(i, i + width)).append(File.separator);
		}
		path.append(fName).append(ext);

		return new File(baseDir, path.toString());
	}
	
	public static List<File> listMetaFiles(final File baseDir) {
		return listFiles(baseDir, META_EXT);
	}
	
	public static List<File> listFiles(final File baseDir, final String ext) {
		List<File> list = new LinkedList<File>();
		listFiles(baseDir, ext, list);
		return list;		
	}
	
	private static void listFiles(final File baseDir, final String ext, List<File> listedFiles) {
		for (File f: baseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) return true;
				else return pathname.getName().endsWith(ext);
			}
		})) {
			if (f.isDirectory()) {
				listFiles(f, ext, listedFiles);
			} else {
				listedFiles.add(f);
			}
		}

	}
	
	public static CacheEntry readCacheEntry(File metaFile, ValueFactory valueFactory) throws IOException {
		BufferedReader br;
			br = new BufferedReader(new FileReader(metaFile));
			try {
				final CacheEntry ce = new CacheEntry();
				
				ce.setResource(valueFactory.createURI(br.readLine()));
				ce.setLastRetrieved(new Date(Long.parseLong(br.readLine().replaceFirst("#.*$", "").trim())));
				ce.setExpiryDate(new Date(Long.parseLong(br.readLine().replaceFirst("#.*$", "").trim())));
				ce.setUpdateCount(Integer.parseInt(br.readLine().replaceFirst("#.*$", "").trim()));
				
				return ce;
			} finally {
				br.close();
			}
	}
	
	public static void writeCacheEntry(CacheEntry ce, File baseDir) throws IOException {
		File metaFile = getMetaFile(ce.getResource(), baseDir);

        // ensure that the directory where we write the file exists
        metaFile.getParentFile().mkdirs();
		try {
			PrintStream ps = new PrintStream(metaFile);
			try {
				ps.println(ce.getResource().stringValue());
				ps.printf("%tQ # last retrieved: %<tF %<tT.%<tL%n", ce.getLastRetrieved());
				ps.printf("%tQ # last retrieved: %<tF %<tT.%<tL%n", ce.getExpiryDate());
				ps.printf("%d # %<d updates%n", ce.getUpdateCount());
			} finally {
				ps.close();
			}
		} catch (FileNotFoundException e) {
			throw e;
		}
	}

	public static boolean isExpired(CacheEntry ce) {
		return ce.getExpiryDate().before(new Date());
	}

}
