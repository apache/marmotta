/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.ldpath.model.functions.text;

import at.newmedialab.ldpath.api.backend.RDFBackend;
import at.newmedialab.ldpath.api.functions.SelectorFunction;

import java.util.Collection;
import java.util.LinkedList;

public class StrRightFunction<Node> extends SelectorFunction<Node> {

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        try {
            if (args.length != 2) { throw new IllegalArgumentException("LdPath function " + getLocalName() + " requires 2 arguments"); }
            if (args[1].size() != 1) { throw new IllegalArgumentException("len argument must be a single literal for function " + getLocalName()); }

            final Collection<Node> nodes = args[0];
            final int length = Math.max(backend.intValue(args[1].iterator().next()), 0);

            final Collection<Node> result = new LinkedList<Node>();
            for (Node node : nodes) {
                final String str = backend.stringValue(node);
                result.add(backend.createLiteral(str.substring(Math.max(0, str.length() - length))));
            }

            return result;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(nfe);
        } catch (ArithmeticException ae) {
            throw new IllegalArgumentException(ae);
        }
    }

    @Override
    public String getSignature() {
        return "fn:strRight(node::Node, length::NumericLiteral) :: StringLiteral";
    }

    @Override
    public String getDescription() {
        return "take the last n chars from the string representation";
    }

    @Override
    protected String getLocalName() {
        return "strRight";
    }

}
