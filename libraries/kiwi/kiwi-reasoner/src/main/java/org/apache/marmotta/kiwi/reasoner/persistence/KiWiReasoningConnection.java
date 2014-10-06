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
package org.apache.marmotta.kiwi.reasoner.persistence;

import info.aduna.iteration.*;
import org.apache.marmotta.kiwi.caching.CacheManager;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.util.ResultSetIteration;
import org.apache.marmotta.kiwi.persistence.util.ResultTransformerFunction;
import org.apache.marmotta.kiwi.reasoner.model.program.*;
import org.apache.marmotta.kiwi.reasoner.model.query.QueryResult;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParser;
import org.apache.marmotta.kiwi.reasoner.parser.ParseException;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Extends the basic KiWiConnection by functionalities for storing, deleting and querying reasoner programs and rules
 * as well as justification information.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiReasoningConnection extends KiWiConnection {

    private static Logger log = LoggerFactory.getLogger(KiWiReasoningConnection.class);

    private ValueFactory valueFactory;

    private Map<Long,Rule> ruleIdCache;

    public KiWiReasoningConnection(KiWiPersistence persistence, KiWiDialect dialect, CacheManager cacheManager, ValueFactory valueFactory) throws SQLException {
        super(persistence, dialect, cacheManager);

        this.valueFactory = valueFactory;
        this.ruleIdCache = getCacheManager().getCacheByName("reasoning-rule-id");

    }


    /**
     * Store the rule given as argument in the database. This method will always create a new entry in the database
     * and does not check if a rule with the same name or content already exists.
     * @param rule
     * @throws SQLException
     */
    public void storeRule(Rule rule, Map<String, String> namespaces) throws SQLException {
        if(rule.getId() >= 0) {
            log.warn("rule {} already had a database ID, not persisting", rule);
            return;
        }

        requireJDBCConnection();

        // first create a new entry in the rules table
        rule.setId(getNextSequence());

        PreparedStatement insertRule = getPreparedStatement("rules.insert");
        synchronized (insertRule) {
            insertRule.setLong(1, rule.getId());
            insertRule.setString(2, rule.getName());
            insertRule.setString(3, rule.getDescription());
            insertRule.setString(4, rule.toString(namespaces));
            insertRule.executeUpdate();
        }

        ruleIdCache.put(rule.getId(), rule);
    }

    /**
     * Load the rule with the given database ID from the database.
     *
     * @param ruleId
     * @return
     * @throws SQLException
     */
    public Rule loadRuleById(long ruleId, Map<String, String> namespaces) throws SQLException {

        Rule cached = ruleIdCache.get(ruleId);

        if(cached != null) {
            return cached;
        } else {
            requireJDBCConnection();

            // load namespaces if they are not yet given
            if(namespaces == null) {
                namespaces = new HashMap<String, String>();
                PreparedStatement loadNamespaces = getPreparedStatement("namespaces.load_by_rule");
                synchronized (loadNamespaces) {
                    loadNamespaces.setLong(1,ruleId);
                    ResultSet namespaceResult = loadNamespaces.executeQuery();
                    while(namespaceResult.next()) {
                        namespaces.put(namespaceResult.getString("ns_prefix"), namespaceResult.getString("ns_uri"));
                    }
                    namespaceResult.close();
                }
            }


            PreparedStatement loadRule = getPreparedStatement("rules.load_by_id");
            synchronized (loadRule) {
                loadRule.setLong(1,ruleId);
                ResultSet result = loadRule.executeQuery();
                try {
                    if(result.next()) {
                        return constructRuleFromDatabase(result,namespaces);
                    } else {
                        return null;
                    }
                } catch (ParseException e) {
                    throw new SQLException("error while parsing rule body",e);
                } finally {
                    result.close();
                }
            }
        }
    }

    /**
     * Load the rule with the given database ID from the database.
     * @return
     * @throws SQLException
     */
    public CloseableIteration<Rule,SQLException> loadRulesByProgram(long programId, final Map<String, String> namespaces) throws SQLException, ParseException {

        requireJDBCConnection();

        PreparedStatement loadRule = getPreparedStatement("rules.load_by_program");
        synchronized (loadRule) {
            loadRule.setLong(1,programId);
            ResultSet result = loadRule.executeQuery();

            return new ResultSetIteration<Rule>(result, new ResultTransformerFunction<Rule>() {
                @Override
                public Rule apply(ResultSet row) throws SQLException {
                    try {
                        return constructRuleFromDatabase(row,namespaces);
                    } catch (ParseException e) {
                        throw new SQLException("error while parsing rule body",e);
                    }
                }
            });
        }
    }


    protected Rule constructRuleFromDatabase(ResultSet row, Map<String, String> namespaces) throws SQLException, ParseException {
        Rule cached = ruleIdCache.get(row.getLong("id"));

        if(cached != null) {
            return cached;
        } else {

            Rule result = KWRLProgramParser.parseRule(row.getString("body"),namespaces, valueFactory);
            result.setId(row.getLong("id"));
            result.setName(row.getString("name"));
            result.setDescription(row.getString("description"));

            ruleIdCache.put(result.getId(), result);

            return result;
        }
    }



    /**
     * Remove the rule given as argument from the database. This will also trigger removing all justifications
     * depending on this rule. The reasoner afterwards needs to clean up in a separate run all inferred triples t
     * hat are no longer justified.
     * @param rule
     * @throws SQLException
     */
    public void removeRule(Rule rule) throws SQLException {
        if(rule.getId() <= 0) {
            log.warn("rule {} does not have a database ID, cannot delete", rule);
            return;
        }

        requireJDBCConnection();

        PreparedStatement deleteRule = getPreparedStatement("rules.delete_by_id");
        synchronized (deleteRule) {
            deleteRule.setLong(1, rule.getId());
            deleteRule.executeUpdate();
        }

        ruleIdCache.remove(rule.getId());
    }



    /**
     * Store the program given as argument in the database. In case the program does not exist yet, it is
     * created together with all rules and namespace definitions. In case the program already exists, an exception
     * is thrown.
     *
     * @param program
     * @throws SQLException
     */
    public void storeProgram(Program program) throws SQLException {
        if(program.getId() >= 0) {
            throw new SQLException("Program already stored in the database");
        }

        requireJDBCConnection();


        // steps:
        // 1) create a program entry in the reasoner_programs table
        // 2) create namespace entries in the reasoner_program_namespaces table
        // 3) create rules in the reasoner_rules table
        // 4) add relation between program and rules to the reasoner_program_rules table


        // 1) create a program entry in the reasoner_programs table

        // get sequence ID
        program.setId(getNextSequence());

        PreparedStatement insertProgram = getPreparedStatement("programs.insert");
        synchronized (insertProgram) {
            insertProgram.setLong(1,program.getId());
            insertProgram.setString(2, program.getName());
            insertProgram.setString(3, program.getDescription());
            insertProgram.executeUpdate();
            insertProgram.clearParameters();
        }

        // 2) create namespace entries in the reasoner_program_namespaces table
        PreparedStatement insertNamespaces = getPreparedStatement("programs.add_ns");
        synchronized (insertNamespaces) {
            insertNamespaces.clearBatch();
            for(Map.Entry<String,String> entry : program.getNamespaces().entrySet()) {
                insertNamespaces.clearParameters();
                insertNamespaces.setLong(1, program.getId());
                insertNamespaces.setString(2,entry.getKey());
                insertNamespaces.setString(3,entry.getValue());
                insertNamespaces.addBatch();
            }
            insertNamespaces.executeBatch();
            insertNamespaces.clearBatch();
        }


        // 3) create rules in the reasoner_rules table
        for(Rule rule : program.getRules()) {
            storeRule(rule,program.getNamespaces());
        }

        // 4) add relation between program and rules to the reasoner_program_rules table
        PreparedStatement insertRuleRelation = getPreparedStatement("programs.add_rule");
        synchronized (insertRuleRelation) {
            insertRuleRelation.clearBatch();
            for(Rule rule : program.getRules()) {
                insertRuleRelation.clearParameters();
                insertRuleRelation.setLong(1,program.getId());
                insertRuleRelation.setLong(2,rule.getId());
                insertRuleRelation.addBatch();
            }
            insertRuleRelation.executeBatch();
            insertRuleRelation.clearBatch();
        }

        // done
    }


    /**
     * Store the program given as argument in the database. In case the program does not exist yet, it is
     * created together with all rules and namespace definitions. In case the program already exists, it is
     * updated according to the new program specification (i.e. rules and namespaces).
     * <p/>
     * We assume that the program has been newly parsed, so it does not have a database ID yet. To find the
     * existing program in the database, we therefore need to use the program name instead.
     *
     *
     * @param program       the program to update
     * @throws SQLException
     */
    public void updateProgram(Program program) throws SQLException {
        requireJDBCConnection();

        // steps:
        // 1) load old program by name
        // 2) create a diff between old and new program as follows:
        //    2a) check if description has been updated and store it if necessary in the database
        //    2b) check if namespaces have been removed, and remove them if necessary from the database
        //    2c) check if namespaces have been added, and add them if necessary to the database
        //    2d) check if rules have been removed, and remove them if necessary from the database
        //    2e) check if rules have been added, and add them if necessary to the database
        Program old = loadProgram(program.getName());
        if(old == null) {
            storeProgram(program);
        } else {
            //    2a) check if description has been updated and store it if necessary in the database
            if( (old.getDescription() != null && !old.getDescription().equals(program.getDescription())) ||
                    (old.getDescription() == null && program.getDescription() != null)) {
                PreparedStatement updateProgramDescription = getPreparedStatement("programs.update_desc");
                synchronized (updateProgramDescription) {
                    updateProgramDescription.setString(1, program.getDescription());
                    updateProgramDescription.setLong(2, old.getId());
                    updateProgramDescription.executeUpdate();
                }
            }

            //    2b) check if namespaces have been removed, and remove them if necessary from the database
            PreparedStatement deleteProgramNS = getPreparedStatement("programs.delete_ns");
            synchronized (deleteProgramNS) {
                deleteProgramNS.clearBatch();
                for(Map.Entry<String,String> oldNS : old.getNamespaces().entrySet()) {
                    if(!program.getNamespaces().entrySet().contains(oldNS)) {
                        deleteProgramNS.setLong(1,old.getId());
                        deleteProgramNS.setString(2,oldNS.getKey());
                        deleteProgramNS.setString(3,oldNS.getValue());
                        deleteProgramNS.addBatch();
                    }
                }
                deleteProgramNS.executeBatch();
            }

            //    2c) check if namespaces have been added, and add them if necessary to the database
            PreparedStatement addProgramNS = getPreparedStatement("programs.add_ns");
            synchronized (addProgramNS) {
                addProgramNS.clearBatch();
                for(Map.Entry<String,String> newNS : program.getNamespaces().entrySet()) {
                    if(!old.getNamespaces().entrySet().contains(newNS)) {
                        addProgramNS.setLong(1,old.getId());
                        addProgramNS.setString(2, newNS.getKey());
                        addProgramNS.setString(3, newNS.getValue());
                        addProgramNS.addBatch();
                    }
                }
                addProgramNS.executeBatch();
            }

            //    2d) check if rules have been removed, and remove them if necessary from the database
            PreparedStatement deleteProgramRule = getPreparedStatement("programs.delete_rule");
            PreparedStatement deleteRule        = getPreparedStatement("rules.delete_by_id");
            synchronized (deleteProgramRule) {
                deleteProgramRule.clearBatch();
                deleteRule.clearBatch();
                for(Rule oldRule : old.getRules()) {
                    if(!program.getRules().contains(oldRule)) {
                        deleteProgramRule.setLong(1,old.getId());
                        deleteProgramRule.setLong(2,oldRule.getId());
                        deleteProgramRule.addBatch();

                        deleteRule.setLong(1,oldRule.getId());
                        deleteRule.addBatch();

                        deleteJustifications(oldRule);

                        oldRule.setId(-1L);
                    }
                }
                deleteProgramRule.executeBatch();
                deleteRule.executeBatch();
            }

            //    2e) check if rules have been added, and add them if necessary to the database
            // first create a new entry in the rules table
            PreparedStatement insertRule = getPreparedStatement("rules.insert");
            PreparedStatement addProgramRule = getPreparedStatement("programs.add_rule");
            synchronized (insertRule) {
                insertRule.clearBatch();
                addProgramRule.clearBatch();

                for(Rule rule : program.getRules()) {
                    if(!old.getRules().contains(rule)) {
                        rule.setId(getNextSequence());

                        insertRule.setLong(1, rule.getId());
                        insertRule.setString(2, rule.getName());
                        insertRule.setString(3, rule.getDescription());
                        insertRule.setString(4, rule.toString(program.getNamespaces()));
                        insertRule.addBatch();

                        addProgramRule.setLong(1,old.getId());
                        addProgramRule.setLong(2,rule.getId());
                        addProgramRule.addBatch();
                    }
                }
                insertRule.executeBatch();
                addProgramRule.executeBatch();
            }
        }


    }


    /**
     * Load and return the program with the name given as argument. Constructing the program requires to invoke
     * the sKWRL parser to parse the textual rule representation.
     *
     *
     * @param name          the name of the program to load
     * @return              the loaded program, or null in case the program does not exist
     * @throws SQLException in case of a database problem or an unparsable program
     */
    public Program loadProgram(String name) throws SQLException {
        requireJDBCConnection();

        PreparedStatement loadProgram = getPreparedStatement("programs.load_by_name");
        synchronized (loadProgram) {
            loadProgram.setString(1, name);
            ResultSet result = loadProgram.executeQuery();
            try {
                if(result.next()) {
                    return constructProgramFromDatabase(result);
                } else {
                    return null;
                }
            } catch (ParseException e) {
                throw new SQLException("error while parsing program rules",e);
            } finally {
                result.close();
            }
        }

    }

    /**
     * Load and return the program with the ID given as argument. Constructing the program requires to invoke
     * the sKWRL parser to parse the textual rule representation.
     *
     * @return
     * @throws SQLException
     */
    public Program loadProgram(Long id) throws SQLException {
        requireJDBCConnection();

        PreparedStatement loadProgram = getPreparedStatement("programs.load_by_id");
        synchronized (loadProgram) {
            loadProgram.setLong(1, id);
            ResultSet result = loadProgram.executeQuery();
            try {
                if(result.next()) {
                    return constructProgramFromDatabase(result);
                } else {
                    return null;
                }
            } catch (ParseException e) {
                throw new SQLException("error while parsing program rules",e);
            } finally {
                result.close();
            }
        }

    }

    protected Program constructProgramFromDatabase(ResultSet row) throws SQLException, ParseException {
        Program program = new Program();
        program.setId(row.getLong("id"));
        program.setName(row.getString("name"));
        program.setDescription(row.getString("description"));

        // load namespaces
        PreparedStatement loadProgramNS = getPreparedStatement("namespaces.load_by_program");
        synchronized (loadProgramNS) {
            loadProgramNS.setLong(1, program.getId());
            ResultSet nsResult = loadProgramNS.executeQuery();
            while(nsResult.next()) {
                program.addNamespace(nsResult.getString("ns_prefix"), nsResult.getString("ns_uri"));
            }
            nsResult.close();
        }

        // load rules
        PreparedStatement loadRule = getPreparedStatement("rules.load_by_program");
        synchronized (loadRule) {
            loadRule.setLong(1,program.getId());
            ResultSet ruleResult = loadRule.executeQuery();
            while(ruleResult.next()) {
                program.addRule(constructRuleFromDatabase(ruleResult,program.getNamespaces()));
            }
            ruleResult.close();
        }

        return program;
    }


    /**
     * List all currently existing programs in the database.
     * @return
     * @throws SQLException
     */
    public CloseableIteration<Program, SQLException> listPrograms() throws SQLException {
        requireJDBCConnection();

        PreparedStatement listPrograms = getPreparedStatement("programs.list");
        synchronized (listPrograms) {
            ResultSet result = listPrograms.executeQuery();

            return new ResultSetIteration<Program>(result, new ResultTransformerFunction<Program>() {
                @Override
                public Program apply(ResultSet row) throws SQLException {
                    try {
                        return constructProgramFromDatabase(row);
                    } catch (ParseException e) {
                        throw new SQLException("error while parsing program rules",e);
                    }
                }
            });
        }
    }


    /**
     * Delete the (persistent) program given as argument from the database, including all rules, justifications and
     * namespaces refenced by the program or its rules.
     *
     * @param program
     * @throws SQLException
     */
    public void deleteProgram(Program program) throws SQLException {
        if(program.getId() <= 0) {
            log.warn("cannot delete non-persistent program (name={})!", program.getName());
            return;
        }

        requireJDBCConnection();

        // 1. delete all rule associations and rules including the justifications they depend on
        PreparedStatement deleteProgramRule = getPreparedStatement("programs.delete_rule");
        PreparedStatement deleteRule        = getPreparedStatement("rules.delete_by_id");
        synchronized (deleteProgramRule) {
            deleteProgramRule.clearBatch();
            deleteRule.clearBatch();
            for(Rule rule : program.getRules()) {
                deleteProgramRule.setLong(1, program.getId());
                deleteProgramRule.setLong(2, rule.getId());
                deleteProgramRule.addBatch();

                deleteRule.setLong(1, rule.getId());
                deleteRule.addBatch();

                deleteJustifications(rule);
            }
            deleteProgramRule.executeBatch();
            deleteRule.executeBatch();
        }

        // 2. delete all namespaces
        PreparedStatement deleteProgramNS = getPreparedStatement("programs.delete_ns");
        synchronized (deleteProgramNS) {
            deleteProgramNS.clearBatch();
            for(Map.Entry<String,String> ns : program.getNamespaces().entrySet()) {
                deleteProgramNS.setLong(1,   program.getId());
                deleteProgramNS.setString(2, ns.getKey());
                deleteProgramNS.setString(3, ns.getValue());
                deleteProgramNS.addBatch();
            }
            deleteProgramNS.executeBatch();
        }

        // 3. delete program itself
        PreparedStatement deleteProgram = getPreparedStatement("programs.delete");
        synchronized (deleteProgram) {
            deleteProgram.setLong(1, program.getId());
            deleteProgram.executeUpdate();
        }
    }


    /**
     * Store a collection of new justification in the database. Uses an SQL batch operation to speed up
     * database insertion.
     *
     * @param justifications
     * @throws SQLException
     */
    public void storeJustifications(Iterable<Justification> justifications) throws SQLException {
        requireJDBCConnection();

        PreparedStatement insertJustification = getPreparedStatement("justifications.insert");
        PreparedStatement justificationAddTriple = getPreparedStatement("justifications.add_triple");
        PreparedStatement justificationAddRule   = getPreparedStatement("justifications.add_rule");

        synchronized (insertJustification) {
            insertJustification.clearBatch();
            justificationAddTriple.clearBatch();
            justificationAddRule.clearBatch();

            for(Justification j : justifications) {
                if(j.getId() >= 0) {
                    log.warn("justification is already stored in database, not persisting again (database ID: {})", j.getId());
                } else {
                    j.setId(getNextSequence());
                    j.setCreatedAt(new Date());

                    // insert an entry to the reasoner_justifications table
                    insertJustification.clearParameters();
                    insertJustification.setLong(1, j.getId());
                    insertJustification.setLong(2, j.getTriple().getId());
                    insertJustification.setTimestamp(3, new Timestamp(j.getCreatedAt().getTime()));
                    insertJustification.addBatch();

                    // insert join entries for all supporting triples
                    for(KiWiTriple supportingTriple : j.getSupportingTriples()) {
                        if(supportingTriple.getId() < 0) {
                            log.error("supporting triple is not persistent, cannot store justification (triple={})",supportingTriple);
                        } else {
                            justificationAddTriple.clearParameters();
                            justificationAddTriple.setLong(1, j.getId());
                            justificationAddTriple.setLong(2, supportingTriple.getId());
                            justificationAddTriple.addBatch();
                        }
                    }

                    // insert join entries for all supporting rules
                    for(Rule supportingRule : j.getSupportingRules()) {
                        if(supportingRule.getId() <= 0) {
                            log.error("supporting rule is not persistent, cannot store justification (rule={})",supportingRule);
                        } else {
                            justificationAddRule.clearParameters();
                            justificationAddRule.setLong(1, j.getId());
                            justificationAddRule.setLong(2, supportingRule.getId());
                            justificationAddRule.addBatch();
                        }
                    }
                }

            }
            insertJustification.executeBatch();
            justificationAddTriple.executeBatch();
            justificationAddRule.executeBatch();

        }
    }


    /**
     * Delete the justifications given as argument (batch operation).
     *
     * @param justifications
     * @throws SQLException
     */
    public void deleteJustifications(Iterable<Justification> justifications) throws SQLException {
        deleteJustifications(new IteratorIteration<Justification, SQLException>(justifications.iterator()));
    }


    /**
     * Delete the justifications given as argument (batch operation).
     *
     * @param justifications
     * @throws SQLException
     */
    public void deleteJustifications(Iteration<Justification, SQLException> justifications) throws SQLException {
        requireJDBCConnection();

        PreparedStatement deleteJustification = getPreparedStatement("justifications.delete");
        PreparedStatement deleteJustificationRules = getPreparedStatement("justifications.del_rule");
        PreparedStatement deleteJustificationTriples = getPreparedStatement("justifications.del_triple");

        synchronized (deleteJustification) {
            deleteJustification.clearBatch();
            deleteJustificationRules.clearBatch();
            deleteJustificationTriples.clearBatch();

            while(justifications.hasNext()) {
                Justification j = justifications.next();
                if(j.getId() < 0) {
                    log.error("cannot delete justification since it does not have a database ID");
                } else {
                    deleteJustificationRules.setLong(1, j.getId());
                    deleteJustificationRules.addBatch();

                    deleteJustificationTriples.setLong(1, j.getId());
                    deleteJustificationTriples.addBatch();

                    deleteJustification.setLong(1, j.getId());
                    deleteJustification.addBatch();
                }
            }
            Iterations.closeCloseable(justifications);

            deleteJustificationTriples.executeBatch();
            deleteJustificationRules.executeBatch();
            deleteJustification.executeBatch();
        }
    }



    /**
     * Delete the justifications referring to a certain rule given as argument.
     *
     * @param rule
     * @throws SQLException
     */
    public void deleteJustifications(Rule rule) throws SQLException {
        deleteJustifications(listJustificationsBySupporting(rule));
    }


    /**
     * Delete the justifications referring to a certain triple given as argument.
     *
     * @param triple
     * @throws SQLException
     */
    public void deleteJustifications(KiWiTriple triple) throws SQLException {
        deleteJustifications(listJustificationsBySupporting(triple));
    }

    /**
     * Delete all justifications.
     *
     * @throws SQLException
     */
    public void deleteJustifications() throws SQLException {
        requireJDBCConnection();

        PreparedStatement deleteJustification = getPreparedStatement("justifications.delete_all");
        PreparedStatement deleteJustificationRules = getPreparedStatement("justifications.delete_all_rules");
        PreparedStatement deleteJustificationTriples = getPreparedStatement("justifications.delete_all_triples");
        synchronized (deleteJustification) {
            deleteJustificationRules.executeUpdate();
            deleteJustificationTriples.executeUpdate();
            deleteJustification.executeUpdate();
        }
    }

    /**
     * List all justifications supported by the given rule.
     *
     * @param rule
     * @return
     * @throws SQLException
     */
    public CloseableIteration<Justification,SQLException> listJustificationsBySupporting(Rule rule) throws SQLException {
        if(rule.getId() <= 0) {
            return new EmptyIteration<Justification, SQLException>();
        } else {
            requireJDBCConnection();

            PreparedStatement listByRule = getPreparedStatement("justifications.load_by_srule");
            synchronized (listByRule) {
                listByRule.setLong(1, rule.getId());

                ResultSet result = listByRule.executeQuery();

                return new ResultSetIteration<Justification>(result, new ResultTransformerFunction<Justification>() {
                    @Override
                    public Justification apply(ResultSet row) throws SQLException {
                        return constructJustificationFromDatabase(row);
                    }
                });
            }
        }
    }

    /**
     * List all justifications supported by the given triple.
     * @param triple
     * @return
     * @throws SQLException
     */
    public CloseableIteration<Justification,SQLException> listJustificationsBySupporting(KiWiTriple triple) throws SQLException {
        if(triple.getId() < 0) {
            return new EmptyIteration<Justification, SQLException>();
        } else {
            requireJDBCConnection();

            PreparedStatement listByTriple = getPreparedStatement("justifications.load_by_striple");
            synchronized (listByTriple) {
                listByTriple.setLong(1, triple.getId());

                ResultSet result = listByTriple.executeQuery();

                return new ResultSetIteration<Justification>(result, new ResultTransformerFunction<Justification>() {
                    @Override
                    public Justification apply(ResultSet row) throws SQLException {
                        return constructJustificationFromDatabase(row);
                    }
                });
            }
        }
    }

    /**
     * List all justifications supporting the given triple.
     * @param triple
     * @return
     * @throws SQLException
     */
    public CloseableIteration<Justification,SQLException> listJustificationsForTriple(KiWiTriple triple) throws SQLException {
        if(triple.getId() < 0) {
            return new EmptyIteration<Justification, SQLException>();
        } else {
            return listJustificationsForTriple(triple.getId());
        }
    }

    /**
     * List all justifications supporting the given triple.
     * @param tripleId
     * @return
     * @throws SQLException
     */
    public CloseableIteration<Justification,SQLException> listJustificationsForTriple(long tripleId) throws SQLException {
        requireJDBCConnection();

        PreparedStatement listForTriple = getPreparedStatement("justifications.load_by_triple");
        synchronized (listForTriple) {
            listForTriple.setLong(1, tripleId);

            ResultSet result = listForTriple.executeQuery();

            return new ResultSetIteration<Justification>(result, new ResultTransformerFunction<Justification>() {
                @Override
                public Justification apply(ResultSet row) throws SQLException {
                    return constructJustificationFromDatabase(row);
                }
            });
        }
    }


    protected Justification constructJustificationFromDatabase(ResultSet row) throws SQLException {
        Justification result = new Justification();
        result.setId(row.getLong("id"));
        result.setTriple(loadTripleById(row.getLong("triple_id")));
        result.setCreatedAt(new Date(row.getTimestamp("createdAt").getTime()));

        // load supporting rules and triples
        PreparedStatement loadRules = getPreparedStatement("justifications.load_rules");
        synchronized (loadRules) {
            loadRules.setLong(1, result.getId());
            ResultSet ruleResult = loadRules.executeQuery();
            while(ruleResult.next()) {
                result.getSupportingRules().add(loadRuleById(ruleResult.getLong("rule_id"),null));
            }
            ruleResult.close();
        }

        PreparedStatement loadTriples = getPreparedStatement("justifications.load_triples");
        synchronized (loadTriples) {
            loadTriples.setLong(1, result.getId());
            ResultSet tripleResult = loadTriples.executeQuery();
            while(tripleResult.next()) {
                result.getSupportingTriples().add(loadTripleById(tripleResult.getLong("triple_id")));
            }
            tripleResult.close();
        }

        return result;
    }

    /**
     * List all triples that are not supported by at least one justification.
     * @return
     * @throws SQLException
     */
    public CloseableIteration<KiWiTriple, SQLException> listUnsupportedTriples() throws SQLException {
        requireJDBCConnection();

        PreparedStatement listUnsupported = getPreparedStatement("justifications.list_unsupported");
        synchronized (listUnsupported) {
            ResultSet result = listUnsupported.executeQuery();

            return new ResultSetIteration<KiWiTriple>(result, new ResultTransformerFunction<KiWiTriple>() {
                @Override
                public KiWiTriple apply(ResultSet row) throws SQLException {
                    return constructTripleFromDatabase(row);
                }
            });
        }

    }



    /**
     * Evaluate a query on the triple store. The query parameters passed to the method call are
     * translated into database queries in HQL and directly evaluated by a single database
     * query that yields the query bindings.
     * <p/>
     * The query patterns may share variable fields, in which case the database query will evaluate
     * a join. In case the optional parameter is true, the database query will succeed even if the query
     * does not yield a value.
     *
     * @param patterns        the set of patterns to query; patterns are considered to be connected by AND;
     *                        occurrences of the same variable in multiple patterns will be evaluated as a join
     * @param initialBindings initial bindings of variable fields may be used in case some bindings are
     *                        already available to further restrict the results; the purpose of this parameter
     *                        is to support query-by-example queries as well as to speed up e.g. incremental
     *                        reasoning
     * @param filters         a set of filters to apply to the result before returning the bindings; depending
     *                        on the kind of filter, filtering will be carried out by the database or in memory
     * @param orderBy         list of variables by whose bindings the result rows should be ordered; variables
     *                        at the beginning of the list take precedence over variables that are further behind
     * @return a list of bindings matching the query patterns and filters, ordered by the specified
     *         variables and offset and limited by the parameters given
     */
    public CloseableIteration<QueryResult, SQLException> query(final Collection<Pattern> patterns, final QueryResult initialBindings, Set<Filter> filters, List<VariableField> orderBy, final boolean justifications) throws SQLException {
        requireJDBCConnection();

        if(filters != null) {
            throw new IllegalArgumentException("filters are not yet supported by the QueryService");
        }

        // some definitions
        String[] positions = new String[] {"subject","predicate","object","context"};


        // associate a name with each pattern; the names are used in the database query to refer to the triple
        // that matched this pattern and in the construction of variable names for the HQL query
        int patternCount = 0;
        final Map<Pattern,String> patternNames = new HashMap<Pattern, String>();
        for(Pattern p : patterns) {
            patternNames.put(p,"P"+ (++patternCount));
        }

        // find all variables occurring in the patterns and create a map to map them to
        // field names in the database query; each variable will have one or several field names,
        // one for each pattern it occurs in; field names are constructed automatically by a counter
        // and the pattern name to ensure the name is a valid HQL identifier
        int variableCount = 0;

        // a map for the variable names; will look like { ?x -> "V1", ?y -> "V2", ... }
        final Map<VariableField,String> variableNames = new HashMap<VariableField, String>();

        // a map for mapping variables to field names; each variable might have one or more field names,
        // depending on the number of patterns it occurs in; will look like
        // { ?x -> ["P1_V1", "P2_V1"], ?y -> ["P2_V2"], ... }
        Map<VariableField,List<String>> queryVariables = new HashMap<VariableField, List<String>>();
        for(Pattern p : patterns) {
            Field[] fields = new Field[] {
                    p.getSubject(),
                    p.getProperty(),
                    p.getObject(),
                    p.getContext()
            };
            for(int i = 0; i<fields.length; i++) {
                if(fields[i] != null && fields[i].isVariableField()) {
                    VariableField v = (VariableField)fields[i];
                    if(variableNames.get(v) == null) {
                        variableNames.put(v,"V"+ (++variableCount));
                        queryVariables.put(v,new LinkedList<String>());
                    }
                    String pName = patternNames.get(p);
                    String vName = variableNames.get(v);
                    queryVariables.get(v).add(pName + "_" + positions[i] + "_" + vName);
                }
            }
        }

        // build the select clause by projecting for each query variable the first name
        StringBuilder selectClause = new StringBuilder();
        final List<VariableField> selectVariables = new LinkedList<VariableField>();
        for(Iterator<VariableField> it = queryVariables.keySet().iterator(); it.hasNext(); ) {
            VariableField v = it.next();
            String projectedName = variableNames.get(v);
            String fromName = queryVariables.get(v).get(0);
            selectClause.append(fromName);
            selectClause.append(".id as ");
            selectClause.append(projectedName);
            if(it.hasNext()) {
                selectClause.append(", ");
            }
            selectVariables.add(v);
        }
        if(justifications) {
            // project also the ids of triples that have matched; we use it for building justifications
            for(Iterator<Pattern> it = patterns.iterator(); it.hasNext(); ) {
                Pattern p = it.next();
                if(selectClause.length() > 0) {
                    selectClause.append(", ");
                }
                selectClause.append(patternNames.get(p));
                selectClause.append(".id as ");
                selectClause.append(patternNames.get(p));
            }
        }



        // build the from-clause of the query; the from clause is constructed as follows:
        // 1. for each pattern P, there will be a "KiWiTriple P" in the from clause
        // 2. for each variable V in P occurring in
        //    - subject, there will be a "inner join P.subject as P_S_V" or "left outer join P.subject as P_S_V",
        //      depending on whether the "optional" parameter is false or true
        //    - property, there will be a "inner join P.property as P_P_V" or "left outer join p.property as P_P_V"
        //    - object, there will be a "inner join P.object as P_O_V" or "left outer join p.object as P_O_V"
        //    - context, there will be a "inner join P.context as P_C_V" or "left outer join p.context as P_C_V"
        StringBuilder fromClause = new StringBuilder();
        for(Iterator<Pattern> it = patterns.iterator(); it.hasNext(); ) {
            Pattern p = it.next();
            String pName = patternNames.get(p);
            fromClause.append("triples "+pName);

            Field[] fields = new Field[] {
                    p.getSubject(),
                    p.getProperty(),
                    p.getObject(),
                    p.getContext()
            };
            for(int i = 0; i<fields.length; i++) {
                if(fields[i] != null && fields[i].isVariableField()) {
                    String vName = variableNames.get(fields[i]);
                    fromClause.append(" INNER JOIN nodes AS ");
                    fromClause.append(pName + "_"+positions[i]+"_" + vName);
                    fromClause.append(" ON " + pName + "." + positions[i] + " = ");
                    fromClause.append(pName + "_"+positions[i]+"_" + vName + ".id ");
                }
            }

            if(it.hasNext()) {
                fromClause.append(",\n ");
            }
        }


        // build the where clause as follows:
        // 1. iterate over all patterns and for each resource and literal field in subject,
        //    property, object, or context, and set a query condition according to the
        //    nodes given in the pattern
        // 2. for each variable that has more than one occurrences, add a join condition
        // 3. for each variable in the initialBindings, add a condition to the where clause

        // list of where conditions that will later be connected by AND
        List<String> whereConditions = new LinkedList<String>();


        // 1. iterate over all patterns and for each resource and literal field in subject,
        //    property, object, or context, and set a query condition according to the
        //    nodes given in the pattern
        for(Pattern p : patterns) {
            String pName = patternNames.get(p);
            Field[] fields = new Field[] {
                    p.getSubject(),
                    p.getProperty(),
                    p.getObject(),
                    p.getContext()
            };
            for(int i = 0; i<fields.length; i++) {
                // find node id of the resource or literal field and use it in the where clause
                // in this way we can avoid setting too many query parameters
                long nodeId = -1;
                if(fields[i] != null && fields[i].isLiteralField()) {
                    nodeId = ((KiWiNode)((LiteralField)fields[i]).getLiteral()).getId();
                } else if(fields[i] != null && fields[i].isResourceField()) {
                    nodeId = ((KiWiNode)((ResourceField)fields[i]).getResource()).getId();
                }

                if(nodeId >= 0) {
                    String condition = pName+"."+positions[i]+" = " + nodeId;
                    whereConditions.add(condition);
                }
            }
        }

        // 2. for each variable that has more than one occurrences, add a join condition
        for(VariableField v : queryVariables.keySet()) {
            List<String> vNames = queryVariables.get(v);
            for(int i = 1; i < vNames.size(); i++) {
                String vName1 = vNames.get(i-1);
                String vName2 = vNames.get(i);
                whereConditions.add(vName1 + ".id = " + vName2 + ".id");
            }
        }

        // 3. for each variable in the initialBindings, add a condition to the where clause setting it
        //    to the node given as binding
        if(initialBindings != null) {
            for(VariableField v : initialBindings.getBindings().keySet()) {
                List<String> vNames = queryVariables.get(v);
                if(vNames != null && vNames.size() > 0) {
                    String vName = vNames.get(0);
                    KiWiNode binding = initialBindings.getBindings().get(v);
                    whereConditions.add(vName+".id = "+binding.getId());
                }
            }
        }

        // 4. for each pattern, ensure that the matched triple is not marked as deleted
        for(Pattern p : patterns) {
            String pName = patternNames.get(p);
            whereConditions.add(pName+".deleted = false");
        }

        // construct the where clause
        StringBuilder whereClause = new StringBuilder();
        for(Iterator<String> it = whereConditions.iterator(); it.hasNext(); ) {
            whereClause.append(it.next());
            whereClause.append("\n ");
            if(it.hasNext()) {
                whereClause.append("AND ");
            }
        }


        // finally, construct the order by clause
        StringBuilder orderByClause = new StringBuilder();
        if(orderBy != null && orderBy.size() > 0) {
            for(Iterator<VariableField> it = orderBy.iterator(); it.hasNext(); ) {
                VariableField v = it.next();
                String vName = variableNames.get(v);
                orderByClause.append(vName);
                if(it.hasNext()) {
                    orderByClause.append(", ");
                }
            }
        }

        // build the query string
        String queryString =
                "SELECT " + selectClause + "\n " +
                        "FROM " + fromClause + "\n " +
                        "WHERE " + whereClause + "\n " +
                        ((orderBy != null && orderBy.size() > 0)?"ORDER BY "+orderByClause+" ASC\n ":"");

        log.debug("constructed SQL query string {}",queryString);

        PreparedStatement queryStatement = getJDBCConnection().prepareStatement(queryString);
        ResultSet result = queryStatement.executeQuery();

        return new ResultSetIteration<QueryResult>(result, true, new ResultTransformerFunction<QueryResult>() {
            @Override
            public QueryResult apply(ResultSet row) throws SQLException {
                QueryResult resultRow = new QueryResult();

                long[] nodeIds = new long[selectVariables.size()];
                for(int i=0; i<selectVariables.size(); i++) {
                    nodeIds[i] = row.getLong(variableNames.get(selectVariables.get(i)));
                }
                KiWiNode[] nodes = loadNodesByIds(nodeIds);

                for(int i=0; i<selectVariables.size(); i++) {
                    VariableField v = selectVariables.get(i);
                    resultRow.getBindings().put(v, nodes[i]);
                }

                if(justifications) {
                    for(Pattern p : patterns) {
                        resultRow.getJustifications().add(loadTripleById(row.getLong(patternNames.get(p))));
                    }
                }

                if(initialBindings != null && initialBindings.getBindings().size() > 0) {
                    for(VariableField v : initialBindings.getBindings().keySet()) {
                        if(!resultRow.getBindings().containsKey(v)) {
                            resultRow.getBindings().put(v,initialBindings.getBindings().get(v));
                        }
                    }
                    if(justifications) {
                        resultRow.getJustifications().addAll(initialBindings.getJustifications());
                    }
                }
                return resultRow;
            }
        });
    }


}
