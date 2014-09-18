/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.kiwi.test.junit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Specialized {@link Parameterized} runner for UnitTests that injects the database config for KiWi.
 * <p>
 * Except for in-memory databases like H2 or Derby, database URLs must be passed as
 * system property, or otherwise the test is skipped for this database. Available system properties:
 * <ul>
 *     <li>PostgreSQL:
 *     <ul>
 *         <li>postgresql.url, e.g. jdbc:postgresql://localhost:5433/kiwitest?prepareThreshold=3</li>
 *         <li>postgresql.user (default: kiwi)</li>
 *         <li>postgresql.pass (default: kiwi)</li>
 *     </ul>
 *     </li>
 *     <li>MySQL:
 *     <ul>
 *         <li>mysql.url, e.g. jdbc:mysql://localhost:3306/kiwitest?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull</li>
 *         <li>mysql.user (default: kiwi)</li>
 *         <li>mysql.pass (default: kiwi)</li>
 *     </ul>
 *     </li>
 *     <li>H2:
 *     <ul>
 *         <li>h2.url, e.g. jdbc:h2:mem:kiwitest;MVCC=true;DB_CLOSE_ON_EXIT=TRUE</li>
 *         <li>h2.user (default: kiwi)</li>
 *         <li>h2.pass (default: kiwi)</li>
 *     </ul>
 *     </li>
 * </ul>
 * @author Jakob Frank <jakob@apache.org>
 *
 */
public class KiWiDatabaseRunner extends Suite {

