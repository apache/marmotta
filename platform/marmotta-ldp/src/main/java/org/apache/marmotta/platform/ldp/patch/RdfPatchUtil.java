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
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParser;
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
 * Created by jakob on 2/25/14.
 */
public class RdfPatchUtil {

    public static void applyPatch(Repository repository, String patch, Resource... contexts) throws RepositoryException, ParseException {
        applyPatch(repository, getPatch(patch), contexts);
    }

    public static void applyPatch(Repository repository, InputStream patchSource, Resource... contexts) throws RepositoryException, ParseException {
        applyPatch(repository, getPatch(patchSource), contexts);
    }
    public static void applyPatch(RepositoryConnection con, String patch, Resource... contexts) throws RepositoryException, ParseException {
        applyPatch(con, getPatch(patch), contexts);
    }
    public static void applyPatch(RepositoryConnection con, InputStream patchSource, Resource... contexts) throws RepositoryException, ParseException {
        applyPatch(con, getPatch(patchSource), contexts);
    }

    public static void applyPatch(Repository repository, List<PatchLine> patch, Resource... contexts) throws RepositoryException {
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

    public static void applyPatch(RepositoryConnection con, List<PatchLine> patch, Resource... contexts) throws RepositoryException {
        Resource subject = null;
        URI predicate = null;
        Value object = null;

        for (PatchLine patchLine : patch) {
            final Statement statement = patchLine.getStatement();
            subject = statement.getSubject()!=null? statement.getSubject():subject;
            predicate = statement.getPredicate()!=null?statement.getPredicate():predicate;
            object = statement.getObject()!=null?statement.getObject():object;

            switch (patchLine.getOperator()) {
                case ADD:
                    con.add(subject, predicate, object, contexts);
                    break;
                case DEL:
                    con.remove(subject, predicate, object, contexts);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown patch operation: " + patchLine.getOperator());
            }
        }
    }

    private static List<PatchLine> getPatch(InputStream is) throws ParseException {
        RdfPatchParser parser = new RdfPatchParser(is);
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
