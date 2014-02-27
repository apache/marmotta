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
package org.apache.marmotta.platform.ldp.patch.model;

import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDFS;

/**
 * A single PatchLine, i.e. a operation of a Patch.
 * Can be either a {@code ADD} od {@code DELETE}
 */
public class PatchLine {
    public enum Operator {
        ADD("A"),
        DELETE("D");

        private final String cmd;

        private Operator(String cmd) {
            this.cmd = cmd;
        }

        public String getCommand() {
            return cmd;
        }

        public static Operator fromCommand(String cmd) {
            for (Operator op: values()) {
                if (op.cmd.equalsIgnoreCase(cmd)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown Operator: " + cmd);
        }
    }


    private final Operator operator;
    private final Statement statement;

    public PatchLine(Operator operator, Statement statement) {
        this.operator = operator;
        this.statement = statement;
    }

    public Operator getOperator() {
        return operator;
    }

    public Statement getStatement() {
        return statement;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof PatchLine) {
            if (operator != ((PatchLine) obj).operator) return false;

            return statement.equals(((PatchLine) obj).statement);
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("%S %s", operator.getCommand(), statement);

    }
}