    /**
     * Assign the {@link KiWiConfiguration} to all member fields with {@link KiWiConfig}-Annotation.
     * If this annotation is used, the class must only have the default constructor.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface KiWiConfig {     
    }
    
    /**
     * Only execute with {@link KiWiConfiguration}s for these {@link KiWiDialect}s.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public static @interface ForDialects {
        Class<? extends KiWiDialect>[] value();
    }
    
    private final ArrayList<Runner> runners = new ArrayList<Runner>();
    
    private final List<Class<? extends KiWiDialect>> forDialects;
    
    public KiWiDatabaseRunner(Class<?> klass) throws Throwable {
        super(klass, Collections.<Runner>emptyList());

        if(System.getProperty("dialect") != null) {
            ArrayList<Class<? extends KiWiDialect>> forDialects = new ArrayList<>();
            forDialects.add((Class<? extends KiWiDialect>) Class.forName(System.getProperty("dialect")));
            this.forDialects = Collections.unmodifiableList(forDialects);
        } else {
            ForDialects d = klass.getAnnotation(ForDialects.class);
            if (d != null) {
                ArrayList<Class<? extends KiWiDialect>> forDialects = new ArrayList<>();
                for (Class<? extends KiWiDialect> dialect : d.value()) {
                    forDialects.add(dialect);
                }
                this.forDialects = Collections.unmodifiableList(forDialects);
            } else {
                forDialects = null;
            }
        }
        
        createRunners();
    }

    private void createRunners() throws InitializationError {
        List<KiWiConfiguration> configs = new ArrayList<>();
        createKiWiConfig("H2", new H2Dialect(), configs);
        createKiWiConfig("PostgreSQL", new PostgreSQLDialect(), configs);
        createKiWiConfig("MySQL", new MySQLDialect(), configs);

        for (KiWiConfiguration config : configs) {
            final DatabaseTestClassRunner runner = new DatabaseTestClassRunner(getTestClass().getJavaClass(), config);
            runners.add(runner);
        }
    }

    private void createKiWiConfig(String database, KiWiDialect dialect, List<KiWiConfiguration> configs) {
        if (forDialects != null && !forDialects.contains(dialect.getClass())) {
            return;
        }
        KiWiConfiguration c = createKiWiConfig(database, dialect);
        if (c!=null) configs.add(c);
    }
    
    public static KiWiConfiguration createKiWiConfig(String database, KiWiDialect dialect) {
        final KiWiConfiguration config;
        if(!(dialect instanceof H2Dialect) && System.getProperty(database.toLowerCase()+".url") != null) {
            config = new KiWiConfiguration(
                    database,
                    System.getProperty(database.toLowerCase()+".url"),
                    System.getProperty(database.toLowerCase()+".user","kiwi"),
                    System.getProperty(database.toLowerCase()+".pass","kiwi"),
                    dialect);
        } else if (dialect instanceof H2Dialect) {
            config = new KiWiConfiguration(
                    "default-H2", 
                    "jdbc:h2:mem:kiwitest;MVCC=true;DB_CLOSE_ON_EXIT=TRUE;DB_CLOSE_DELAY=-1",
                    "kiwi", "kiwi", 
                    dialect);
        } else {
            return null;
        }
        config.setDefaultContext("http://localhost/context/default");
        config.setInferredContext("http://localhost/context/inferred");
        return config;
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }
    
    private class DatabaseTestClassRunner extends BlockJUnit4ClassRunner {

        private final KiWiConfiguration config;
        
        private final Logger logger;

        private final CheckDBRule checkDB;
        private final ExecutionLogger loggerRule;

        public DatabaseTestClassRunner(Class<?> klass, KiWiConfiguration config)
                throws InitializationError {
            super(klass);
            logger = LoggerFactory.getLogger(klass);
            this.config = config;
            
            checkDB = new CheckDBRule(config);
            loggerRule = new ExecutionLogger();
        }

        @Override
        protected void runChild(FrameworkMethod method, RunNotifier notifier) {
            final ForDialects forD = method.getAnnotation(ForDialects.class);
            if (forD != null) {
                if (ArrayUtils.contains(forD.value(), config.getDialect().getClass())) {
                    super.runChild(method, notifier);
                } else {
                    notifier.fireTestIgnored(describeChild(method));
                }
            } else {
                super.runChild(method, notifier);
            }

        }
        
        @Override
        protected Object createTest() throws Exception {
            if (fieldAnnotated()) {
                Object testInstance = getTestClass().getOnlyConstructor().newInstance();
                List<FrameworkField> configFields = getFieldsAnnotatedByKiWiConfig();
                for (FrameworkField field : configFields) {
                    try {
                        field.getField().set(testInstance, config);
                    } catch (IllegalArgumentException iae) {
                        throw new Exception(getTestClass().getName() + ": Trying to set " + field.getName() + " that has a wrong type.");
                    }
                }
                return testInstance;
            }
            return getTestClass().getOnlyConstructor().newInstance(config);
        }
        
        @Override
        protected List<MethodRule> rules(Object target) {
            LinkedList<MethodRule> rules = new LinkedList<>();
            rules.add(loggerRule);
            rules.addAll(super.rules(target));
            rules.add(checkDB);
            return rules;
        }
        
        @Override
        protected String getName() {
            return "KiWi-Triplestore - " + config.getName();
        }
        
        @Override
        protected String testName(FrameworkMethod method) {
            //return method.getName() + "(" + config.getName() + ")";
            return method.getName();
        }


        
        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
            if (fieldAnnotated()) {
                validateZeroArgConstructor(errors);
            }
        }
        
        @Override
        protected void validateFields(List<Throwable> errors) {
            super.validateFields(errors);
            if (fieldAnnotated()) {
                List<FrameworkField> configFields = getFieldsAnnotatedByKiWiConfig();
                for (FrameworkField field : configFields) {
                    if (!field.getType().isAssignableFrom(KiWiConfiguration.class)) {
                        errors.add(new Exception(String.format("Invalid type %s for field %s, must be %s", field.getType().getName(), field.getName(), KiWiConfiguration.class.getSimpleName())));
                    }
                }
            }
        }
        
        @Override
        protected Statement classBlock(RunNotifier notifier) {
            return childrenInvoker(notifier);
        }

        @Override
        protected Annotation[] getRunnerAnnotations() {
            return new Annotation[0];
        }
        
        private class CheckDBRule implements MethodRule {

            private final AssumptionViolatedException assume;
            private final KiWiConfiguration dbConfig;

            public CheckDBRule(KiWiConfiguration dbConfig) {
                this.dbConfig = dbConfig;
                AssumptionViolatedException ex = null;
                try {
                    DBConnectionChecker.checkDatabaseAvailability(dbConfig);
                } catch (AssumptionViolatedException ave) {
                    ex = ave;
                }
                // Cache the result.
                this.assume = ex;
            }
            
            @Override
            public Statement apply(final Statement base, final FrameworkMethod method,
                    Object target) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        if (assume != null) {
                            if (logger.isDebugEnabled()) {
                                logger.info("{} skipped because database {} ({}) is not available", testName(method), dbConfig.getName(), dbConfig.getJdbcUrl());
                            } else {
                                logger.info("{} skipped because database {} is not available", testName(method), dbConfig.getName());
                            }
                            throw assume;
                        }
                        base.evaluate();
                    }
                };
            }
            
        }
        
        private class ExecutionLogger extends TestWatcher implements MethodRule {


            @Override
            public Statement apply(final Statement base, final FrameworkMethod method,
                    Object target) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        logger.info("{} starting...", testName(method));
                        try {
                            base.evaluate();
                            logger.debug("{} SUCCESS", testName(method));
                        } catch (AssumptionViolatedException e) {
                            logger.info("{} Ignored: {}", testName(method), e.getMessage());
                            throw e;
                        } catch (Throwable t) {
                            logger.warn("{} FAILED: {}", testName(method), t.getMessage());
                            throw t;
                        }
                    }
                };
            }
            
        }
        
    }
    
    private boolean fieldAnnotated() {
        return !getFieldsAnnotatedByKiWiConfig().isEmpty();
    }

    private List<FrameworkField> getFieldsAnnotatedByKiWiConfig() {
        return getTestClass().getAnnotatedFields(KiWiConfig.class);
    }
    
    
}
