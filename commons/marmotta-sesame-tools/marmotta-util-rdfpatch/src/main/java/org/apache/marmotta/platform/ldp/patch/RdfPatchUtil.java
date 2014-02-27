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
package org.apache.marmotta.platform.ldp.patch;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParser;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParserImpl;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * RdfPatchUtil - Util-Class to apply rdf-patches on a {@link Repository} or {@link org.openrdf.repository.RepositoryConnection}
 *
 * @author Jakob Frank
 */
public class RdfPatchUtil {

    /**
     * Apply the provided patch to the repository
     * @param repository the {@link org.openrdf.repository.Repository} to patch
     * @param patch the patch to apply
     * @param contexts restrict changes to these contexts (leave empty to apply to <em>all</em> contexts)
     * @throws ParseException if the patch could not be parsed
     * @throws InvalidPatchDocumentException if the patch is invalid
     */
    public static void applyPatch(Repository repository, String patch, Resource... contexts) throws RepositoryException, ParseException, InvalidPatchDocumentException {
        applyPatch(repository, getPatch(patch), contexts);
    }

    /**
     * Apply the provided patch to the repository
     * @param repository the {@link org.openrdf.repository.Repository} to patch
     * @param patchSource the patch to apply
     * @param contexts restrict changes to these contexts (leave empty to apply to <em>all</em> contexts)
     * @throws ParseException if the patch could not be parsed
     * @throws InvalidPatchDocumentException if the patch is invalid
     */
    public static void applyPatch(Repository repository, InputStream patchSource, Resource... contexts) throws RepositoryException, ParseException, InvalidPatchDocumentException {
        applyPatch(repository, getPatch(patchSource), contexts);
    }

    /**
     * Apply the provided patch to the repository
     * @param connection the {@link org.openrdf.repository.RepositoryConnection} to patch
     * @param patch the patch to apply
     * @param contexts restrict changes to these contexts (leave empty to apply to <em>all</em> contexts)
     * @throws ParseException if the patch could not be parsed
     * @throws InvalidPatchDocumentException if the patch is invalid
     */
    public static void applyPatch(RepositoryConnection connection, String patch, Resource... contexts) throws RepositoryException, ParseException, InvalidPatchDocumentException {
        applyPatch(connection, getPatch(patch), contexts);
    }

    /**
     * Apply the provided patch to the repository
     * @param connection the {@link org.openrdf.repository.RepositoryConnection} to patch
     * @param patchSource the patch to apply
     * @param contexts restrict changes to these contexts (leave empty to apply to <em>all</em> contexts)
     * @throws ParseException if the patch could not be parsed
     * @throws InvalidPatchDocumentException if the patch is invalid
     */
    public static void applyPatch(RepositoryConnection connection, InputStream patchSource, Resource... contexts) throws RepositoryException, ParseException, InvalidPatchDocumentException {
        applyPatch(connection, getPatch(patchSource), contexts);
    }

    /**
     * Apply the provided patch to the repository
     * @param repository the {@link org.openrdf.repository.Repository} to patch
     * @param patch the patch to apply
     * @param contexts restrict changes to these contexts (leave empty to apply to <em>all</em> contexts)
     * @throws InvalidPatchDocumentException if the patch is invalid
     */
    public static void applyPatch(Repository repository, List<PatchLine> patch, Resource... contexts) throws RepositoryException, InvalidPatchDocumentException {
        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();
            applyPatch(con, patch, contexts);
            con.commit();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
    }

    /**
     * Apply the provided patch to the repository
     * @param connection the {@link org.openrdf.repository.RepositoryConnection} to patch
     * @param patch the patch to apply
     * @param contexts restrict changes to these contexts (leave empty to apply to <em>all</em> contexts)
     * @throws InvalidPatchDocumentException if the patch is invalid
     */
    public static void applyPatch(RepositoryConnection connection, List<PatchLine> patch, Resource... contexts) throws RepositoryException, InvalidPatchDocumentException {
        Resource subject = null;
        URI predicate = null;
        Value object = null;

        for (PatchLine patchLine : patch) {
            final Statement statement = patchLine.getStatement();
            subject = statement.getSubject()!=null? statement.getSubject():subject;
            predicate = statement.getPredicate()!=null?statement.getPredicate():predicate;
            object = statement.getObject()!=null?statement.getObject():object;

            if (subject == null || predicate == null || object == null) {
                if (subject == null) {
                    throw new InvalidPatchDocumentException("Cannot resolve 'R' - subject was never set");
                }
                if (predicate == null) {
                    throw new InvalidPatchDocumentException("Cannot resolve 'R' - predicate was never set");
                }
                if (object == null) {
                    throw new InvalidPatchDocumentException("Cannot resolve 'R' - object was never set");
                }
            }

            switch (patchLine.getOperator()) {
                case ADD:
                    connection.add(subject, predicate, object, contexts);
                    break;
                case DELETE:
                    connection.remove(subject, predicate, object, contexts);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown patch operation: " + patchLine.getOperator());
            }
        }
    }

    private static List<PatchLine> getPatch(InputStream is) throws ParseException {
        RdfPatchParser parser = new RdfPatchParserImpl(is);
        return parser.parsePatch();
    }

    private static List<PatchLine> getPatch(String patch) throws ParseException {
        try (InputStream is = IOUtils.toInputStream(patch)) {
            return getPatch(is);
        } catch (IOException e) {
            // You can always close an InputStream on a String
            assert(false);
            return null;
        }
    }

    private RdfPatchUtil() {
        // static access only
    }
}
