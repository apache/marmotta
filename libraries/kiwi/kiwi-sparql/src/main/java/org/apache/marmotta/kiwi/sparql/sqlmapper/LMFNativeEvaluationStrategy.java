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
package at.newmedialab.lmf.sparql.services.evaluation.sql;

import at.newmedialab.sesame.commons.model.LiteralCommons;
import info.aduna.iteration.CloseableIteration;
import kiwi.core.api.config.ConfigurationService;
import kiwi.core.api.persistence.PersistenceService;
import kiwi.core.api.triplestore.SesameService;
import kiwi.core.events.DBInitialisationEvent;
import kiwi.core.model.rdf.KiWiNode;
import kiwi.core.services.sail.LMFEvaluationStrategy;
import org.apache.commons.validator.routines.UrlValidator;
import org.jooq.Cursor;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.impl.MapBindingSet;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An implementation of a Sesame evaluation strategy that translates SPARQL queries into native SQL queries
 * on the LMF triple store for improved performance. This implementation is based on the jOOQ Java-to-SQL library
 * to achieve a certain level of database independence.
 *
 * TODO: this class should somehow move to the lmf-sparql package
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class LMFNativeEvaluationStrategy implements LMFEvaluationStrategy {

    @Inject
    private Logger log;

    @Inject
    private PersistenceService persistenceService;

    @Inject
    private ConfigurationService configurationService;


    @Inject
    private SesameService sesameService;


    @PostConstruct
    public void initialise() {
        log.info("initialising LMF native query evaluation strategy (SPARQL -> SQL mapper)");

    }


    public void onDatabaseInitialisation(@Observes DBInitialisationEvent event) {
        log.debug("Received DB-initialisation event, checking existance of SPARQL query indexes");

        if(!"off".equals(configurationService.getStringConfiguration("database.mode")) && "native".equals(configurationService.getStringConfiguration("sparql.strategy"))) {
            checkIndexes();
        }
    }


    /**
     * Depending on the database dialect, check for the existance of appropriate indexes to speed up
     * typical SPARQL queries
     */
    private void checkIndexes() {
        switch (getSQLDialect()) {
            case POSTGRES:
                log.info("SQL Dialect is PostgreSQL, creating advanced indexes");
                createIndexesPostgres();
                break;
        }

    }

    private void createIndexesPostgres() {
        Connection connection = persistenceService.getJDBCConnection();
        try {
            // index for language matches
            String idx_sparql_lang = "create index idx_sparql_lang on kiwinode ( coalesce(locale,''));";
            connection.createStatement().executeUpdate("drop index if exists idx_sparql_lang");
            connection.createStatement().executeUpdate(idx_sparql_lang);

            // index for string value matches
            //String idx_sparql_content = "create index idx_sparql_content on kiwinode ( coalesce(content,uri,anonid,'') text_pattern_ops);";
            String idx_sparql_content = "create index idx_sparql_content on kiwinode using hash( coalesce(content,uri,anonid,''));";
            connection.createStatement().executeUpdate("drop index if exists idx_sparql_content");
            connection.createStatement().executeUpdate(idx_sparql_content);

            // index for combined URI/ID matches (typical join in triple patterns)
            String idx_sparql_uri = "create unique index idx_sparql_uri on kiwinode (id, uri);";
            connection.createStatement().executeUpdate("drop index if exists idx_sparql_uri");
            connection.createStatement().executeUpdate(idx_sparql_uri);

            if(!connection.getAutoCommit())
                connection.commit();
        } catch (SQLException e) {
            log.error("Postgres: error while trying to create indexes!",e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("could not close SQL connection",e);
            }
        }

    }

    /**
     * Return the SQL Dialect used in this LMF instance. Returns null for unknown dialects.
     *
     * @return
     */
    private SQLDialect getSQLDialect() {
        String db_type = configurationService.getStringConfiguration("database.type","h2");

        SQLDialect dialect = null;
        if(db_type.equals("h2")) {
            dialect = SQLDialect.H2;
        } else if(db_type.equals("postgres")) {
            dialect = SQLDialect.POSTGRES;
        } else if(db_type.equals("oracle")) {
            dialect = SQLDialect.ORACLE;
        } else if(db_type.equals("mysql")) {
            dialect = SQLDialect.MYSQL;
        }
        return dialect;
    }

    /**
     * Get the unique identifier for the evaluation strategy (e.g. "sesame", "hql", "native")
     *
     * @return
     */
    @Override
    public String getIdentifier() {
        return "native";
    }

    /**
     * Return true if this evaluation strategy is available for use. Evaluation strategies can e.g. disable
     * themselves in case the underlying database system or persistence layer is not supported.
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        String db_type = configurationService.getStringConfiguration("database.type","h2");

        return "h2".equals(db_type) || "postgres".equals(db_type) || "mysql".equals(db_type) || "oracle".equals(db_type);
    }

    /**
     * Evaluates the tuple expression against the supplied triple source with the
     * specified set of variable bindings as input.
     *
     * @param expr     The Tuple Expression to evaluate
     * @param bindings The variables bindings to use for evaluating the expression, if
     *                 applicable.
     * @return A closeable iterator over the variable binding sets that match the
     *         tuple expression.
     */
    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final TupleExpr expr, final BindingSet bindings) throws QueryEvaluationException {


        String db_type = configurationService.getStringConfiguration("database.type","h2");

        SQLDialect dialect = null;
        if(db_type.equals("h2")) {
            dialect = SQLDialect.H2;
        } else if(db_type.equals("postgres")) {
            dialect = SQLDialect.POSTGRES;
        } else if(db_type.equals("oracle")) {
            dialect = SQLDialect.ORACLE;
        } else if(db_type.equals("mysql")) {
            dialect = SQLDialect.MYSQL;
        }

        SparqlToSQLMapper.CastType castType = SparqlToSQLMapper.CastType.STRICT;
        String castCfg = configurationService.getStringConfiguration("sparql.native.casttype","strict");
        if("strict".equals(castCfg)) {
            castType = SparqlToSQLMapper.CastType.STRICT;
        } else if("loose".equals(castCfg)) {
            castType = SparqlToSQLMapper.CastType.LOOSE;
        } else if("none".equals(castCfg)) {
            castType = SparqlToSQLMapper.CastType.NONE;
        }

        if(dialect != null) {

            // rearrange the SPARQL abstract syntax tree so things are more suitable for SQL

            // no unions as subselects in joins, we move the joins inside the union instead
            new UnionOptimizer().optimize(expr);

            final Connection connection = persistenceService.getJDBCConnection();

            try {
                connection.setAutoCommit(false);

                final SparqlToSQLMapper mapper = new SparqlToSQLMapper(connection,dialect,expr, castType);

                final Select query =  mapper.getQuery();

                if(log.isDebugEnabled() || configurationService.getBooleanConfiguration("sparql.native.logsql",true)) {
                    log.info("SQL query from SPARQL transformation:\n{}", query.getSQL());
                }


                final EntityManager em = persistenceService.getEntityManager();

                return new CloseableIteration<BindingSet, QueryEvaluationException>() {

                    private Cursor cursor = query.fetchLazy(50);

                    private UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES | UrlValidator.ALLOW_LOCAL_URLS);

                    // in case we need to split one SQL query row into several SPARQL query rows, we do it by creating
                    // a temporary iterator
                    private Iterator<BindingSet> splitBindings = null;

                    @Override
                    public void close() throws QueryEvaluationException {
                        if(!cursor.isClosed()) {
                            cursor.close();
                        }
                        try {
                            if(!connection.getAutoCommit()) {
                                connection.commit();
                            }
                            connection.close();
                        } catch (SQLException e) {
                            log.error("SQL exception while closing JDBC connection",e);
                        }
                        em.close();
                    }

                    @Override
                    public boolean hasNext() throws QueryEvaluationException {
                        return (splitBindings !=null && splitBindings.hasNext()) || cursor.hasNext();
                    }

                    @Override
                    public BindingSet next() throws QueryEvaluationException {
                        if(splitBindings != null) {
                            return splitBindings.next();
                        } else {
                            Record record = cursor.fetchOne();

                            // check if the result contains multiple SPARQL bindings in a single row or
                            // only one SPARQL row; we can do this by checking whether the first result starts
                            // with _multi
                            if(record.getField(0).getName().startsWith("_multi")) {
                                log.info("transforming single-row SQL result into multi-row SPARQL result");
                                List<BindingSet> results = new ArrayList<BindingSet>();
                                for(int i=1; true; i++) {
                                    MapBindingSet result = new MapBindingSet();
                                    for(String var : mapper.getProjectedVariables()) {
                                        if(var.startsWith("_multi") && var.endsWith("_"+i)) {
                                            Long nodeId = record.getValue(var, Long.class);

                                            if(nodeId != null) {
                                                Value value = em.find(KiWiNode.class, nodeId);

                                                result.addBinding(var.substring(var.indexOf('_',1)+1,var.lastIndexOf('_')),value);
                                            }
                                        }
                                    }
                                    for(Map.Entry<String,Class> ext : mapper.getExtensionVariables().entrySet()) {
                                        String var = ext.getKey();
                                        if(var.startsWith("_multi") && var.endsWith("_"+i)) {
                                            Object val = record.getValue(ext.getKey(),ext.getValue());

                                            // this is truly a hack: we check whether the string is a URI, and if yes create a URI resource...
                                            // it would be better to carry over this information from the value constants
                                            if(urlValidator.isValid(val.toString())) {
                                                URI value = new URIImpl(val.toString());
                                                result.addBinding(var.substring(var.indexOf('_',1)+1,var.lastIndexOf('_')),value);
                                            } else {
                                                String type = LiteralCommons.getXSDType(ext.getValue());

                                                // we only create an in-memory representation of the value, the LMF methods
                                                // would automatically persist it, so we create a Sesame value
                                                Value value = new LiteralImpl(val.toString(),sesameService.getValueFactory().createURI(type));
                                                result.addBinding(var.substring(var.indexOf('_',1)+1,var.lastIndexOf('_')),value);
                                            }
                                        }
                                    }
                                    if(result.size() == 0) {
                                        break;
                                    } else {
                                        results.add(result);
                                    }
                                }
                                em.clear();
                                splitBindings = results.iterator();
                                return splitBindings.next();
                            } else {

                                MapBindingSet result = new MapBindingSet();
                                for(String var : mapper.getProjectedVariables()) {
                                    Long nodeId = record.getValue(var, Long.class);

                                    if(nodeId != null) {
                                        Value value = em.find(KiWiNode.class, nodeId);
                                        result.addBinding(var,value);
                                    }
                                }
                                for(Map.Entry<String,Class> ext : mapper.getExtensionVariables().entrySet()) {
                                    Object val = record.getValue(ext.getKey(),ext.getValue());

                                    // this is truly a hack: we check whether the string is a URI, and if yes create a URI resource...
                                    // it would be better to carry over this information from the value constants
                                    if(urlValidator.isValid(val.toString())) {
                                        URI value = new URIImpl(val.toString());
                                        result.addBinding(ext.getKey(),value);
                                    } else {
                                        String type = LiteralCommons.getXSDType(ext.getValue());

                                        // we only create an in-memory representation of the value, the LMF methods
                                        // would automatically persist it, so we create a Sesame value
                                        Value value = new LiteralImpl(val.toString(),sesameService.getValueFactory().createURI(type));
                                        result.addBinding(ext.getKey(),value);
                                    }
                                }

                                em.clear();

                                return result;
                            }
                        }
                    }

                    @Override
                    public void remove() throws QueryEvaluationException {
                        throw new UnsupportedOperationException("removing not supported");
                    }
                };

            } catch(Exception ex) {
                log.error("exception while translating SPARQL query",ex);
                log.error("abstract query tree was: {}",expr);
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("error while trying to close JDBC connection",e);
                }
            }

            throw new UnsupportedOperationException("not yet implemented");
        }

        throw new UnsupportedOperationException("the database "+db_type+" is not yet supported for SPARQL->SQL mapping");
    }

    /**
     * Gets the value of this expression.
     *
     * @param bindings The variables bindings to use for evaluating the expression, if
     *                 applicable.
     * @return The Value that this expression evaluates to, or <tt>null</tt> if
     *         the expression could not be evaluated.
     */
    @Override
    public Value evaluate(ValueExpr expr, BindingSet bindings) throws ValueExprEvaluationException, QueryEvaluationException {
        String db_type = configurationService.getStringConfiguration("database.type","h2");
 /*
        SQLDialect dialect = null;
        if(db_type.equals("h2")) {
            dialect = SQLDialect.H2;
        } else if(db_type.equals("postgres")) {
            dialect = SQLDialect.POSTGRES;
        } else if(db_type.equals("oracle")) {
            dialect = SQLDialect.ORACLE;
        } else if(db_type.equals("mysql")) {
            dialect = SQLDialect.MYSQL;
        }

        SparqlToSQLMapper.CastType castType = SparqlToSQLMapper.CastType.STRICT;
        String castCfg = configurationService.getStringConfiguration("sparql.native.casttype","strict");
        if("strict".equals(castCfg)) {
            castType = SparqlToSQLMapper.CastType.STRICT;
        } else if("loose".equals(castCfg)) {
            castType = SparqlToSQLMapper.CastType.LOOSE;
        } else if("none".equals(castCfg)) {
            castType = SparqlToSQLMapper.CastType.NONE;
        }

        if(dialect != null) {
            final Connection connection = persistenceService.getJDBCConnection();

            try {
                final SparqlToSQLMapper mapper = new SparqlToSQLMapper(connection,dialect,expr,castType);

                final Select query =  mapper.getQuery();

                log.info("SQL query would be: {}", query.getSQL());


                Object value = query.fetchOne(0);


                connection.commit();
                connection.close();

                throw new UnsupportedOperationException("not yet implemented");
            } catch (SQLException e) {
                log.error("error while evaluating SQL query",e);
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("error while trying to close JDBC connection",e);
                }
            }

        }
*/
        throw new UnsupportedOperationException("the database "+db_type+" is not yet supported for SPARQL->SQL mapping");
    }

    /**
     * Evaluates the boolean expression on the supplied TripleSource object.
     *
     * @param bindings The variables bindings to use for evaluating the expression, if
     *                 applicable.
     * @return The result of the evaluation.
     * @throws org.openrdf.query.algebra.evaluation.ValueExprEvaluationException
     *          If the value expression could not be evaluated, for example when
     *          comparing two incompatible operands. When thrown, the result of
     *          the boolean expression is neither <tt>true</tt> nor
     *          <tt>false</tt>, but unknown.
     */
    @Override
    public boolean isTrue(ValueExpr expr, BindingSet bindings) throws ValueExprEvaluationException, QueryEvaluationException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }



}
