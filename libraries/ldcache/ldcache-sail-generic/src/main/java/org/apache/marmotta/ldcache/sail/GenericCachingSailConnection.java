package org.apache.marmotta.ldcache.sail;

import info.aduna.iteration.CloseableIteration;

import org.apache.marmotta.commons.sesame.filter.AlwaysTrueFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.ldcache.services.LDCache;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericCachingSailConnection extends NotifyingSailConnectionWrapper {

	private static Logger log = LoggerFactory.getLogger(GenericCachingSailConnection.class);
	
	private final LDCache ldcache;
	private final SesameFilter<Resource> acceptForCaching;

	public GenericCachingSailConnection(NotifyingSailConnection connection, LDCache ldcache) {
		this(connection, ldcache, new AlwaysTrueFilter<Resource>());
	}

	
	public GenericCachingSailConnection(NotifyingSailConnection connection, LDCache ldcache, SesameFilter<Resource> acceptForCaching) {
		super(connection);
		this.ldcache = ldcache;
		this.acceptForCaching = acceptForCaching;
	}
	
	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(
			Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts) throws SailException {
		
		if (accept(subj)) {
			log.warn("Refreshing resource: {}", subj.stringValue());
			ldcache.refreshResource((URI) subj, false);
		}

		return super.getStatements(subj, pred, obj, includeInferred, contexts);
	}


	private boolean accept(Resource subj) {
		return subj != null && ResourceUtils.isURI(subj) && acceptForCaching.accept(subj);
	}

}
