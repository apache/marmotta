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

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.openrdf.model.*;
import org.openrdf.rio.turtle.TurtleUtil;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serialize/Write an rdf-patch document
 *
 * @author Jakob Frank
 */
public class RdfPatchIO {

    /**
     * Serialize the provided Patch as String.
     * @param patch the patch to serialize
     * @return String representation of the given patch
     */
    public static String toString(List<PatchLine> patch) {
        return toString(patch, Collections.<String,String>emptyMap());
    }

    /**
     * Serialize the provided Patch to String.
     * @param patch the patch to serialize
     * @param namespaces the namespaces to use
     * @return String representation of the given patch.
     */
    public static String toString(List<PatchLine> patch, Map<String, String> namespaces) {
        StringBuilderWriter sbw = new StringBuilderWriter();
        writePatch(sbw, patch, namespaces);
        return sbw.toString();
    }

    /**
     * Write the provided patch to the given OutputStream.
     * @param os the OutputStream to write to, will <em>not</em> be closed.
     * @param patch the patch to write
     */
    public static void writePatch(OutputStream os, List<PatchLine> patch) {
        writePatch(os, patch, Collections.<String,String>emptyMap());
    }

    /**
     * Write the provided patch to the given OutputStream.
     * @param os the OutputStream to write to, will <em>not</em> be closed.
     * @param patch the patch to write
     * @param namespaces the namespaces to write (and replace)
     */
    public static void writePatch(OutputStream os, List<PatchLine> patch, Map<String, String> namespaces) {
        writePatch(new OutputStreamWriter(os), patch, namespaces);
    }

    /**
     * Write the provided patch to the given Writer.
     * @param writer the target to write to, will <em>not</em> be closed.
     * @param patch the patch to write
     */
    public static void writePatch(Writer writer, List<PatchLine> patch) {
        writePatch(writer, patch, Collections.<String,String>emptyMap());
    }

    /**
     * Write the provided patch to the given Writer.
     * @param writer the target to write to, will <em>not</em> be closed.
     * @param patch the patch to write
     * @param namespaces the namespaces to write (and replace)
     */
    public static void writePatch(Writer writer, List<PatchLine> patch, Map<String, String> namespaces) {
        PrintWriter ps = new PrintWriter(writer);

        HashMap<String, String> inverseNamespaceMap = new HashMap<>(namespaces.size());
        for (Map.Entry<String, String> ns : namespaces.entrySet()) {
            inverseNamespaceMap.put(ns.getValue(), ns.getKey());
            ps.printf("@prefix %s: <%s> .%n", ns.getKey(), ns.getValue());
        }
        ps.println();
        for (PatchLine patchLine : patch) {
            final Statement st = patchLine.getStatement();
            ps.printf("%S %s %s %s .", patchLine.getOperator().getCommand(),
                    io(st.getSubject(), inverseNamespaceMap),
                    io(st.getPredicate(), inverseNamespaceMap),
                    io(st.getObject(), inverseNamespaceMap)
                    );
        }

        ps.flush();
    }

    /**
     * "Stringify" a Sesame-Value for rdf-patch.
     * Heavily inspired by {@link org.openrdf.rio.turtle.TurtleWriter} and {@link org.openrdf.rio.turtle.TurtleUtil}
     *
     * @param v the Value to write
     * @param inverseNamespaceMap an inverse Map of known namespaces (e.g. http://example.com/foo# -> foo)
     * @return String-serialization of the Value.
     */
    private static String io(Value v, Map<String, String> inverseNamespaceMap) {
        if (v == null) {
            return "R";
        } else if (v instanceof URI) {
            final String uri = v.stringValue();
            String prefix = null;

            final int si = TurtleUtil.findURISplitIndex(uri);
            if (si > 0) {
                String namespace = uri.substring(0, si);
                prefix = inverseNamespaceMap.get(namespace);
            }

            if (prefix != null) {
                return String.format("%s:%s", prefix, uri.substring(si));
            } else {
                return String.format("<%s>", uri);
            }
        } else if (v instanceof BNode) {
            return "_:" + ((BNode) v).getID();
        } else if (v instanceof Literal) {
            final Literal l = (Literal) v;
            final String label = l.getLabel();
            final StringBuilder sb = new StringBuilder();
            if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
                sb.append("\"\"\"").append(TurtleUtil.encodeLongString(label)).append("\"\"\"");
            } else {
                sb.append("\"").append(TurtleUtil.encodeString(label)).append("\"");
            }
            if (l.getLanguage() != null) {
                sb.append("@").append(l.getLanguage());
            } else if (l.getDatatype() != null) {
                sb.append("^^").append(io(l.getDatatype(), inverseNamespaceMap));
            }
            return sb.toString();
        }

        // Fall through
        return v.toString();
    }

    private RdfPatchIO() {
        // static access only
    }
}
