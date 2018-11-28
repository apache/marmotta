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
package org.apache.marmotta.platform.ldf.services;

import com.google.common.collect.FluentIterable;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.repository.ResultUtils;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldf.api.LdfService;
import org.apache.marmotta.platform.ldf.vocab.HYDRA;
import org.apache.marmotta.platform.ldf.vocab.RDF;
import org.apache.marmotta.platform.ldf.vocab.SSD;
import org.apache.marmotta.platform.ldf.vocab.VOID;
import org.openrdf.model.*;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Linked Media Fragments service implementation
 *
 * @author Sergio Fernández
 */
public class LdfServiceImpl implements LdfService {

    private static final Logger log = LoggerFactory.getLogger(LdfServiceImpl.class);

    @Inject
    private SesameService sesameService;

    @Override
    public Model getFragment(String subjectStr, String predicateStr, String objectStr, int page, java.net.URI uri) throws RepositoryException, IllegalArgumentException {
        return getFragment(subjectStr, predicateStr, objectStr, null, page, uri);
    }

    @Override
    public Model getFragment(URI subject, URI predicate, Value object, int page, java.net.URI uri) throws RepositoryException, IllegalArgumentException {
        return getFragment(subject, predicate, object, null, page, uri);
    }

    @Override
    public Model getFragment(String subjectStr, String predicateStr, String objectStr, String contextStr, int page, java.net.URI uri) throws RepositoryException, IllegalArgumentException {
        final ValueFactoryImpl vf = new ValueFactoryImpl();

        URI subject = null;
        if (StringUtils.isNotBlank(subjectStr)) {
            try {
                new java.net.URI(subjectStr);
                subject = vf.createURI(subjectStr);
            } catch (URISyntaxException e) {
                log.error("invalid subject '{}': {}", subjectStr, e.getMessage());
            }
        }

        URI predicate = null;
        if (StringUtils.isNotBlank(predicateStr)) {
            try {
                new java.net.URI(predicateStr);
                predicate = vf.createURI(predicateStr);
            } catch (URISyntaxException e) {
                log.error("invalid predicate '{}': {}", predicateStr, e.getMessage());
            }
        }

        Value object = null;
        if (StringUtils.isNotBlank(objectStr)) {
            try {
                new java.net.URI(objectStr);
                object = vf.createURI(objectStr);
            } catch (URISyntaxException e) {
                object = vf.createLiteral(objectStr);
            }
        }

        URI context = null;
        if (StringUtils.isNotBlank(contextStr)) {
            try {
                new java.net.URI(contextStr);
                context = vf.createURI(contextStr);
            } catch (URISyntaxException e) {
                log.error("invalid context '{}': {}", contextStr, e.getMessage());
            }
        }

        return getFragment(subject, predicate, object, context, page, uri);
    }

