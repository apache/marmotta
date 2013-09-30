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
package org.apache.marmotta.platform.sparql.services.evaluation.sql;

import org.apache.marmotta.platform.sparql.model.Tables;
import org.apache.marmotta.platform.sparql.model.tables.TNodes;
import org.apache.marmotta.platform.sparql.model.tables.TTriples;
import org.apache.marmotta.platform.sparql.model.tables.records.RNodes;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.util.DateUtils;
import at.newmedialab.sesame.commons.util.HashUtils;
import com.google.common.collect.Sets;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.ResourceService;
import org.apache.marmotta.platform.core.model.rdf.KiWiNode;
import org.apache.marmotta.platform.core.model.rdf.KiWiUriResource;
import org.apache.marmotta.platform.core.util.KiWiContext;
import org.jooq.*;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.StatementType;
import org.jooq.impl.Factory;
import org.jooq.impl.SQLDataType;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.CalendarLiteralImpl;
import org.openrdf.model.impl.DecimalLiteralImpl;
import org.openrdf.model.impl.IntegerLiteralImpl;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.sparql.ast.VisitorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class implementing the query model visitor to translate a SPARQL algebra model to a SQL query using the JOOQ
 * Java to SQL mapper. Takes a JDBC database connection and a SQLDialect as argument, based on which the
 * translation takes place. The resulting SQL query can be retrieved by calling the getQuery() method.
 * <p/>
 * The SPARQL->SQL mapper translates the major SPARQL constructs as follows:
 * <ul>
 *     <li>a triple pattern is translated into a join of the TRIPLES table with the subject,
 *         property, object, and optionally context nodes; each variable occurring in the pattern
 *         represents a separate query to the KIWINODES table; variables with the same name
 *         are reused to ensure SPARQL joins to be translated into SQL JOINs</li>
 *     <li>a JOIN between triple patterns is translated into an SQL INNER JOIN, using the variables used
 *         in the triple patterns as JOIN conditions</li>
 *     <li>an OPTIONAL between triple patterns is translated into an SQL LEFT OUTER JOIN, using the
 *         variables occurring in the optional pattern as join conditions</li>
 *     <li>a UNION is translated into an SQL UNION; since SPARQL untions occur inside
 *         the WHERE part of the query, the UnionOptimizer moves them out to the top level
 *         so the UNION is always calculated properly over SQL queries</li>
 *     <li>INTERSECTION and DIFFERENCE are translated similar to UNION queries to INTERSECTION and EXCEPT
 *         in SQL</li>
 *     <li>variables are named queries to the KIWINODES table; in case the value of a variable is
 *         needed, the getValue() method below translates the table representation into either the
 *         content, uri, or anonId fields of the table; in case the variable is returned by the
 *         projection, the mapping translates it into the ID field of the table to be able to
 *         load the complete RDF node; specialised methods are implemented for accessing the
 *         double, integer, or date content of a variable</li>
 *     <li>SPARQL functions and operators are translated as far as possible directly into their
 *         SQL counterparts; for constructs where a SQL equivalent does not exist, an exception
 *         is thrown</li>
 * </ul>
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SparqlToSQLMapper  {

    private static Logger log = LoggerFactory.getLogger(SparqlToSQLMapper.class);

    // TODO: implement language tag matches using this regular expression
    static final String langtag_ex =
            "(\\A[xX]([\\x2d]\\p{Alnum}{1,8})*\\z)"
                    + "|(((\\A\\p{Alpha}{2,8}(?=\\x2d|\\z)){1}"
                    + "(([\\x2d]\\p{Alpha}{3})(?=\\x2d|\\z)){0,3}"
                    + "([\\x2d]\\p{Alpha}{4}(?=\\x2d|\\z))?"
                    + "([\\x2d](\\p{Alpha}{2}|\\d{3})(?=\\x2d|\\z))?"
                    + "([\\x2d](\\d\\p{Alnum}{3}|\\p{Alnum}{5,8})(?=\\x2d|\\z))*)"
                    + "(([\\x2d]([a-wyzA-WYZ](?=\\x2d))([\\x2d](\\p{Alnum}{2,8})+)*))*"
                    + "([\\x2d][xX]([\\x2d]\\p{Alnum}{1,8})*)?)\\z";


    private Factory create;

    /** jOOQ representation of the triples table */
    private TTriples T_TRIPLES;

    /** jOOQ representation of the nodes table */
    private TNodes T_NODES;

    /** jOOQ representation of the namespaces table */
    private Table<Record> T_NAMESPACES;


    private SQLDialect dialect;

    // keep track of the variables that have so far been used in the query;
    // currentVariables is used by sub queries
    private Map<String,TNodes> variables;
    private Map<String,TNodes> currentVariables;

    // map from URI resources to nodes table, to avoid unnecessary joins
    private Map<String,TNodes> uriNodes;


    // keep track of SPARQL extension values used in the query; extension values are bound e.g. to
    // function results
    private Map<String,Field> extensions;

    // count current triple; used for naming triple patterns
    private int tripleCounter = 0;

    // the set of variables that are actually projected by this query
    private Set<String> projectedVariables;

    // the set of extensions that are projected by this query (together with type information for proper casting)
    private HashMap<String,Class> extensionVariables;


    // collect mappings from internal variable name to result variable name; filled by Projection
    private Map<String,String> variableMappings;
    private Map<String,String> revVariableMappings;

    // fields used for sorting the results
    private Map<String,Field> sortFields;
    private Map<String,Set<String>> sortDependencies;


    // store variables that can only be used as resources; for such variables, we can detect the getValue
    // method, because in this case we only need to return the URI as string value
    private Set<String> resourceVariables;

    private Select query;

    private CastType typeCast;

    // collection of fields on which to group the query; collected by group(), used by projection() ...
    private Set<String> groupBy;

    private ResourceService resourceService;

    private ConfigurationService configurationService;

    public SparqlToSQLMapper(Connection connection, SQLDialect dialect, CastType typeCast) {
        create = new Factory(connection,dialect);
        create.getSettings().setRenderFormatted(true);
        create.getSettings().setRenderNameStyle(RenderNameStyle.LOWER);
        create.getSettings().setStatementType(StatementType.STATIC_STATEMENT);

        this.dialect = dialect;

        T_TRIPLES    = Tables.TRIPLES;
        T_NODES      = Tables.KIWINODE;
        T_NAMESPACES = create.table("KIWINAMESPACE");

        variables = new HashMap<String, TNodes>();
        currentVariables = new HashMap<String, TNodes>();
        projectedVariables = new HashSet<String>();
        extensionVariables = new HashMap<String, Class>();

        extensions = new HashMap<String, Field>();

        variableMappings = new HashMap<String, String>();
        revVariableMappings = new HashMap<String, String>();
        sortFields = new HashMap<String, Field>();
        sortDependencies = new HashMap<String, Set<String>>();

        resourceVariables = new HashSet<String>();

        uriNodes = new HashMap<String, TNodes>();

        groupBy = new HashSet<String>();

        this.typeCast = typeCast;

        // require some beans to get additional information
        this.resourceService = KiWiContext.getInstance(ResourceService.class);
        this.configurationService = KiWiContext.getInstance(ConfigurationService.class);
    }

    public SparqlToSQLMapper(Connection connection, SQLDialect dialect, TupleExpr queryRoot, CastType typeCast) {
        this(connection,dialect, typeCast);

        query = meetTupleExpression(queryRoot, create.selectQuery(), new HashSet<Condition>(), new HashSet<String>());
    }

    public SparqlToSQLMapper(Connection connection, SQLDialect dialect, ValueExpr queryRoot, CastType typeCast) {
        this(connection,dialect, typeCast);

        query = create.select((Field)meetValueExpression(queryRoot));
    }



    public Select getQuery() {
        return query;
    }


    public Set<String> getProjectedVariables() {
        return projectedVariables;
    }

    public HashMap<String, Class> getExtensionVariables() {
        return extensionVariables;
    }

    public Condition meet(And node)  {
        QueryPart leftPart = meetValueExpression(node.getLeftArg());
        QueryPart rightPart = meetValueExpression(node.getRightArg());

        if(! (leftPart instanceof Condition) || !(rightPart instanceof Condition)) {
            throw new IllegalStateException("the left or right part of the AND condition are not boolean: "+node);
        }

        return ((Condition)leftPart).and((Condition)rightPart);

    }


    public SelectQuery arbitraryLengthPath(ArbitraryLengthPath node, SelectQuery query)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }


    public Field avg(Avg node)  {
        QueryPart part = meetValueExpression(node.getArg());


        return getDoubleValue(part, true).avg();
    }


    public SelectQuery bindingSetAssignment(BindingSetAssignment node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }



    public QueryPart bound(Bound node)  {
        QueryPart part = meetValueExpression(node.getArg());

        // variable
        if(part instanceof TNodes) {
            return create.condition(part + ".id is not null") ;
        } else {
            throw new IllegalArgumentException("NOT NULL can only be computed for the node table; type was: "+part.getClass().getName());
        }
    }



    public Field coalesce(Coalesce node)  {

        /*
        if(node.getArguments().size() > 0) {
            Field part1;
            Field[] parts2_n = new Field[node.getArguments().size()-1];
            for(int i=0; i<node.getArguments().size(); i++) {
                QueryPart part = meetValueExpression(node.getArguments().get(i));
                if(part instanceof Table && ((Table) part).getName().equals(T_NODES.getName())) {
                    if(i == 0) {
                        part1 = ((Table)part).getField("");
                    } else {
                        parts2_n[i-1] = (Table)part;
                    }
                } else {
                    throw new IllegalArgumentException("COALESCE can only be computed for the node table; type was: "+part.getClass().getName());
                }

            }
            return create.coalesce()
        }
        */


        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }


    public Condition compare(Compare node)  {
        QueryPart leftArg = meetValueExpression(node.getLeftArg());
        QueryPart rightArg = meetValueExpression(node.getRightArg());

        Field left, right;

        // type negotiation:
        // 1. if both are variables, casting should take place based on the type field, or if no type is given, we use
        // the value returned by getValue (might be the URI)
        if(leftArg instanceof TNodes && rightArg instanceof TNodes) {
            TNodes tleft  = (TNodes) leftArg;
            TNodes tright = (TNodes) rightArg;

            switch (node.getOperator()) {
                case EQ:
                    return create.decode().when(tleft.NODETYPE.equal("DOUBLE").and(tright.NODETYPE.equal("DOUBLE")), tleft.DOUBLECONTENT.equal(tright.DOUBLECONTENT))
                            .when(tleft.NODETYPE.equal("INTEGER").and(tright.NODETYPE.equal("INTEGER")), tleft.INTCONTENT.equal(tright.INTCONTENT))
                            .when(tleft.NODETYPE.equal("STRING").and(tright.NODETYPE.equal("STRING")),tleft.CONTENT.equal(tright.CONTENT))
                            .when(tleft.NODETYPE.equal("DATE").and(tright.NODETYPE.equal("DATE")),tleft.DATECONTENT.equal(tright.DATECONTENT))
                            .when(tleft.NODETYPE.equal("URI").and(tright.NODETYPE.equal("URI")),tleft.URI.equal(tright.URI))
                            .when(tleft.NODETYPE.equal("ANON").and(tright.NODETYPE.equal("ANON")),tleft.ID.equal(tright.ID))
                            .otherwise(create.falseCondition()).isTrue();
                case GE:
                    return create.decode().when(tleft.NODETYPE.equal("DOUBLE").and(tright.NODETYPE.equal("DOUBLE")), tleft.DOUBLECONTENT.greaterOrEqual(tright.DOUBLECONTENT))
                            .when(tleft.NODETYPE.equal("INTEGER").and(tright.NODETYPE.equal("INTEGER")), tleft.INTCONTENT.greaterOrEqual(tright.INTCONTENT))
                            .when(tleft.NODETYPE.equal("DATE").and(tright.NODETYPE.equal("DATE")),tleft.DATECONTENT.greaterOrEqual(tright.DATECONTENT))
                            .when(tleft.NODETYPE.equal("STRING").and(tright.NODETYPE.equal("STRING")),tleft.CONTENT.greaterOrEqual(tright.CONTENT))
                            .otherwise(create.falseCondition()).isTrue();
                case GT:
                    return create.decode().when(tleft.NODETYPE.equal("DOUBLE").and(tright.NODETYPE.equal("DOUBLE")), tleft.DOUBLECONTENT.greaterThan(tright.DOUBLECONTENT))
                            .when(tleft.NODETYPE.equal("INTEGER").and(tright.NODETYPE.equal("INTEGER")), tleft.INTCONTENT.greaterThan(tright.INTCONTENT))
                            .when(tleft.NODETYPE.equal("STRING").and(tright.NODETYPE.equal("STRING")),tleft.CONTENT.greaterThan(tright.CONTENT))
                            .when(tleft.NODETYPE.equal("DATE").and(tright.NODETYPE.equal("DATE")),tleft.DATECONTENT.greaterThan(tright.DATECONTENT))
                            .otherwise(create.falseCondition()).isTrue();
                case LE:
                    return create.decode().when(tleft.NODETYPE.equal("DOUBLE").and(tright.NODETYPE.equal("DOUBLE")), tleft.DOUBLECONTENT.lessOrEqual(tright.DOUBLECONTENT))
                            .when(tleft.NODETYPE.equal("INTEGER").and(tright.NODETYPE.equal("INTEGER")), tleft.INTCONTENT.lessOrEqual(tright.INTCONTENT))
                            .when(tleft.NODETYPE.equal("STRING").and(tright.NODETYPE.equal("STRING")),tleft.CONTENT.lessOrEqual(tright.CONTENT))
                            .when(tleft.NODETYPE.equal("DATE").and(tright.NODETYPE.equal("DATE")),tleft.DATECONTENT.lessOrEqual(tright.DATECONTENT))
                            .otherwise(create.falseCondition()).isTrue();
                case LT:
                    return create.decode().when(tleft.NODETYPE.equal("DOUBLE").and(tright.NODETYPE.equal("DOUBLE")), tleft.DOUBLECONTENT.lessThan(tright.DOUBLECONTENT))
                            .when(tleft.NODETYPE.equal("INTEGER").and(tright.NODETYPE.equal("INTEGER")), tleft.INTCONTENT.lessThan(tright.INTCONTENT))
                            .when(tleft.NODETYPE.equal("STRING").and(tright.NODETYPE.equal("STRING")),tleft.CONTENT.lessThan(tright.CONTENT))
                            .when(tleft.NODETYPE.equal("DATE").and(tright.NODETYPE.equal("DATE")),tleft.DATECONTENT.lessThan(tright.DATECONTENT))
                            .otherwise(create.falseCondition()).isTrue();
                case NE:
                    return create.decode()  .when(tleft.NODETYPE.equal("DOUBLE").and(tright.NODETYPE.equal("DOUBLE")), tleft.DOUBLECONTENT.notEqual(tright.DOUBLECONTENT))
                            .when(tleft.NODETYPE.equal("INTEGER").and(tright.NODETYPE.equal("INTEGER")), tleft.INTCONTENT.notEqual(tright.INTCONTENT))
                            .when(tleft.NODETYPE.equal("STRING").and(tright.NODETYPE.equal("STRING")),tleft.CONTENT.notEqual(tright.CONTENT))
                            .when(tleft.NODETYPE.equal("URI").and(tright.NODETYPE.equal("URI")),tleft.URI.notEqual(tright.URI))
                            .when(tleft.NODETYPE.equal("ANON").and(tright.NODETYPE.equal("ANON")),tleft.ID.notEqual(tright.ID))
                            .when(tleft.NODETYPE.equal("DATE").and(tright.NODETYPE.equal("DATE")),tleft.DATECONTENT.notEqual(tright.DATECONTENT))
                            .otherwise(create.falseCondition()).isTrue();

            }

            throw new IllegalArgumentException("unknown operator for comparison: "+node.getOperator());

            // 2. if only one is a variable, we take the type of the other argument and cast the variable to either string or
            // double for comparison
        } else if(leftArg instanceof TNodes && rightArg instanceof Field) {
            if(((Field) rightArg).getType().equals(Long.class)) {
                left = getIntegerValue(leftArg, typeCast == CastType.STRICT);
                right = getIntegerValue(rightArg, typeCast == CastType.STRICT);
            } else if(((Field) rightArg).getType().equals(Double.class)) {
                left = getDoubleValue(leftArg, typeCast == CastType.STRICT);
                right = getDoubleValue(rightArg, typeCast == CastType.STRICT);
            } else if(((Field) rightArg).getType().equals(Timestamp.class)) {
                left = getDateValue(leftArg, typeCast == CastType.STRICT);
                right = getDateValue(rightArg, typeCast == CastType.STRICT);
            } else {
                left = getValue(leftArg, typeCast == CastType.STRICT);
                right = getValue(rightArg, typeCast == CastType.STRICT);
            }
        } else if(rightArg instanceof TNodes && leftArg instanceof Field) {
            if(((Field) leftArg).getType().equals(Long.class)) {
                left = getIntegerValue(leftArg, typeCast == CastType.STRICT);
                right = getIntegerValue(rightArg, typeCast == CastType.STRICT);
            } else if(((Field) leftArg).getType().equals(Double.class)) {
                left = getDoubleValue(leftArg, typeCast == CastType.STRICT);
                right = getDoubleValue(rightArg, typeCast == CastType.STRICT);
            } else if(((Field) leftArg).getType().equals(Timestamp.class)) {
                left = getDateValue(leftArg, typeCast == CastType.STRICT);
                right = getDateValue(rightArg, typeCast == CastType.STRICT);
            } else {
                left = getValue(leftArg, typeCast == CastType.STRICT);
                right = getValue(rightArg, typeCast == CastType.STRICT);
            }
        } else {
            left   = getValue(leftArg, true);
            right  = getValue(rightArg, true);
        }



        // TODO: here we could possibly also work with getAlternativeValues, but it is unclear to me what comparison with null fields will yield!
        // maybe we need sth like (x is not null AND x < y)


        switch(node.getOperator()) {
            case EQ:
                return left.equal(right);
            case GE:
                return left.greaterOrEqual(right);
            case GT:
                return left.greaterThan(right);
            case LE:
                return left.lessOrEqual(right);
            case LT:
                return left.lessThan(right);
            case NE:
                return left.notEqual(right);
        }

        throw new IllegalArgumentException("unknown operator for comparison: "+node.getOperator());
    }


    public QueryPart compareAll(CompareAll node)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }


    public QueryPart compareAny(CompareAny node)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }



    public Field count(Count node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof TNodes) {
            return ((TNodes)part).ID.count();

        } else {
            throw new IllegalArgumentException("COUNT is only supported for variables, was: "+part.getClass().getName());
        }
    }


    public Field datatype(Datatype node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof TNodes) {
            return ((TNodes)part).TYPE;

        } else {
            throw new IllegalArgumentException("DATATYPE is only supported for variables, was: "+part.getClass().getName());
        }
    }



    public List<JoinTable> difference(Difference node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections,Collection<Table> tables)  {
        return merge(node,query,MergeType.DIFFERENCE, conditions,  projections, tables);
    }


    public Select distinct(Distinct node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {

        Select subQuery = meetTupleExpression(node.getArg(), query,  conditions,  projections);

        if(subQuery instanceof SelectQuery) {
            ((SelectQuery) subQuery).setDistinct(true);
            return subQuery;
        } else {
            return create.selectDistinct().from(subQuery);
        }

        /*
        try {
            // distinct and order by is not possible in SQL
            if(! (new HasOrderBy(node).isTrue())) {
                query.setDistinct(true);
                return meetTupleExpression(node.getArg(), query, type);
            } else {
                // we have to do a subquery first and then select the distinct values
                Select subquery = meetTupleExpression(node.getArg(), query, type);

                return create.selectDistinct().from(subquery);
            }
        } catch (VisitorException e) {
            return meetTupleExpression(node.getArg(),query,type);
        }
        */
    }


    public SelectQuery emptySet(EmptySet node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {
        query.addConditions(create.falseCondition());
        return query;
    }


    public Condition exists(Exists node)  {
        // save previous state
        Map<String,TNodes> oldVars = currentVariables;

        currentVariables = new HashMap<String, TNodes>(oldVars);   // copy over old variable mappings so modifications have no influence to the rest of the evaluation
        SelectQuery subQuery = create.selectQuery();
        Set<Condition> conditions = new HashSet<Condition>();
        List<Table> tables = new ArrayList<Table>();

        Table from = null;
        for(JoinTable tbl : meetTableLike(node.getSubQuery(),subQuery, conditions, new HashSet<String>(), tables)) {
            if(from == null) {
                from = tbl.getTable();
                subQuery.addConditions(tbl.getConditions());
            } else {
                from = from.join(tbl.getTable()).on(tbl.getConditions().toArray(new Condition[0]));
            }
        }
        subQuery.addFrom(from);
        subQuery.addConditions(conditions);
        if(tables.size() > 0 && tables.get(0) instanceof TTriples) {
            subQuery.addSelect(((TTriples)tables.get(0)).ID.as("_singlevalue"));
        }
        subQuery.addLimit(1);

        // reload previous state
        currentVariables = oldVars;

        return create.exists(subQuery);
    }


    public List<JoinTable> extension(Extension node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections, Collection<Table> tables)  {
        List<JoinTable> select;
        if(node.getArg() instanceof SingletonSet) {
            // TODO: optimization would be to not run against the database at all ...
            select = new ArrayList<JoinTable>();
        } else {
            select = meetTableLike(node.getArg(), query, conditions, projections, tables);
        }

        for(ExtensionElem elem : node.getElements()) {
            extensionElem(elem);
        }

        return select;
    }

    public void extensionElem(ExtensionElem elem) {
        if(elem.getExpr() instanceof Var) {
            // do nothing, declaration
        } else {
            extensions.put(elem.getName().replace("-","_"), getValue(meetValueExpression(elem.getExpr()), true));
        }
    }


    public List<JoinTable> filter(Filter node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections, Collection<Table> tables)  {
        List<JoinTable> tableList = meetTableLike(node.getArg(), query, conditions, projections, tables); // ensure the tables are evaluated before the conditions

        QueryPart condition = meetValueExpression(node.getCondition());
        if(condition instanceof Condition) {
            conditions.add((Condition)condition);
        } else {
            throw new IllegalStateException("the transformation of the value expression did not result in a Condition");
        }



        return tableList;
    }


    public QueryPart functionCall(FunctionCall node)  {
        if(node.getURI().equals(Namespaces.NS_XSD+"double") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getDoubleValue(arg, true);
        } else if(node.getURI().equals(Namespaces.NS_XSD+"float") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getDoubleValue(arg, true);
        } else if(node.getURI().equals(Namespaces.NS_XSD+"decimal") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getIntegerValue(arg, true);
        } else if(node.getURI().equals(Namespaces.NS_XSD+"integer") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getIntegerValue(arg, true);
        } else if(node.getURI().equals(Namespaces.NS_XSD+"boolean") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getValue(arg, true).cast(SQLDataType.BOOLEAN);
        } else if(node.getURI().equals(Namespaces.NS_XSD+"string") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getValue(arg, true);
        } else if(node.getURI().equals(Namespaces.NS_XSD+"dateTime") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getDateValue(arg, true);
        } else if(node.getURI().equalsIgnoreCase("ABS") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getDoubleValue(arg,typeCast == CastType.STRICT).abs();
        } else if(node.getURI().equalsIgnoreCase("CEIL") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getDoubleValue(arg,typeCast == CastType.STRICT).ceil();
        } else if(node.getURI().equalsIgnoreCase("FLOOR") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getDoubleValue(arg,typeCast == CastType.STRICT).floor();
        } else if(node.getURI().equalsIgnoreCase("RAND") && node.getArgs().size() == 0) {
            return create.rand();
        } else if(node.getURI().equalsIgnoreCase("ROUND") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return getDoubleValue(arg,typeCast == CastType.STRICT).round();
        } else if(node.getURI().equalsIgnoreCase("DAY") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.extract(getDateValue(arg,true),DatePart.DAY);
        } else if(node.getURI().equalsIgnoreCase("HOURS") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.extract(getDateValue(arg,true),DatePart.HOUR);
        } else if(node.getURI().equalsIgnoreCase("MINUTES") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.extract(getDateValue(arg,true),DatePart.MINUTE);
        } else if(node.getURI().equalsIgnoreCase("MONTH") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.extract(getDateValue(arg,true),DatePart.MONTH);
        } else if(node.getURI().equalsIgnoreCase("NOW") && node.getArgs().size() == 0) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.currentTimestamp();
        } else if(node.getURI().equalsIgnoreCase("SECONDS") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.extract(getDateValue(arg,true),DatePart.SECOND);
        } else if(node.getURI().equalsIgnoreCase("YEAR") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.extract(getDateValue(arg,true),DatePart.YEAR);
        } else if(node.getURI().equalsIgnoreCase("CONCAT") && node.getArgs().size() >= 1) {
            ArrayList<Field> fields = new ArrayList<Field>(node.getArgs().size());
            for(ValueExpr expr : node.getArgs()) {
                Field result = getValue(meetValueExpression(node.getArgs().get(0)),true);
                fields.add(result);
            }

            return create.concat(fields.toArray(new Field[0]));
        } else if(node.getURI().equalsIgnoreCase("STRLEN") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.length(getValue(arg, typeCast == CastType.STRICT));
        } else if(node.getURI().equalsIgnoreCase("SUBSTR") && node.getArgs().size() >= 2) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));
            Field<Long> startPos = getIntegerValue(meetValueExpression(node.getArgs().get(1)),true);

            if(node.getArgs().size() == 3) {
                Field<Long> endPos = getIntegerValue(meetValueExpression(node.getArgs().get(2)),true);
                return create.substring(getValue(arg,typeCast == CastType.STRICT),startPos,endPos);
            } else {
                return create.substring(getValue(arg, typeCast == CastType.STRICT), startPos);
            }
        } else if(node.getURI().equalsIgnoreCase("UCASE") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.upper(getValue(arg, typeCast == CastType.STRICT));
        } else if(node.getURI().equalsIgnoreCase("LCASE") && node.getArgs().size() == 1) {
            QueryPart arg = meetValueExpression(node.getArgs().get(0));

            return create.lower(getValue(arg, typeCast == CastType.STRICT));
        } else if(node.getURI().equalsIgnoreCase("STRSTARTS") && node.getArgs().size() == 2) {
            QueryPart arg1 = meetValueExpression(node.getArgs().get(0));
            QueryPart arg2 = meetValueExpression(node.getArgs().get(1));

            return getValue(arg1,typeCast == CastType.STRICT).startsWith(getValue(arg2, typeCast == CastType.STRICT));
        } else if(node.getURI().equalsIgnoreCase("STRENDS") && node.getArgs().size() == 2) {
            QueryPart arg1 = meetValueExpression(node.getArgs().get(0));
            QueryPart arg2 = meetValueExpression(node.getArgs().get(1));

            return getValue(arg1,typeCast == CastType.STRICT).endsWith(getValue(arg2, typeCast == CastType.STRICT));
        } else if(node.getURI().equalsIgnoreCase("STRENDS") && node.getArgs().size() == 2) {
            QueryPart arg1 = meetValueExpression(node.getArgs().get(0));
            QueryPart arg2 = meetValueExpression(node.getArgs().get(1));

            return getValue(arg1,typeCast == CastType.STRICT).contains(getValue(arg2, typeCast == CastType.STRICT));
        } else if(node.getURI().equalsIgnoreCase("STRBEFORE") && node.getArgs().size() == 2) {
            QueryPart arg1 = meetValueExpression(node.getArgs().get(0));
            QueryPart arg2 = meetValueExpression(node.getArgs().get(1));

            return create.substring(getValue(arg1, typeCast == CastType.STRICT), create.val(0), create.position(getValue(arg1, typeCast == CastType.STRICT), getValue(arg2, typeCast == CastType.STRICT)));
        } else if(node.getURI().equalsIgnoreCase("STRAFTER") && node.getArgs().size() == 2) {
            QueryPart arg1 = meetValueExpression(node.getArgs().get(0));
            QueryPart arg2 = meetValueExpression(node.getArgs().get(1));

            Field val1 = getValue(arg1,typeCast == CastType.STRICT);
            Field val2 = getValue(arg2,typeCast == CastType.STRICT);

            return create.substring(val1,create.position(val1,val2).add(create.length(val2)),create.length(val1));
        } else if(node.getURI().equalsIgnoreCase("MD5") && node.getArgs().size() == 1) {
            if(dialect == SQLDialect.MYSQL || dialect == SQLDialect.POSTGRES) {
                QueryPart arg = meetValueExpression(node.getArgs().get(0));

                return create.field("MD5({0})",String.class,getValue(arg,typeCast == CastType.STRICT));
            } else {
                throw new UnsupportedOperationException("computing MD5 sum is only supported by PostgreSQL and MySQL backends");
            }
        }


        throw new UnsupportedOperationException("function call "+node.getURI()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }


    public List<JoinTable> group(Group node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections, Collection<Table> tables)  {
         for(String elem : node.getGroupBindingNames()) {
            groupBy.add(elem);
        }
        return meetTableLike(node.getArg(), query, conditions, projections, tables);
    }


    public QueryPart groupConcat(GroupConcat node)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }



    public Field ifCond(If node)  {
        QueryPart condition = meetValueExpression(node.getCondition());
        QueryPart result    = meetValueExpression(node.getResult());
        QueryPart alternative = meetValueExpression(node.getAlternative());

        if(condition instanceof Condition) {
            Field resultValue = null, alternativeValue = null;
            if(result instanceof Table) {
                resultValue = getValue((Table)result, true);
            } else if(result instanceof Field) {
                resultValue = (Field) result;
            }
            if(alternative instanceof Table) {
                alternativeValue = getValue((Table)alternative, true);
            } else if(alternative instanceof Field) {
                alternativeValue = (Field)alternative;
            }
            if(resultValue != null && alternativeValue != null) {
                return create.decode().when((Condition)condition, resultValue).otherwise(alternativeValue);
            }
        }

        throw new IllegalArgumentException("incorrect argument types for IF .. THEN .. ELSE");
    }


    public QueryPart inCond(In node)  {
        // TODO: how to deal with values vs. ids of a subselect?
        /*
        QueryPart subquery = meetTupleExpression(node.getSubQuery());
        QueryPart value    = meetValueExpression(node.getArg());
        */
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }



    public List<JoinTable> intersection(Intersection node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections,Collection<Table> tables)  {
        return merge(node,query,MergeType.INTERSECTION, conditions,  projections, tables);
    }


    public QueryPart iriFunction(IRIFunction node)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }


    public Condition isBNode(IsBNode node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof TNodes) {
            Field<String> nodetype = ((TNodes)part).NODETYPE;

            return nodetype.equal("ANON");
        } else {
            throw new IllegalArgumentException("argument of IsBNode must be a variable");
        }
    }


    public Condition isLiteral(IsLiteral node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof Table) {
            Field<String> nodetype = ((TNodes)part).NODETYPE;

            return nodetype.equal("STRING").or(nodetype.equal("INTEGER")).or(nodetype.equal("DOUBLE"));
        } else if(part instanceof Field) {
            return create.trueCondition();
        } else {
            throw new IllegalArgumentException("argument of IsLiteral must be a variable or a constant");
        }
    }


    public Condition isNumeric(IsNumeric node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof Table) {
            Field<String> nodetype = ((TNodes)part).NODETYPE;

            return nodetype.equal("INTEGER").or(nodetype.equal("DOUBLE"));
        } else {
            throw new IllegalArgumentException("argument of IsNumeric must be a variable");
        }
    }


    public Condition isResource(IsResource node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof Table) {
            Field<String> nodetype = ((TNodes)part).NODETYPE;

            return nodetype.equal("URI").or(nodetype.equal("ANON"));
        } else {
            throw new IllegalArgumentException("argument of IsResource must be a variable");
        }
    }


    public Condition isURI(IsURI node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof Table) {
            Field<String> nodetype = ((TNodes)part).NODETYPE;

            return nodetype.equal("URI");
        } else {
            throw new IllegalArgumentException("argument of IsURI must be a variable");
        }
    }


    public List<JoinTable> join(Join node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections, Collection<Table> tables)  {

        List<JoinTable> resultTables = new ArrayList<JoinTable>();

        resultTables.addAll(meetTableLike(node.getLeftArg(), query, conditions,  projections, tables));
        resultTables.addAll(meetTableLike(node.getRightArg(), query, conditions, projections, tables));

        return resultTables;
    }


    public Field label(Label node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof Table) {
            return getValue((Table) part, true);
        } else if(part instanceof Field) {
            return (Field)part;
        } else {
            throw new IllegalArgumentException("argument of IsURI must be a variable");
        }
    }


    public Field lang(Lang node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof TNodes) {
            Field lang = ((TNodes)part).LANGUAGE;

            return create.coalesce(lang, create.val(""));
        } else if(part instanceof Field) {
            return create.val("");
        } else {
            throw new IllegalArgumentException("argument of Lang must be a variable or constant");
        }
    }


    public Condition langMatches(LangMatches node)  {
        QueryPart leftPart = meetValueExpression(node.getLeftArg());
        QueryPart rightPart = meetValueExpression(node.getRightArg());

        if(dialect == SQLDialect.POSTGRES) {
            // Postgres supports ILIKE
            return create.condition(getValue(leftPart, true)+" ILIKE "+create.decode().value(getValue(rightPart, true)).when("*", create.val("%")).otherwise(getValue(rightPart, true).concat("%")));
        } else {
            return getValue(leftPart, true).lower().like(create.decode().value(getValue(rightPart, true)).when("*", create.val("%")).otherwise(getValue(rightPart, true).lower().concat("%")));
        }
    }


    public List<JoinTable> leftJoin(LeftJoin node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections,Collection<Table> tables)  {
        Set<Condition> conditions2 = new HashSet<Condition>();
        // this belongs to the join condition!
        if(node.getCondition() != null) {
            QueryPart conditionPart = meetValueExpression(node.getCondition());
            conditions2.add((Condition)conditionPart);
        }

        List<JoinTable> leftTables = meetTableLike(node.getLeftArg(), query, conditions,  projections, tables);

        Table left = null;
        for(JoinTable joinTable : leftTables) {
            if(left == null) {
                left = joinTable.getTable();
                conditions.addAll(joinTable.getConditions());
            } else {
                left = left.join(joinTable.getTable()).on(joinTable.getConditions().toArray(new Condition[0]));
            }
        }

        List<JoinTable> rightTables = meetTableLike(node.getRightArg(), query, conditions2,  projections, tables);
        Table right = null;
        for(JoinTable joinTable : rightTables) {
            if(right == null) {
                right = joinTable.getTable();
                conditions2.addAll(joinTable.getConditions());
            } else {
                Set<Condition> onConditions = new HashSet<Condition>();
                Set<Condition> backConditions = joinTable.getBackConditions();
                for(Condition c : joinTable.getConditions()) {
                    if(!backConditions.contains(c)) {
                        onConditions.add(c);
                    }
                }

                right = right.join(joinTable.getTable()).on(onConditions.toArray(new Condition[0]));
                conditions2.addAll(backConditions);
            }
        }

        if(left != null && right != null) {
            return Collections.singletonList(new JoinTable((Table)left.leftOuterJoin(right).on(conditions2.toArray(new Condition[0]))));
        } else {
            conditions.addAll(conditions2);
            return Collections.singletonList(new JoinTable((Table) left != null? left : right));
        }


    }


    public Condition like(Like node)  {
        QueryPart part = meetValueExpression(node.getArg());

        Condition result = null;
        for(Field f : getAlternativeValues(part, true)) {
            if(result == null) {
                result = f.like(node.getOpPattern());
            } else {
                result = result.or(f.like(node.getOpPattern()));
            }
        }

        return result;
    }




    /**
     * Mathematical expression on the values of the two arguments of the expression. We implement this by
     * always retrieving the double value of the two arguments and then performing the corresponding mathematical
     * operation on the SQL level.
     * @param node
     * @return
     */
    public Field mathExpr(MathExpr node)  {
        QueryPart leftPart = meetValueExpression(node.getLeftArg());
        QueryPart rightPart = meetValueExpression(node.getRightArg());

        Field left, right;

        left = getDoubleValue(leftPart,typeCast == CastType.STRICT);
        right = getDoubleValue(rightPart,typeCast == CastType.STRICT);

        switch (node.getOperator()) {
            case DIVIDE:
                return left.div(right);
            case MINUS:
                return left.sub(right);
            case MULTIPLY:
                return left.mul(right);
            case PLUS:
                return left.add(right);
            default:
                throw new IllegalArgumentException("unknown operator: "+node.getOperator());
        }


    }


    /**
     * Return the maximum value of the arguments. Translated to SQL max() function
     * @param node
     * @return
     */
    public Field max(Max node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof Table && ((Table) part).getName().equals(T_NODES.getName())) {
            return getValue((Table)part, true).max();
        } else {
            throw new IllegalArgumentException("MAX can only be computed for the node table; type was: "+part.getClass().getName());
        }
    }


    /**
     * Return the minimum value of the arguments. Translated to SQL min() function
     * @param node
     * @return
     */
    public Field min(Min node)  {
        QueryPart part = meetValueExpression(node.getArg());

        if(part instanceof Table && ((Table) part).getName().equals(T_NODES.getName())) {
            return getValue((Table)part, true).min();
        } else {
            throw new IllegalArgumentException("MAX can only be computed for the node table; type was: "+part.getClass().getName());
        }
    }




    public Select multiProjection(MultiProjection node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Namespace querying. Currently not supported by the translator. Could be implemented either by using
     * SQL string operations on the URI of the argument, or by joining with the namespace table and trying to
     * find a prefix.
     * @param node
     * @return
     */
    public QueryPart namespace(Namespace node)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Namespace querying. Currently not supported by the translator. Could be implemented either by using
     * SQL string operations on the URI of the argument, or by joining with the namespace table and trying to
     * find a prefix.
     * @param node
     * @return
     */
    public QueryPart localName(LocalName node)  {
        // todo: would require querying against the namespace table as well!

        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }


    /**
     * NOT of a value expression. Translated into a NOT in case the argument is a condition, or
     * a NEG in case the argument is a value.
     * @param node
     * @return
     */
    public QueryPart not(Not node)  {
        QueryPart subPart = meetValueExpression(node.getArg());

        if(subPart instanceof Condition) {
            return ((Condition)subPart).not();
        } else if(subPart instanceof Field) {
            return ((Field)subPart).neg();
        }

        throw new IllegalStateException("the argument of the NOT condition is not boolean: "+node);
    }

    /**
     * OR between value expressions; translated into a disjunction at the SQL level.
     * @param node
     * @return
     */
    public Condition or(Or node)  {
        QueryPart leftPart = meetValueExpression(node.getLeftArg());
        QueryPart rightPart = meetValueExpression(node.getRightArg());

        if(! (leftPart instanceof Condition) || !(rightPart instanceof Condition)) {
            throw new IllegalStateException("the left or right part of the OR condition are not boolean: "+node);
        }

        return ((Condition)leftPart).or((Condition) rightPart);
    }

    /**
     * Ordering of query results. For each ordering used in the query, we keep track of a value expression
     * that needs to be projected by all subqueries so we can later sort over the results of several subqueries
     * (otherwise the SQL database might complain about ordering over non-projected variables, which is semantically
     * not allowed in SQL).
     *
     * @param node
     * @param query
     * @param conditions
     * @param projections
     * @return
     */
    public Select order(Order node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {
        int i=0;
        for(OrderElem elem : node.getElements()) {
            QueryPart expr = meetValueExpression(elem.getExpr());
            String name = "_order_" + (++i);
            sortFields.put(name, getValue(expr, true).as(name));
            sortDependencies.put(name,getValueExpressionVariables(elem.getExpr()));
        }


        Select select = meetTupleExpression(node.getArg(),query,conditions,  projections);
        if(select instanceof SelectQuery) {
            i=0;
            for(OrderElem elem : node.getElements()) {
                String name = "_order_" + (++i);
                if(elem.isAscending()) {
                    ((SelectQuery)select).addOrderBy(sortFields.get(name).asc());
                } else {
                    ((SelectQuery)select).addOrderBy(sortFields.get(name).desc());
                }
            }
            return select;
        } else {
            List<SortField<?>> fields = new ArrayList<SortField<?>>();
            i=0;
            for(OrderElem elem : node.getElements()) {
                String name = "_order_" + Math.abs(query.hashCode()) + "_" + (++i);
                if(elem.isAscending()) {
                    fields.add(sortFields.get(name).asc());
                } else {
                    fields.add(sortFields.get(name).desc());
                }
            }
            return create.select().from(select).orderBy(fields);
        }
    }

    /**
     * Projection: for all variables (table KiWiNode) in the projection we select all fields and add them to the select
     * part of the SQL query; this allows us to reconstruct KiWiNode instances in the query results
     * @param node
     * @param query
     * @return
     */
    public Select projection(Projection node, SelectQuery query, Collection<Condition> origConditions, Collection<String> projections)  {
        Set<String> projections2 = new HashSet<String>();

        // collect mappings before running recursively so they are available to child expressions
        for(ProjectionElem elem : node.getProjectionElemList().getElements()) {
            variableMappings.put(elem.getSourceName().replace("-","_"),elem.getTargetName());
            revVariableMappings.put(elem.getTargetName(),elem.getSourceName().replace("-","_"));
            projections2.add(elem.getSourceName());
        }


        Set<Condition> conditions = new HashSet<Condition>();

/*
        Table from = null;
        for(JoinTable tbl : meetTableLike(node.getArg(), query, conditions,  projections2, new HashSet<Table>())) {
            if(from == null) {
                from = tbl.getTable();
                query.addConditions(tbl.getConditions());
            } else {
                from = from.join(tbl.getTable()).on(tbl.getConditions().toArray(new Condition[0]));
            }
        }
        if(from != null)
            query.addFrom(from);
*/

        for(JoinTable tbl : meetTableLike(node.getArg(), query, conditions,  projections2, new HashSet<Table>())) {
                query.addFrom(tbl.getTable());
                query.addConditions(tbl.getConditions());
        }


        query.addConditions(conditions);

        if(groupBy.size() > 0) {
            for(String group : groupBy) {
                Field variable = extensions.get(group.replace("-","_"));
                if(variable == null) {
                    TNodes table = getVariable(group);
                    if(table != null) {
                        query.addGroupBy(table.ID);
                        query.addGroupBy(table.URI);
                        query.addGroupBy(table.ANONID);
                        query.addGroupBy(table.CONTENT);
                    }

                } else {
                    query.addGroupBy(variable);
                }
            }
        }

        if(node.getArg() instanceof Union || node.getArg() instanceof Intersection || node.getArg() instanceof Difference) {
            // union, difference, intersection: we take all results, as the merge operation already does the projection
            projectedVariables.addAll(node.getBindingNames());
        } else {
            for(ProjectionElem elem : node.getProjectionElemList().getElements()) {
                projectionElem(elem, query);
            }
            for(Map.Entry<String,Field> entry : sortFields.entrySet()) {
                if(!hasSelectionVariable(query,entry.getKey())) {
                    Set<String> dependencies = sortDependencies.get(entry.getKey());
                    boolean containsAll = true;
                    for(String dep : dependencies) {
                        if(!currentVariables.containsKey(dep) && !extensions.containsKey(dep)) {
                            containsAll = false;
                        }
                    }
                    if(containsAll) {
                        query.addSelect(entry.getValue());
                    } else {
                        query.addSelect(create.value(null).as(entry.getKey()));
                    }
                }
            }
        }

        return query;
    }


    public void projectionElem(ProjectionElem node, SelectQuery query)  {
        // renaming needs to take place in the result construction!
        TNodes var = getVariable(node.getSourceName());

        // if var is null, then we have an extension
        if(var != null) {
            query.addSelect(var.ID.as(node.getTargetName())); // just the ID for now, we will use Hibernate to load the complete node
            projectedVariables.add(node.getTargetName());
        } else {
            Field extension = extensions.get(node.getSourceName().replace("-","_"));
            query.addSelect(extension.as(node.getTargetName()));
            extensionVariables.put(node.getTargetName(), extension.getType());
        }

        // extension bindings need to be treated differently in result construction!
    }




    public Select queryRoot(QueryRoot node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {
        this.resourceVariables = new FindResourceVariables(node).getResourceVariables();

        return meetTupleExpression(node.getArg(),query, conditions,  projections);
    }


    /**
     * Reduced is a strange construct that means "sometimes distinct". We translate it into distinct in all cases.
     * @param node
     * @param query
     * @param conditions
     * @param projections
     * @return
     */
    public Select reduced(Reduced node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {

        if(configurationService.getBooleanConfiguration("sparql.native.reduced_as_distinct",false)) {

            Select subQuery = meetTupleExpression(node.getArg(), query, conditions,  projections);

            if(subQuery instanceof SelectQuery) {
                ((SelectQuery) subQuery).setDistinct(true);
                return subQuery;
            } else {
                return create.selectDistinct().from(subQuery);
            }
        } else {
            return meetTupleExpression(node.getArg(), query, conditions,  projections);
        }
    }

    /**
     * Translate a regular expression into a corresponding database operator. Regular expressions are only supported
     * in some databases (currently: Postgres, H2, Oracle, MySQL) and are in some implementations comparably slow, so
     * use them with care.
     * <p/>
     * This method tries to translate simple regular expressions into LIKE operators, which are considerably more
     * efficient to evaluate in many database systems. Currently, this is done for regular expressions that do not
     * contain any special constructs, i.e. consist of alphanumeric characters and blanks only.
     * @param node
     * @return
     */
    public Condition regex(Regex node)  {
        QueryPart argPart = meetValueExpression(node.getArg());

        if(node.getPatternArg() instanceof ValueConstant) {
            ValueConstant pat = (ValueConstant)node.getPatternArg();
            String pattern = pat.getValue().stringValue();

            Condition result = null;
            Field f = getValue(argPart,typeCast == CastType.STRICT);

            // simple cases can be handled by LIKE
            if(pattern.matches("^\\^?[\\p{Alnum}\\p{Blank}]+$")) {
                if(pattern.startsWith("^")) {
                    result = f.like(pattern.substring(1) + "%");
                } else {
                    result = f.like("%" + pattern + "%");
                }
            } else {
                switch (dialect) {
                    case POSTGRES:
                        result = create.condition(f + " ~ ?",pattern);
                        break;
                    case H2:
                        result = create.condition(f + " REGEXP ?",pattern);
                        break;
                    case MYSQL:
                        result = create.condition(f + " REGEXP ?",pattern);
                        break;
                    case ORACLE:
                        result = create.condition("REGEXP_LIKE("+f + ",?)" ,pattern);
                        break;
                }
            }
            return result;
        } else {
            QueryPart patPart = meetValueExpression(node.getPatternArg());
            switch (dialect) {
                case POSTGRES:
                    return create.condition(getValue(argPart, typeCast == CastType.STRICT) + " ~ " + getValue(patPart, typeCast == CastType.STRICT));
                case H2:
                    return create.condition(getValue(argPart, typeCast == CastType.STRICT) + " REGEXP " + getValue(patPart, typeCast == CastType.STRICT));
                case MYSQL:
                    return create.condition(getValue(argPart, typeCast == CastType.STRICT) + " REGEXP " + getValue(patPart, typeCast == CastType.STRICT));
                case ORACLE:
                    return create.condition("REGEXP_LIKE("+getValue(argPart, typeCast == CastType.STRICT) + "," + getValue(patPart, typeCast == CastType.STRICT)+")");

            }
        }
        throw new UnsupportedOperationException("database "+dialect+" not supported for regular expression matches");
    }

    /**
     * Check whether the left and the right argument are the same term. In case both arguments identify variables,
     * we translate this into a check for equality of database identifiers, as this is most efficient. In all other
     * cases we check equality of the content
     * @param node
     * @return
     */
    public QueryPart sameTerm(SameTerm node)  {
        QueryPart leftPart = meetValueExpression(node.getLeftArg());
        QueryPart rightPart = meetValueExpression(node.getRightArg());

        if(leftPart instanceof TNodes && rightPart instanceof TNodes) {
            return ((TNodes) leftPart).ID.equal(((TNodes) rightPart).ID);
        } else if(leftPart instanceof TNodes && rightPart instanceof Field) {
            // check if one of the value-holding fields of leftpart matches rightpart
            Condition result = null;
            for(Field f : getAlternativeValues(leftPart,false)) {
                if(result == null) {
                    result = f.equal((Field)rightPart);
                } else {
                    result = result.or(f.equal((Field)rightPart));
                }
            }
            return result;
        } else if(rightPart instanceof TNodes && leftPart instanceof Field) {
            // check if one of the value-holding fields of leftpart matches rightpart
            Condition result = null;
            for(Field f : getAlternativeValues(rightPart,false)) {
                if(result == null) {
                    result = f.equal((Field)leftPart);
                } else {
                    result = result.or(f.equal((Field)leftPart));
                }
            }
            return result;
        } else {
            // TODO: language tags for literals?
            return getValue(leftPart, true).equal(getValue(rightPart, true));
        }
    }


    public QueryPart sample(Sample node)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }


    public Select service(Service node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }


    public Select singletonSet(SingletonSet node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {
        query.addConditions(create.trueCondition());
        return query;
    }


    public Select slice(Slice node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections)  {
        query.addLimit(node.hasOffset() ? (int) node.getOffset() : 0, node.hasLimit() ? (int) node.getLimit() : 0);
        if(node.getArg() instanceof Projection || node.getArg() instanceof Distinct || node.getArg() instanceof Reduced || node.getArg() instanceof Order) {
            return meetTupleExpression(node.getArg(), query, conditions,  projections);
        } else {
            List<Table> tables = new ArrayList<Table>();
            Table from = null;
            for(JoinTable tbl : meetTableLike(node.getArg(), query, conditions,  node.getBindingNames(),tables)) {
                if(from == null) {
                    from = tbl.getTable();
                    query.addConditions(tbl.getConditions());
                } else {
                    from = from.join(tbl.getTable()).on(tbl.getConditions().toArray(new Condition[0]));
                }
            }
            query.addFrom(from);
            query.addConditions(conditions);
            if(tables.size() > 0 && tables.get(0) instanceof TTriples) {
                query.addSelect(((TTriples) tables.get(0)).ID.as("_firsttable"));
            }
            return query;
        }
    }


    public List<JoinTable> statementPattern(StatementPattern p, SelectQuery query, Collection<Condition> conditions, Collection<String> projections, Collection<Table> tables)  {
        String name = "t"+ Math.abs(++tripleCounter);

        // statement pattern; we add a triple to the from clause and require it is not deleted; then we add
        // conditions for subject, predicate, object, context depending on whether they are variable or
        // constant values

        TTriples triple = T_TRIPLES.as(name);
        JoinTable j_triple = new JoinTable(triple);
        j_triple.addCondition(triple.DELETED.isFalse());
        tables.add(triple);

        TNodes t_subject = null, t_property = null, t_object = null, t_context = null;
        JoinTable j_subject = null, j_property = null, j_object = null, j_context = null;

        List<JoinTable> resultTables = new ArrayList<JoinTable>();


        resultTables.add(j_triple);


        // conditions for subject:
        // - if there is a value, and the value is a KiWiNode, simply check the ID (most efficient query)
        // - if there is a value, but it is not a KiWiNode, check the properties depending on the type of value
        // - if there is no value, add a variable to the from clause and add a join condition
        Var subj = p.getSubjectVar();
        if(subj.hasValue()) {
            Value v = subj.getValue();
            t_subject =  T_NODES.as(name+"_subject");
            j_subject = new JoinTable(t_subject);

            if(v instanceof KiWiNode && ((KiWiNode) v).getId() != null) {
                KiWiNode n = (KiWiNode) v;
                j_triple.addCondition(triple.SUBJECT_ID.equal((Field) create.val(n.getId())));
            } else if(v instanceof URI) {

                if(configurationService.getBooleanConfiguration("sparql.native.preload_constants",true)) {
                    KiWiUriResource r_subject = resourceService.getUriResource(v.stringValue());
                    if(r_subject != null) {
                        j_triple.addCondition(triple.SUBJECT_ID.equal((Field) create.val(r_subject.getId())));
                    } else {
                        j_triple.addCondition(create.falseCondition());
                    }
                } else {
                    // performance improvement: if uri already was used, reuse the node instead of doing a new join
                    if(uriNodes.containsKey(v.stringValue())) {
                        t_subject = uriNodes.get(v.stringValue());
                        Condition c_subject = triple.SUBJECT_ID.equal((Field) t_subject.ID);
                        j_triple.addCondition(c_subject);
                        j_triple.addBackCondition(c_subject);
                    } else {
                        resultTables.add(j_subject);
                        j_subject.addCondition(t_subject.URI.equal((Field) create.val(v.stringValue())));
                        j_subject.addCondition(triple.SUBJECT_ID.equal((Field) t_subject.ID));
                        uriNodes.put(v.stringValue(),t_subject);
                    }
                }

            } else if(v instanceof BNode) {
                resultTables.add(j_subject);
                j_subject.addCondition(t_subject.ANONID.equal((Field) create.val(((BNode) v).getID())));
                j_subject.addCondition(triple.SUBJECT_ID.equal((Field) t_subject.ID));
            } else {
                throw new UnsupportedOperationException("value of type "+v.getClass()+" not supported!");
            }
        } else {
            String varname = subj.getName().replace("-", "_");

            t_subject = getVariable(varname);
            if(!hasQueryVariable(query,varname)) {
                // variable does not exist yet, so we create it and join it to the current from clause ...
                t_subject = createVariable(varname);
                addQueryVariable(query,varname,t_subject);
                j_subject = new JoinTable(t_subject);
                j_subject.addCondition(triple.SUBJECT_ID.equal((Field) t_subject.ID));
                resultTables.add(j_subject);
            } else {
                // variable already exists in FROM, we simply add a join condition
                Condition c_subject = triple.SUBJECT_ID.equal((Field) t_subject.ID);
                j_triple.addCondition(c_subject);
                j_triple.addBackCondition(c_subject);
            }
        }

        Var prop = p.getPredicateVar();
        if(prop.hasValue()) {
            Value v = prop.getValue();
            t_property = T_NODES.as(name+"_property");
            j_property = new JoinTable(t_property);

            if(v instanceof KiWiNode && ((KiWiNode) v).getId() != null) {
                KiWiNode n = (KiWiNode) v;
                j_triple.addCondition(triple.PROPERTY_ID.equal((Field) create.val(n.getId())));
            } else if(v instanceof URI) {
                if(configurationService.getBooleanConfiguration("sparql.native.preload_constants",true)) {
                    KiWiUriResource r_property = resourceService.getUriResource(v.stringValue());
                    if(r_property != null) {
                        j_triple.addCondition(triple.PROPERTY_ID.equal((Field) create.val(r_property.getId())));
                    } else {
                        j_triple.addCondition(create.falseCondition());
                    }
                } else {
                    // performance improvement: if uri already was used, reuse the node instead of doing a new join
                    if(uriNodes.containsKey(v.stringValue())) {
                        t_property = uriNodes.get(v.stringValue());
                        Condition c_property = triple.PROPERTY_ID.equal((Field) t_property.ID);
                        j_triple.addCondition(c_property);
                        j_triple.addBackCondition(c_property);
                    } else {
                        resultTables.add(j_property);
                        j_property.addCondition(t_property.URI.equal((Field) create.val(v.stringValue())));
                        j_property.addCondition(triple.PROPERTY_ID.equal((Field) t_property.ID));
                        uriNodes.put(v.stringValue(),t_property);
                    }
                }


            } else {
                throw new UnsupportedOperationException("value of type "+v.getClass()+" not supported!");
            }
        } else {
            String varname = prop.getName().replace("-","_");

            t_property = getVariable(varname);
            if(!hasQueryVariable(query,varname)) {
                // variable does not exist yet, so we create it and join it to the current from clause ...
                t_property = createVariable(varname);
                addQueryVariable(query,varname,t_property);
                j_property = new JoinTable(t_property);
                j_property.addCondition(triple.PROPERTY_ID.equal((Field) t_property.ID));
                resultTables.add(j_property);
            } else {
                // variable already exists in FROM, we simply add a join condition
                Condition c_property = triple.PROPERTY_ID.equal((Field) t_property.ID);
                j_triple.addCondition(c_property);
                j_triple.addBackCondition(c_property);
            }
        }

        Var obj  = p.getObjectVar();
        if(obj.hasValue()) {
            Value v = obj.getValue();
            t_object = T_NODES.as(name+"_object");
            j_object = new JoinTable(t_object);

            if(v instanceof KiWiNode && ((KiWiNode) v).getId() != null) {
                KiWiNode n = (KiWiNode) v;
                j_triple.addCondition(triple.OBJECT_ID.equal((Field) create.val(n.getId())));
            } else if(v instanceof URI) {
                if(configurationService.getBooleanConfiguration("sparql.native.preload_constants",true)) {
                    KiWiUriResource r_object = resourceService.getUriResource(v.stringValue());
                    if(r_object != null) {
                        j_triple.addCondition(triple.OBJECT_ID.equal((Field) create.val(r_object.getId())));
                    } else {
                        j_triple.addCondition(create.falseCondition());
                    }
                } else {
                    // performance improvement: if uri already was used, reuse the node instead of doing a new join
                    if(uriNodes.containsKey(v.stringValue())) {
                        t_object = uriNodes.get(v.stringValue());
                        Condition c_object = triple.OBJECT_ID.equal((Field) t_object.ID);
                        j_triple.addCondition(c_object);
                        j_triple.addBackCondition(c_object);
                    } else {
                        resultTables.add(j_object);
                        j_object.addCondition(t_object.URI.equal((Field) create.val(v.stringValue())));
                        j_object.addCondition(triple.OBJECT_ID.equal((Field) t_object.ID));
                        uriNodes.put(v.stringValue(),t_object);
                    }
                }

            } else if(v instanceof BNode) {
                resultTables.add(j_object);
                j_object.addCondition(t_object.ANONID.equal((Field) create.val(((BNode) v).getID())));
                j_object.addCondition(triple.OBJECT_ID.equal((Field) t_object.ID));
            } else if(v instanceof Literal) {
                resultTables.add(j_object);
                j_object.addCondition(t_object.CONTENTMD5.equal((Field) create.val(HashUtils.md5sum(v.stringValue()))));
                j_object.addCondition(triple.OBJECT_ID.equal((Field) t_object.ID));
            } else {
                throw new UnsupportedOperationException("value of type "+v.getClass()+" not supported!");
            }
        } else  {
            String varname = obj.getName().replace("-","_");

            t_object = getVariable(varname);
            if(!hasQueryVariable(query,varname)) {
                // variable does not exist yet, so we create it and join it to the current from clause ...
                t_object = createVariable(varname);
                addQueryVariable(query,varname,t_object);
                j_object = new JoinTable(t_object);
                j_object.addCondition(triple.OBJECT_ID.equal((Field) t_object.ID));
                resultTables.add(j_object);
            } else {
                // variable already exists in FROM, we simply add a join condition
                Condition c_object = triple.OBJECT_ID.equal((Field) t_object.ID);
                j_triple.addCondition(c_object);
                j_triple.addBackCondition(c_object);
            }
        }

        Var ctx  = p.getContextVar();
        if(ctx != null) {
            if(ctx.hasValue()) {
                Value v = ctx.getValue();
                t_context =  T_NODES.as(name+"_context");
                j_context = new JoinTable(t_context);

                if(v instanceof KiWiNode && ((KiWiNode) v).getId() != null) {
                    KiWiNode n = (KiWiNode) v;
                    j_triple.addCondition(triple.CONTEXT_ID.equal((Field) create.val(n.getId())));
                } else if(v instanceof URI) {
                    if(configurationService.getBooleanConfiguration("sparql.native.preload_constants",true)) {
                        KiWiUriResource r_context = resourceService.getUriResource(v.stringValue());
                        if(r_context != null) {
                            j_triple.addCondition(triple.CONTEXT_ID.equal((Field) create.val(r_context.getId())));
                        } else {
                            j_triple.addCondition(create.falseCondition());
                        }
                    } else {
                        // performance improvement: if uri already was used, reuse the node instead of doing a new join
                        if(uriNodes.containsKey(v.stringValue())) {
                            t_context = uriNodes.get(v.stringValue());
                            Condition c_context = triple.CONTEXT_ID.equal((Field) t_context.ID);
                            j_triple.addCondition(c_context);
                            j_triple.addBackCondition(c_context);
                        } else {
                            resultTables.add(j_context);
                            j_context.addCondition(t_context.URI.equal((Field) create.val(v.stringValue())));
                            j_context.addCondition(triple.CONTEXT_ID.equal((Field) t_context.ID));
                            uriNodes.put(v.stringValue(),t_context);
                        }
                    }

                } else if(v instanceof BNode) {
                    resultTables.add(j_context);
                    j_context.addCondition(t_context.ANONID.equal((Field) create.val(((BNode) v).getID())));
                    j_context.addCondition(triple.CONTEXT_ID.equal((Field) t_context.ID));
                } else {
                    throw new UnsupportedOperationException("value of type "+v.getClass()+" not supported!");
                }
            } else {
                String varname = ctx.getName().replace("-","_");

                t_context = getVariable(varname);
                if(!hasQueryVariable(query,varname)) {
                    // variable does not exist yet, so we create it and join it to the current from clause ...
                    t_context = createVariable(varname);
                    addQueryVariable(query,varname,t_context);
                    j_context = new JoinTable(t_context);
                    j_context.addCondition(triple.CONTEXT_ID.equal((Field) t_context.ID));
                    resultTables.add(j_context);
                } else {
                    // variable already exists in FROM, we simply add a join condition
                    Condition c_context = triple.CONTEXT_ID.equal((Field) t_context.ID);
                    j_triple.addCondition(c_context);
                    j_triple.addBackCondition(c_context);
                }
            }
        }

        return resultTables;

    }


    public Field str(Str node)  {
        QueryPart part = meetValueExpression(node.getArg());

        return getValue(part, true);
    }


    public Field sum(Sum node)  {
        QueryPart part = meetValueExpression(node.getArg());

        return getDoubleValue(part, typeCast == CastType.STRICT).sum();
    }


    private List<JoinTable> unionTable(Union node, SelectQuery query, Collection<Condition> conditions, Collection<String> projections, Collection<Table> tables)  {
        return merge(node, query, MergeType.UNION, conditions, projections, tables);
    }


    // special case: union occurring as tuple expression, not as table; this is typically the result of a multi-projection
    private Select unionQuery(Union node, SelectQuery query,Collection<Condition> conditions, Collection<String> projections) {
        // the bindings in proper order
        List<String> bindings = new ArrayList<String>();
        for(String b : node.getBindingNames()) {
            if(!b.startsWith("-const") && projections.contains(b)) {
                bindings.add(b);
            }
        }

        Map<String,TNodes> contextVars = currentVariables;
        Map<String,TNodes> contextUris = uriNodes;

        // build subquery for left part of the binary tuple query
        currentVariables = new HashMap<String, TNodes>();
        uriNodes         = new HashMap<String, TNodes>();
        Set<Condition> leftConditions = new HashSet<Condition>();
        SelectQuery leftQuery = create.selectQuery();
        leftQuery.addFrom(meetTupleExpression(node.getLeftArg(), create.selectQuery(), leftConditions,  projections));
        if(leftConditions.size() > 0) {
            leftQuery.addConditions(leftConditions);
        }

        // for each of the projected variables of the main query, add a projection to the subquery; if the projection
        // variable is not bound in this subquery, bind it explicitly to NULL
        for(String var : bindings) {
            var = var.replace("-","_");
            String revVar = revVariableMappings.get(var);

            if(!hasSelectionVariable(leftQuery,var)) {

                if(currentVariables.containsKey(var)) {
                    leftQuery.addSelect(currentVariables.get(var).ID.as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else if(extensions.containsKey(var)) {
                    leftQuery.addSelect(extensions.get(var).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else if(revVar != null && extensions.containsKey(revVar)) {
                    leftQuery.addSelect(extensions.get(revVar).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else {
                    leftQuery.addSelect(create.value(null,Long.class).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                }
            }
        }
        // for each of the fields used in sorting the overall result, add an additional column to the projection so
        // that we can afterwards sort easily over the UNION
        for(Map.Entry<String,Field> entry : sortFields.entrySet()) {
            if(!hasSelectionVariable(leftQuery,entry.getKey())) {
                Set<String> dependencies = sortDependencies.get(entry.getKey());
                boolean containsAll = true;
                for(String dep : dependencies) {
                    if(!currentVariables.containsKey(dep) && !extensions.containsKey(dep)) {
                        containsAll = false;
                    }
                }
                if(containsAll) {
                    leftQuery.addSelect(entry.getValue());
                } else {
                    leftQuery.addSelect(create.value(null).as(entry.getKey()));
                }
            }
        }

        currentVariables = new HashMap<String, TNodes>();
        uriNodes         = new HashMap<String, TNodes>();
        Set<Condition> rightConditions = new HashSet<Condition>();
        SelectQuery rightQuery = create.selectQuery();
        rightQuery.addFrom(meetTupleExpression(node.getRightArg(), create.selectQuery(), rightConditions,  projections));
        rightQuery.addConditions(rightConditions);

        for(String var : bindings) {
            var = var.replace("-","_");
            String revVar = revVariableMappings.get(var);

            if(!hasSelectionVariable(rightQuery,var)) {

                if(currentVariables.containsKey(var)) {
                    rightQuery.addSelect(currentVariables.get(var).ID.as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else if(extensions.containsKey(var)) {
                    rightQuery.addSelect(extensions.get(var).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else if(revVar != null && extensions.containsKey(revVar)) {
                    rightQuery.addSelect(extensions.get(revVar).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else {
                    rightQuery.addSelect(create.value(null,Long.class).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                }
            }
        }
        for(Map.Entry<String,Field> entry : sortFields.entrySet()) {
            if(!hasSelectionVariable(rightQuery,entry.getKey())) {
                Set<String> dependencies = sortDependencies.get(entry.getKey());
                boolean containsAll = true;
                for(String dep : dependencies) {
                    if(!currentVariables.containsKey(dep) && !extensions.containsKey(dep)) {
                        containsAll = false;
                    }
                }
                if(containsAll) {
                    rightQuery.addSelect(entry.getValue());
                } else {
                    rightQuery.addSelect(create.value(null).as(entry.getKey()));
                }
            }
        }

        currentVariables = contextVars;
        uriNodes         = contextUris;

        return leftQuery.union(rightQuery);
     }

    /**
     * Merge the results of two subqueries using either UNION, DIFFERENCE, or INTERSECTION.
     * Note that UNION & co in SPARQL and SQL have slightly different semantics: whereas SQL requires an equal number
     * of columns (of equal types) in the subqueries to carry out the UNION, SPARQL performs the UNION on the names of
     * the variables; it does not require that all variables are bound in all query parts.
     * <p/>
     * In order to model this behaviour in SQL, this method performs the following steps:
     * <ul>
     *     <li>build separate subqueries for each of the parts of the union</li>
     *     <li>find out which variables to project in which order, and which variables need to be bound to NULL
     *         in one of the subqueries because they are not otherwise bound</li>
     *     <li>find out which of the variables that are NOT projected might be required for sorting the overall
     *         result, and add these to the projections of the subqueries</li>
     * </ul>
     * @param node
     * @param query
     * @param op
     * @param conditions
     * @param projections
     * @param tables
     * @return
     */
    public List<JoinTable> merge(BinaryTupleOperator node, SelectQuery query, MergeType op, Collection<Condition> conditions, Collection<String> projections, Collection<Table> tables )  {
        // the bindings in proper order
        List<String> bindings = new ArrayList<String>();
        for(String b : node.getBindingNames()) {
            if(!b.startsWith("-const") && projections.contains(b)) {
                bindings.add(b);
            }
        }

        Map<String,TNodes> contextVars = currentVariables;
        Map<String,TNodes> contextUris = uriNodes;

        // build subquery for left part of the binary tuple query
        currentVariables = new HashMap<String, TNodes>();
        uriNodes         = new HashMap<String, TNodes>();
        Set<Condition> leftConditions = new HashSet<Condition>();
        SelectQuery leftQuery = create.selectQuery();

        // the FROM and CONDITION are retrieved from the subquery
/*
        Table left = null;
        for(JoinTable tbl : meetTableLike(node.getLeftArg(), leftQuery, leftConditions,  projections, new HashSet<Table>())) {
            if(left == null) {
                left = tbl.getTable();
                leftConditions.addAll(tbl.getConditions());
            } else {
                left = left.join(tbl.getTable()).on(tbl.getConditions().toArray(new Condition[0]));
            }
        }
        leftQuery.addFrom(left);
*/

        for(JoinTable tbl : meetTableLike(node.getLeftArg(), leftQuery, leftConditions,  projections, new HashSet<Table>())) {
            leftQuery.addFrom(tbl.getTable());
            leftConditions.addAll(tbl.getConditions());
        }
        leftQuery.addConditions(leftConditions);

        // for each of the projected variables of the main query, add a projection to the subquery; if the projection
        // variable is not bound in this subquery, bind it explicitly to NULL
        for(String var : bindings) {
            var = var.replace("-","_");
            String revVar = revVariableMappings.get(var);

            if(!hasSelectionVariable(leftQuery,var)) {

                if(currentVariables.containsKey(var)) {
                    leftQuery.addSelect(currentVariables.get(var).ID.as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else if(extensions.containsKey(var)) {
                    leftQuery.addSelect(extensions.get(var).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else if(revVar != null && extensions.containsKey(revVar)) {
                    leftQuery.addSelect(extensions.get(revVar).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else {
                    leftQuery.addSelect(create.value(null,Long.class).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                }
            }
        }
        // for each of the fields used in sorting the overall result, add an additional column to the projection so
        // that we can afterwards sort easily over the UNION
        for(Map.Entry<String,Field> entry : sortFields.entrySet()) {
            if(!hasSelectionVariable(leftQuery,entry.getKey())) {
                Set<String> dependencies = sortDependencies.get(entry.getKey());
                boolean containsAll = true;
                for(String dep : dependencies) {
                    if(!currentVariables.containsKey(dep) && !extensions.containsKey(dep)) {
                        containsAll = false;
                    }
                }
                if(containsAll) {
                    leftQuery.addSelect(entry.getValue());
                } else {
                    leftQuery.addSelect(create.value(null).as(entry.getKey()));
                }
            }
        }

        currentVariables = new HashMap<String, TNodes>();
        uriNodes         = new HashMap<String, TNodes>();
        Set<Condition> rightConditions = new HashSet<Condition>();
        SelectQuery rightQuery = create.selectQuery();
/*
        Table right = null;
        for(JoinTable tbl : meetTableLike(node.getRightArg(), leftQuery, leftConditions,  projections, new HashSet<Table>())) {
            if(right == null) {
                right = tbl.getTable();
                rightConditions.addAll(tbl.getConditions());
            } else {
                right = right.join(tbl.getTable()).on(tbl.getConditions().toArray(new Condition[0]));
            }
        }
        rightQuery.addFrom(right);
*/

        for(JoinTable tbl : meetTableLike(node.getRightArg(), leftQuery, leftConditions,  projections, new HashSet<Table>())) {
            rightQuery.addFrom(tbl.getTable());
            rightConditions.addAll(tbl.getConditions());
        }
        rightQuery.addConditions(rightConditions);

        for(String var : bindings) {
            var = var.replace("-","_");
            String revVar = revVariableMappings.get(var);

            if(!hasSelectionVariable(rightQuery,var)) {

                if(currentVariables.containsKey(var)) {
                    rightQuery.addSelect(currentVariables.get(var).ID.as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else if(extensions.containsKey(var)) {
                    rightQuery.addSelect(extensions.get(var).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else if(revVar != null && extensions.containsKey(revVar)) {
                    rightQuery.addSelect(extensions.get(revVar).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                } else {
                    rightQuery.addSelect(create.value(null,Long.class).as(variableMappings.containsKey(var)?variableMappings.get(var):var));
                }
            }
        }
        for(Map.Entry<String,Field> entry : sortFields.entrySet()) {
            if(!hasSelectionVariable(rightQuery,entry.getKey())) {
                Set<String> dependencies = sortDependencies.get(entry.getKey());
                boolean containsAll = true;
                for(String dep : dependencies) {
                    if(!currentVariables.containsKey(dep) && !extensions.containsKey(dep)) {
                        containsAll = false;
                    }
                }
                if(containsAll) {
                    rightQuery.addSelect(entry.getValue());
                } else {
                    rightQuery.addSelect(create.value(null).as(entry.getKey()));
                }
            }
        }

        currentVariables = contextVars;
        uriNodes         = contextUris;

        switch (op) {
            case UNION:
                return Collections.singletonList(new JoinTable((Table)leftQuery.union(rightQuery).asTable()));
            case DIFFERENCE:
                return Collections.singletonList(new JoinTable((Table)leftQuery.except(rightQuery).asTable()));
            case INTERSECTION:
                return Collections.singletonList(new JoinTable((Table)leftQuery.intersect(rightQuery).asTable()));
            default:
                throw new IllegalStateException("unknown operator");
        }
    }


    public Field valueConstant(ValueConstant node)  {
        if(node.getValue() instanceof IntegerLiteralImpl) {
            return create.val(((IntegerLiteralImpl) node.getValue()).longValue());
        } else if(node.getValue() instanceof DecimalLiteralImpl) {
            return create.val(((DecimalLiteralImpl) node.getValue()).doubleValue());
        } else if(node.getValue() instanceof CalendarLiteralImpl) {
            return create.val(new Timestamp(((CalendarLiteralImpl) node.getValue()).calendarValue().toGregorianCalendar().getTimeInMillis()));
        } else if(node.getValue() instanceof Literal && ((Literal) node.getValue()).getDatatype() != null &&
                (((Literal) node.getValue()).getDatatype().stringValue().equals(Namespaces.NS_XSD+"integer") ||
                 ((Literal) node.getValue()).getDatatype().stringValue().equals(Namespaces.NS_XSD+"long")) ) {
            return create.val(((Literal) node.getValue()).longValue());
        } else if(node.getValue() instanceof Literal && ((Literal) node.getValue()).getDatatype() != null &&
                (((Literal) node.getValue()).getDatatype().stringValue().equals(Namespaces.NS_XSD+"double") ||
                        ((Literal) node.getValue()).getDatatype().stringValue().equals(Namespaces.NS_XSD+"float")) ) {
            return create.val(((Literal) node.getValue()).doubleValue());
        } else if(node.getValue() instanceof Literal && ((Literal) node.getValue()).getDatatype() != null &&
                  (((Literal) node.getValue()).getDatatype().stringValue().equals(Namespaces.NS_XSD+"dateTime") ||
                   ((Literal) node.getValue()).getDatatype().stringValue().equals(Namespaces.NS_XSD+"date")     ||
                          ((Literal) node.getValue()).getDatatype().stringValue().equals(Namespaces.NS_XSD+"time")) ) {
            return create.val(new Timestamp(DateUtils.parseDate(node.getValue().stringValue()).getTime()));
        } else {
            return create.val(node.getValue().stringValue());
        }
    }

    /**
     * A variable is represented by a query to the KIWINODES table and accessible via its name.
     * @param node
     * @return
     */
    public Table<RNodes> var(Var node)  {
        return T_NODES.as(getNodeName(node.getName()));
    }


    public QueryPart zeroLengthPath(ZeroLengthPath node)  {
        throw new UnsupportedOperationException(node.getClass().getName()+" not supported");    //To change body of overridden methods use File | Settings | File Templates.
    }


    protected Select meetTupleExpression(TupleExpr expr, SelectQuery query, Collection<Condition> conditions, Collection<String> projections) {
        if(expr instanceof BindingSetAssignment) {
            return bindingSetAssignment((BindingSetAssignment) expr, query, conditions,  projections);
        } else if(expr instanceof Distinct) {
            return distinct((Distinct) expr, query, conditions,  projections);
        } else if(expr instanceof EmptySet) {
            return emptySet((EmptySet) expr, query, conditions,  projections);
        } else if(expr instanceof SingletonSet) {
            return singletonSet((SingletonSet) expr, query, conditions,  projections);
        } else if(expr instanceof MultiProjection) {
            return multiProjection((MultiProjection) expr, query, conditions, projections);
        } else if(expr instanceof Order) {
            return order((Order) expr, query, conditions, projections);
        } else if(expr instanceof Projection) {
            return projection((Projection) expr, query, conditions, projections);
        } else if(expr instanceof QueryRoot) {
            return queryRoot((QueryRoot) expr, query, conditions, projections);
        } else if(expr instanceof Reduced) {
            return reduced((Reduced) expr, query, conditions, projections);
        } else if(expr instanceof Service) {
            return service((Service) expr, query, conditions, projections);
        } else if(expr instanceof Slice) {
            return slice((Slice) expr, query, conditions, projections);
        } else if(expr instanceof Union) {
            return unionQuery((Union) expr, query, conditions, projections);
        } else {
            Table from = null;
            for(JoinTable tbl : meetTableLike(expr, query, conditions,  projections, new HashSet<Table>())) {
                if(from == null) {
                    from = tbl.getTable();
                    query.addConditions(tbl.getConditions());
                } else {
                    from = from.join(tbl.getTable()).on(tbl.getConditions().toArray(new Condition[0]));
                }
            }
            query.addFrom(from);
            query.addConditions(conditions);
            return query;
        }
    }


    protected List<JoinTable> meetTableLike(TupleExpr expr, SelectQuery query, Collection<Condition> conditions, Collection<String> projections, Collection<Table> tables) {
        if(expr instanceof StatementPattern) {
            return statementPattern((StatementPattern) expr, query, conditions,  projections,tables);
        } else if(expr instanceof Join) {
            return join((Join) expr, query, conditions,  projections, tables);
        } else if(expr instanceof LeftJoin) {
            return leftJoin((LeftJoin) expr, query, conditions,  projections, tables);
        } else if(expr instanceof Union) {
            return unionTable((Union) expr, query, conditions, projections, tables);
        } else if(expr instanceof Intersection) {
            return intersection((Intersection) expr, query, conditions,  projections, tables);
        } else if(expr instanceof Difference) {
            return difference((Difference) expr, query, conditions,  projections, tables);
        } else if(expr instanceof Filter) {
            return filter((Filter) expr, query, conditions,  projections, tables);
        } else if(expr instanceof Extension) {
            return extension((Extension) expr, query, conditions,  projections, tables);
        } else if(expr instanceof SingletonSet) {
            return new ArrayList<JoinTable>();
        } else if(expr instanceof Group) {
            return group((Group)expr, query, conditions, projections, tables);
        } else {
            throw new UnsupportedOperationException("unsupported table-like tuple expression: "+expr);
        }
    }


    protected QueryPart meetValueExpression(ValueExpr expr) {
        if(expr instanceof And) {
            return meet((And) expr);
        } else if(expr instanceof Avg) {
            return avg((Avg) expr);
        } else if(expr instanceof Bound) {
            return bound((Bound) expr);
        } else if(expr instanceof Coalesce) {
            return coalesce((Coalesce) expr);
        } else if(expr instanceof Compare) {
            return compare((Compare) expr);
        } else if(expr instanceof CompareAll) {
            return compareAll((CompareAll) expr);
        } else if(expr instanceof CompareAny) {
            return compareAny((CompareAny) expr);
        } else if(expr instanceof Count) {
            return count((Count) expr);
        } else if(expr instanceof Datatype) {
            return datatype((Datatype) expr);
        } else if(expr instanceof Exists) {
            return exists((Exists) expr);
        } else if(expr instanceof FunctionCall) {
            return functionCall((FunctionCall) expr);
        } else if(expr instanceof GroupConcat) {
            return groupConcat((GroupConcat) expr);
        } else if(expr instanceof If) {
            return ifCond((If) expr);
        } else if(expr instanceof In) {
            return inCond((In) expr);
        } else if(expr instanceof IRIFunction) {
            return iriFunction((IRIFunction) expr);
        } else if(expr instanceof IsBNode) {
            return isBNode((IsBNode) expr);
        } else if(expr instanceof IsLiteral) {
            return isLiteral((IsLiteral) expr);
        } else if(expr instanceof IsNumeric) {
            return isNumeric((IsNumeric) expr);
        } else if(expr instanceof IsResource) {
            return isResource((IsResource) expr);
        } else if(expr instanceof IsURI) {
            return isURI((IsURI) expr);
        } else if(expr instanceof Label) {
            return label((Label) expr);
        } else if(expr instanceof Lang) {
            return lang((Lang) expr);
        } else if(expr instanceof LangMatches) {
            return langMatches((LangMatches) expr);
        } else if(expr instanceof Like) {
            return like((Like) expr);
        } else if(expr instanceof LocalName) {
            return localName((LocalName) expr);
        } else if(expr instanceof MathExpr) {
            return mathExpr((MathExpr) expr);
        } else if(expr instanceof Max) {
            return max((Max) expr);
        } else if(expr instanceof Min) {
            return min((Min) expr);
        } else if(expr instanceof Namespace) {
            return namespace((Namespace) expr);
        } else if(expr instanceof Not) {
            return not((Not) expr);
        } else if(expr instanceof Or) {
            return or((Or) expr);
        } else if(expr instanceof Regex) {
            return regex((Regex) expr);
        } else if(expr instanceof SameTerm) {
            return sameTerm((SameTerm) expr);
        } else if(expr instanceof Sample) {
            return sample((Sample) expr);
        } else if(expr instanceof Str) {
            return str((Str) expr);
        } else if(expr instanceof Sum) {
            return sum((Sum) expr);
        } else if(expr instanceof ValueConstant) {
            return valueConstant((ValueConstant) expr);
        } else if(expr instanceof Var) {
            return var((Var) expr);
        } else {
            throw new UnsupportedOperationException("unsupported value expression: "+expr);
        }
    }

    protected Set<String> getValueExpressionVariables(ValueExpr expr) {
        if(expr instanceof Var) {
            return Collections.singleton(((Var) expr).getName());
        } else if(expr instanceof UnaryValueOperator) {
            return getValueExpressionVariables(((UnaryValueOperator) expr).getArg());
        } else if(expr instanceof BinaryValueOperator) {
            return Sets.union(getValueExpressionVariables(((BinaryValueOperator) expr).getLeftArg()),
                    getValueExpressionVariables(((BinaryValueOperator) expr).getRightArg()));
        } else if(expr instanceof NAryValueOperator) {
            Set<String> result = new HashSet<String>();
            for(ValueExpr e : ((NAryValueOperator) expr).getArguments()) {
                result.addAll(getValueExpressionVariables(e));
            }
            return result;
        } else if(expr instanceof Bound) {
            return getValueExpressionVariables(((Bound) expr).getArg());
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Return aliases to use for variables with the given name
     * @param name
     * @return
     */
    protected static String getNodeName(String name) {
        return "n_"+name.replace("-","_");
    }


    /**
     * Return the value of the node table instance passed as argument
     *
     * @param part
     * @param typeCasting
     * @return
     */
    protected Field getValue(QueryPart part, boolean typeCasting) {

        if(part instanceof TNodes) {
            TNodes table = (TNodes)part;

            if(typeCasting && !resourceVariables.contains(table.getName())) {
                return create.coalesce(table.CONTENT, table.URI, table.ANONID);
            } else if(resourceVariables.contains(table.getName())) {
                return table.URI;
            } else {
                return table.CONTENT;
            }
        } else if(part instanceof Field) {
            return (Field)part;
        } else if(part instanceof Condition) {
            return create.decode().when(((Condition)part),create.val(true))
                    .otherwise(create.val(false));
        } else {
            throw new IllegalArgumentException("unsupported query part for value exctraction: "+part.getClass().getName());
        }
    }

    protected Field<Double> getDoubleValue(QueryPart part, boolean typeCasting) {
        if(part instanceof TNodes) {
            TNodes table = (TNodes)part;

            if(typeCasting) {
                return create.coalesce(table.DOUBLECONTENT,table.INTCONTENT.cast(SQLDataType.DOUBLE),table.CONTENT.cast(SQLDataType.DOUBLE));
            } else {
                return table.DOUBLECONTENT;
            }
        } else if(part instanceof Field) {
            return ((Field)part).cast(SQLDataType.DOUBLE);
        } else {
            throw new IllegalArgumentException("unsupported query part for value exctraction: "+part.getClass().getName());
        }
    }

    protected Field<Long> getIntegerValue(QueryPart part, boolean typeCasting) {
        if(part instanceof TNodes) {
            TNodes table = (TNodes)part;
            if(typeCasting) {
                return create.coalesce(table.INTCONTENT,table.DOUBLECONTENT.cast(SQLDataType.BIGINT),table.CONTENT.cast(SQLDataType.BIGINT));
            } else {
                return table.INTCONTENT;
            }
        } else if(part instanceof Field) {
            return ((Field)part).cast(SQLDataType.BIGINT);
        } else {
            throw new IllegalArgumentException("unsupported query part for value exctraction: "+part.getClass().getName());
        }
    }

    protected Field<Timestamp> getDateValue(QueryPart part, boolean typeCasting) {
        if(part instanceof TNodes) {
            TNodes table = (TNodes)part;

            if(typeCasting) {
                return create.coalesce(table.DATECONTENT,table.CONTENT.cast(SQLDataType.TIMESTAMP));
            } else {
                return ((TNodes) part).DATECONTENT;
            }
        } else if(part instanceof Field) {
            return ((Field)part).cast(SQLDataType.TIMESTAMP);
        } else {
            throw new IllegalArgumentException("unsupported query part for value exctraction: "+part.getClass().getName());
        }
    }


    /**
     * Get all possible values from a query part. In case this is a table, these are the fields content, uri, doublecontent
     * and intcontent. In case it is a field, it is just the field.
     *
     * This method differs from the simple getValue in that it does not create a complex CASE statement and allows
     * more efficient evaluation in some cases.
     *
     * @param part
     * @param stringOnly
     * @return
     */
    protected Set<Field> getAlternativeValues(QueryPart part, boolean stringOnly) {
        if(part instanceof TNodes) {
            TNodes table = (TNodes)part;

            HashSet<Field> result = new HashSet<Field>();
            result.add(table.URI);
            result.add(table.CONTENT);
            if(!stringOnly) {
                result.add(table.DOUBLECONTENT);
                result.add(table.INTCONTENT);
                result.add(table.DATECONTENT);
            }

            return result;


        } else if(part instanceof Field) {
            return Collections.singleton((Field) part);
        } else if(part instanceof Condition && !stringOnly) {
            return Collections.singleton((Field) create.decode().when(((Condition) part), create.val(true)).otherwise(create.val(false)));
        } else {
            throw new IllegalArgumentException("unsupported query part for value exctraction: "+part.getClass().getName());
        }

    }


    // keep track of variables that have been created during the processing of the abstract syntax tree so we do not add them twice
    protected TNodes getVariable(String name) {
        return variables.get(name);
    }

    protected TNodes createVariable(String name) {
        TNodes variable = getVariable(name);
        if(variable == null) {
            variable = T_NODES.as(getNodeName(name));
            variables.put(name,variable);
        }
        return variable;
    }

    protected boolean hasQueryVariable(Select query, String name) {
        return currentVariables.containsKey(name);
    }

    protected void addQueryVariable(Select query, String name, TNodes var) {
        currentVariables.put(name,var);
    }


    protected boolean hasSelectionVariable(Select query, String name) {
        for(Object f : query.getSelect()) {
            if(f instanceof Field && ((Field) f).toString().equals(name)) {
                return true;
            }
        }
        return false;
    }


    private static enum MergeType {
        UNION, INTERSECTION, DIFFERENCE
    }

    public static enum CastType {
        STRICT, // do strict casting in all possible situations (performance impact)
        LOOSE,  // do a simple kind of type casting for literals (small performance impact)
        NONE    // no type casting, rely on correct queries and data
    }


    private static class FindResourceVariables extends QueryModelVisitorBase<VisitorException> {
        private Set<String> resourceVariables;

        private FindResourceVariables(TupleExpr expr) {
            this.resourceVariables = new HashSet<String>();

            try {
                expr.visit(this);
            } catch (VisitorException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


        public Set<String> getResourceVariables() {
            return resourceVariables;
        }

        @Override
        public void meet(StatementPattern node) throws VisitorException {

            Var subject = node.getSubjectVar();
            if(!subject.hasValue()) {
                String varname = getNodeName(subject.getName().replace("-","_"));
                resourceVariables.add(varname);
            }

            Var property = node.getPredicateVar();
            if(!property.hasValue()) {
                String varname = getNodeName(property.getName().replace("-","_"));
                resourceVariables.add(varname);
            }

            Var ctx = node.getContextVar();
            if(ctx != null && !ctx.hasValue()) {
                String varname = getNodeName(ctx.getName().replace("-","_"));
                resourceVariables.add(varname);
            }

        }
    }

}
