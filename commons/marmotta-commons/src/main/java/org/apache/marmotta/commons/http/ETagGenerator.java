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
package org.apache.marmotta.commons.http;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.nio.charset.Charset;

/**
 * HTTP ETag generator for Sesame
 * 
 * @author Sergio Fernández
 *
 */
public class ETagGenerator {
    
    public static String getETag(RepositoryConnection conn, String uri) throws RepositoryException {
        URI resource = ResourceUtils.getUriResource(conn, uri);
        return getETag(conn, resource);
    }
    
    public static String getETag(RepositoryConnection conn, URI resource) throws RepositoryException {
    	if (resource == null) return "";
    	
        Hasher hasher = buildHasher();
        hasher.putString(resource.stringValue(), Charset.defaultCharset());
        //FIXME: The order of the statements is not defined -> might result in different hash!
        RepositoryResult<Statement> outgoing = conn.getStatements(resource, null, null, true);
        try {
        	while (outgoing.hasNext()) {
        		Statement statement = outgoing.next();
        		hasher.putString(statement.getPredicate().stringValue(), Charset.defaultCharset());
            	hasher.putString(statement.getObject().stringValue(), Charset.defaultCharset());
            	//TODO: statement modification date?
        	}
        } finally {
        	outgoing.close();
        }
        RepositoryResult<Statement> incoming = conn.getStatements(null, null, resource, true);
        try {
        	while (incoming.hasNext()) {
        		Statement statement = incoming.next();
        		hasher.putString(statement.getSubject().stringValue(), Charset.defaultCharset());
        		hasher.putString(statement.getPredicate().stringValue(), Charset.defaultCharset());
        		//TODO: statement modification date?
        	}    
        } finally {
        	incoming.close();
        }
        return hasher.hash().toString();
    }
    
    public static String getWeakETag(RepositoryConnection conn, String resource) throws RepositoryException {
        if (resource.startsWith("http://")) {
        	return getWeakETag(conn, ResourceUtils.getUriResource(conn, resource));
        } else {
        	return getWeakETag(conn, ResourceUtils.getAnonResource(conn, resource));
        }
    }   
    
//    public static String getWeakETag(RepositoryConnection conn, Resource resource) throws RepositoryException {
//    	if (resource instanceof URI) {
//    		return getWeakETag(conn, (URI)resource);
//    	} else if (resource instanceof BNode) {
//    		return getWeakETag(conn, (BNode)resource);
//    	} else {
//    		return null;
//    	}
//    }
    
    public static String getWeakETag(RepositoryConnection conn, Resource resource) throws RepositoryException {
    	if (resource == null) return "";
    	
        Hasher hasher = buildHasher();
        hasher.putString(resource.stringValue(), Charset.defaultCharset());
        //FIXME: The order of the statements is not defined -> might result in different hash!
        RepositoryResult<Statement> statements = conn.getStatements(resource, null, null, true);
        try {
        	while (statements.hasNext()) {
        		Statement statement = statements.next();
        		hasher.putString(statement.getPredicate().stringValue(), Charset.defaultCharset());
        		hasher.putString(statement.getObject().stringValue(), Charset.defaultCharset());
        		//TODO: statement modification date?
        	}
        } finally {
        	statements.close();
        }
        return hasher.hash().toString();
    }

    private static Hasher buildHasher() {
        HashFunction function = Hashing.goodFastHash(16);
        Hasher hasher = function.newHasher();
        return hasher;
    } 

}
