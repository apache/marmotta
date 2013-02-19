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
import com.google.common.base.Preconditions;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;

public class WordCountFunction<Node> extends SelectorFunction<Node> {

    private final URI dataType = URI.create("http://www.w3.org/2001/XMLSchema#integer");

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        Preconditions.checkArgument(args.length == 1, "Check usage: " + getSignature());

        LinkedList<Node> result = new LinkedList<Node>();
        for (Node node : args[0]) {
            final String stringValue = backend.stringValue(node);
            boolean isWordChar = false;
            int c = 0;
            for (int i = 0; i < stringValue.length(); i++) {
                final boolean isWC = Character.isLetterOrDigit(stringValue.codePointAt(i));
                if (!isWordChar && isWC) {
                    c++;
                }
                isWordChar = isWC;
            }
            result.add(backend.createLiteral(String.valueOf(c), null, dataType));
        }

        return result;
    }

    @Override
    public String getSignature() {
        return "fn:wc(text : LiteralList) : IntegerLiteralList";
    }

    @Override
    public String getDescription() {
        return "Calculates the length of the provided literals (counting continuous chunks of letters+digits)";
    }

    @Override
    public String getLocalName() {
        return "wc";
    }

}