    @Override
    public Model getFragment(URI subject, URI predicate, Value object, Resource context, int page, java.net.URI uri) throws RepositoryException, IllegalArgumentException {
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();

            //first get the triple fragment for ordering by a fixed criteria
            //TODO: do this effectively
            final RepositoryResult<Statement> results = conn.getStatements(subject, predicate, object, true, context);
            final List<Statement> statements = FluentIterable.from(ResultUtils.iterable(results)).toSortedList(new Comparator<Statement>() {
                @Override
                public int compare(Statement s1, Statement s2) {
                    int subjectComparison = s1.getSubject().stringValue().compareTo(s2.getSubject().stringValue());
                    int predicatedComparison = s1.getPredicate().stringValue().compareTo(s2.getPredicate().stringValue());
                    if (subjectComparison != 0) {
                        return subjectComparison;
                    } else if (predicatedComparison != 0) {
                        return predicatedComparison;
                    } else if ((s1.getObject() instanceof Literal) && (s2.getObject() instanceof Resource)) {
                        return 1;
                    } else if ((s1.getObject() instanceof Resource) && (s2.getObject() instanceof Literal)) {
                        return -1;
                    } else {
                        return s1.getObject().stringValue().compareTo(s2.getObject().stringValue());
                    }
                }
            });
            if (!results.isClosed()) {
                //ResultUtils in theory closes the RepositoryResult connection...
                results.close();
            }

            //then filter
            final int size = statements.size();
            final int offset = LdfService.PAGE_SIZE * (page - 1);

            if (offset > size) {
                throw new IllegalArgumentException("page " + page + " can't be generated, empty fragment");
            }

            final Model model = new TreeModel();
            final ValueFactoryImpl vf = new ValueFactoryImpl();

            final int limit = LdfService.PAGE_SIZE < size - offset ? LdfService.PAGE_SIZE : size - offset;
            List<Statement> filteredStatements = statements.subList(offset, limit);
            if (filteredStatements.isEmpty()) {
                throw new IllegalArgumentException("empty fragment");
            }

            //add the fragment
            model.addAll(filteredStatements);

            //and add ldf metadata
            URI dataset = vf.createURI(UriBuilder.fromUri(uri).replaceQuery(null).build().toASCIIString());
            model.add(dataset, RDF.type, VOID.Dataset);
            model.add(dataset, RDF.type, HYDRA.Collection);
            if (context != null) {
                model.add(dataset, VOID.inDataset, context);
                model.add(dataset, SSD.namedGraph, context);
            }

            Resource fragment = vf.createBNode(String.format("fragment-%tFT%<tH-%<tM-%<tS.%<tL", new Date()));
            model.add(dataset, VOID.subset, fragment);
            model.add(fragment, RDF.type, HYDRA.Collection);
            if (offset != 0 && limit != size) {
                model.add(fragment, RDF.type, HYDRA.PagedCollection);
            }
            model.add(fragment, VOID.triples, vf.createLiteral(Integer.toString(filteredStatements.size()), XSD.Integer));

            //and add hydra controls
            model.add(fragment, HYDRA.totalItems, vf.createLiteral(Integer.toString(filteredStatements.size()), XSD.Integer));
            model.add(fragment, HYDRA.itemsPerPage, vf.createLiteral(Integer.toString(LdfService.PAGE_SIZE), XSD.Integer));
            model.add(fragment, HYDRA.firstPage, vf.createURI(UriBuilder.fromUri(uri).queryParam("page", 1).build().toASCIIString()));
            if (offset > 0) {
                model.add(fragment, HYDRA.previousPage, vf.createURI(UriBuilder.fromUri(uri).queryParam("page", page-1).build().toASCIIString()));
            }
            if (offset + limit < statements.size()) {
                model.add(fragment, HYDRA.nextPage, vf.createURI(UriBuilder.fromUri(uri).queryParam("page", page+1).build().toASCIIString()));
            }
            Resource triplePattern = vf.createBNode("triplePattern");
            model.add(dataset, HYDRA.search, triplePattern);
            model.add(triplePattern, HYDRA.template, vf.createLiteral(dataset.stringValue() + "{?subject,predicate,object}"));
            Resource subjectMapping = vf.createBNode("subjectMapping");
            model.add(triplePattern, HYDRA.mapping, subjectMapping);
            model.add(subjectMapping, HYDRA.variable, vf.createLiteral("subject"));
            model.add(subjectMapping, HYDRA.property, RDF.subject);
            Resource predicateMapping = vf.createBNode("predicateMapping");
            model.add(triplePattern, HYDRA.mapping, predicateMapping);
            model.add(predicateMapping, HYDRA.variable, vf.createLiteral("predicate"));
            model.add(predicateMapping, HYDRA.property, RDF.predicate);
            Resource objectMapping = vf.createBNode("objectMapping");
            model.add(triplePattern, HYDRA.mapping, objectMapping);
            model.add(objectMapping, HYDRA.variable, vf.createLiteral("object"));
            model.add(objectMapping, HYDRA.property, RDF.object);


            return model;

        } finally {
            conn.commit();
            if(conn.isOpen()) {
                conn.close();
            }
        }

    }

}
