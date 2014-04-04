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
import org.apache.marmotta.platform.ldp.patch.model.WildcardStatement;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParser;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParserImpl;
import org.openrdf.model.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * RdfPatchUtil - Util-Class to apply and create rdf-patches on a {@link Repository} or {@link org.openrdf.repository.RepositoryConnection}
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
        applyPatch(repository, getPatch(repository.getValueFactory(), patch), contexts);
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
        applyPatch(repository, getPatch(repository.getValueFactory(), patchSource), contexts);
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
        applyPatch(connection, getPatch(connection.getValueFactory(), patch), contexts);
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
        applyPatch(connection, getPatch(connection.getValueFactory(), patchSource), contexts);
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

    private static List<PatchLine> getPatch(ValueFactory valueFactory, InputStream is) throws ParseException {
        RdfPatchParser parser = new RdfPatchParserImpl(is);
        parser.setValueFactory(valueFactory);
        return parser.parsePatch();
    }

    private static List<PatchLine> getPatch(ValueFactory valueFactory, String patch) throws ParseException {
        try (InputStream is = IOUtils.toInputStream(patch)) {
            return getPatch(valueFactory, is);
        } catch (IOException e) {
            // You can always close an InputStream on a String
            assert(false);
            return null;
        }
    }

    /**
     * Create an RDF-Patch that applies the changes from {@code r1} to {@code r2}.
     * @param r1 the 'from' Repository
     * @param r2 the 'to' Repository
     * @param optimize optimize the patch, i.e. remove duplicate or idempotent operations.
     * @param contexts restrict analysis to these contexts (leave empty to use <em>all</em> contexts)
     * @return List of PatchLines
     */
    public static List<PatchLine> diff(Repository r1, Repository r2, boolean optimize, Resource... contexts) throws RepositoryException {
        final RepositoryConnection c1 = r1.getConnection(), c2 = r2.getConnection();
        try {
            c1.begin();
            c2.begin();
            final List<PatchLine> diff = diff(c1, c2, optimize, contexts);
            c1.commit();
            c2.commit();
            return diff;
        } finally {
            c1.close();
            c2.close();
        }
    }

    /**
     * Create an RDF-Patch that applies the changes from {@code c1} to {@code c2}.
     * @param c1 the 'from' RepositoryConnection
     * @param c2 the 'to' RepositoryConnection
     * @param optimize optimize the patch, i.e. remove duplicate or idempotent operations.
     * @param contexts restrict analysis to these contexts (leave empty to use <em>all</em> contexts)
     * @return List of PatchLines
     */
    public static List<PatchLine> diff(RepositoryConnection c1, RepositoryConnection c2, boolean optimize, Resource... contexts) throws RepositoryException {
        Set<Statement> additions = new HashSet<>(),
                removals = new HashSet<>();

        final RepositoryResult<Statement> st1 = c1.getStatements(null, null, null, false, contexts);
        try {
            while (st1.hasNext()) {
                final Statement st = st1.next();
                if (!c2.hasStatement(st, false, contexts)) {
                    removals.add(st);
                }
            }
        } finally {
            st1.close();
        }

        final RepositoryResult<Statement> st2 = c2.getStatements(null, null, null, false, contexts);
        try {
            while (st2.hasNext()) {
                final Statement st = st2.next();
                if (!c1.hasStatement(st, false, contexts)) {
                    additions.add(st);
                }
            }
        } finally {
            st2.close();
        }

        if (optimize) {
            final TreeSet<Statement> delList = new TreeSet<>(new StatementComparator());
            delList.addAll(removals);
            final TreeSet<Statement> addList = new TreeSet<>(new StatementComparator());
            addList.addAll(additions);

            additions = addList;
            removals = delList;
        }

        Resource pS = null;
        URI pP = null;
        Value pO = null;
        ArrayList<PatchLine> patch = new ArrayList<>(removals.size()+additions.size());
        for (Statement s : removals) {
            final WildcardStatement ws = new WildcardStatement(
                    s.getSubject().equals(pS) ? null : s.getSubject(),
                    s.getPredicate().equals(pP) ? null : s.getPredicate(),
                    s.getObject().equals(pO) ? null : s.getObject()
            );
            patch.add(new PatchLine(PatchLine.Operator.DELETE, ws));
            pS = s.getSubject();
            pP = s.getPredicate();
            pO = s.getObject();
        }
        for (Statement s : additions) {
            final WildcardStatement ws = new WildcardStatement(
                    s.getSubject().equals(pS) ? null : s.getSubject(),
                    s.getPredicate().equals(pP) ? null : s.getPredicate(),
                    s.getObject().equals(pO) ? null : s.getObject()
            );
            patch.add(new PatchLine(PatchLine.Operator.ADD, ws));
            pS = s.getSubject();
            pP = s.getPredicate();
            pO = s.getObject();
        }

        return patch;
    }

    private static class StatementComparator implements Comparator<Statement> {
        @Override
        public int compare(Statement s1, Statement s2) {
            final int si = compare(s1.getSubject(), s2.getSubject());
            if (si != 0) {
                return si;
            } else {
                final int pi = compare(s1.getPredicate(), s2.getPredicate());
                if (pi != 0) {
                    return pi;
                }
                else {
                    return compare(s1.getObject(), s2.getObject());
                }
            }
        }

        private int compare(Value v1, Value v2) {
            return v1.toString().compareTo(v2.toString());
        }
    }

    private RdfPatchUtil() {
        // static access only
    }
}
