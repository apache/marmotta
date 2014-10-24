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
package org.apache.marmotta.platform.ldf.vocab;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Namespace Hydra.
 * Prefix: {@code <http://www.w3.org/ns/hydra/core#>}
 */
public class HYDRA {

    /** {@code http://www.w3.org/ns/hydra/core#} **/
    public static final String NAMESPACE = "http://www.w3.org/ns/hydra/core#";

    /** {@code hydra} **/
    public static final String PREFIX = "hydra";

    /**
     * ApiDocumentation
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#ApiDocumentation}.
     * <p>
     * The Hydra API documentation class
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#ApiDocumentation">ApiDocumentation</a>
     */
    public static final URI ApiDocumentation;

    /**
     * apiDocumentation
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#apiDocumentation}.
     * <p>
     * A link to the API documentation
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#apiDocumentation">apiDocumentation</a>
     */
    public static final URI apiDocumentation;

    /**
     * Hydra Class
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#Class}.
     * <p>
     * The class of Hydra classes. Hydra classes and their instances are
     * dereferenceable resources.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#Class">Class</a>
     */
    public static final URI Class;

    /**
     * Collection
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#Collection}.
     * <p>
     * A collection holding references to a number of related resources.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#Collection">Collection</a>
     */
    public static final URI Collection;

    /**
     * CreateResourceOperation
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#CreateResourceOperation}.
     * <p>
     * A CreateResourceOperation is a HTTP operation which expects an input
	 * of the type specified by hydra:expects and creates a resource of the
	 * type specified by hydra:returns.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#CreateResourceOperation">CreateResourceOperation</a>
     */
    public static final URI CreateResourceOperation;

    /**
     * DeleteResourceOperation
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#DeleteResourceOperation}.
     * <p>
     * A DeleteResourceOperation is a HTTP operation that deletes a resource.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#DeleteResourceOperation">DeleteResourceOperation</a>
     */
    public static final URI DeleteResourceOperation;

    /**
     * description
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#description}.
     * <p>
     * A description.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#description">description</a>
     */
    public static final URI description;

    /**
     * entrypoint
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#entrypoint}.
     * <p>
     * A link to main entry point of the Web API
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#entrypoint">entrypoint</a>
     */
    public static final URI entrypoint;

    /**
     * Error
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#Error}.
     * <p>
     * A runtime error, used to report information beyond the returned status
	 * code.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#Error">Error</a>
     */
    public static final URI Error;

    /**
     * expects
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#expects}.
     * <p>
     * The information expected by the Web API.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#expects">expects</a>
     */
    public static final URI expects;

    /**
     * first page
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#firstPage}.
     * <p>
     * The first page of an interlinked set of PagedCollections
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#firstPage">firstPage</a>
     */
    public static final URI firstPage;

    /**
     * freetext query
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#freetextQuery}.
     * <p>
     * A property representing a freetext query.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#freetextQuery">freetextQuery</a>
     */
    public static final URI freetextQuery;

    /**
     * IRI Template
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#IriTemplate}.
     * <p>
     * The class of IRI templates.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#IriTemplate">IriTemplate</a>
     */
    public static final URI IriTemplate;

    /**
     * IriTemplateMapping
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#IriTemplateMapping}.
     * <p>
     * A mapping from an IRI template variable to a property.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#IriTemplateMapping">IriTemplateMapping</a>
     */
    public static final URI IriTemplateMapping;

    /**
     * items per page
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#itemsPerPage}.
     * <p>
     * The maximum number of items referenced by each single PagedCollection
	 * in a set of interlinked PagedCollections.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#itemsPerPage">itemsPerPage</a>
     */
    public static final URI itemsPerPage;

    /**
     * last page
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#lastPage}.
     * <p>
     * The last page of an interlinked set of PagedCollections
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#lastPage">lastPage</a>
     */
    public static final URI lastPage;

    /**
     * Link
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#Link}.
     * <p>
     * The class of properties representing links.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#Link">Link</a>
     */
    public static final URI Link;

    /**
     * mapping
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#mapping}.
     * <p>
     * A variable-to-property mapping of the IRI template.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#mapping">mapping</a>
     */
    public static final URI mapping;

    /**
     * member
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#member}.
     * <p>
     * A member of the collection
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#member">member</a>
     */
    public static final URI member;

    /**
     * method
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#method}.
     * <p>
     * The HTTP method.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#method">method</a>
     */
    public static final URI method;

    /**
     * next page
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#nextPage}.
     * <p>
     * The page following the current instance in an interlinked set of
	 * PagedCollections
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#nextPage">nextPage</a>
     */
    public static final URI nextPage;

    /**
     * Operation
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#Operation}.
     * <p>
     * An operation.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#Operation">Operation</a>
     */
    public static final URI Operation;

    /**
     * operation
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#operation}.
     * <p>
     * An operation supported by the Hydra resource
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#operation">operation</a>
     */
    public static final URI operation;

    /**
     * PagedCollection
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#PagedCollection}.
     * <p>
     * A PagedCollection is a subclass of Collection with the only difference
	 * that its members are sorted and only a subset of all members are
	 * returned in a single PagedCollection. To get the other members, the
	 * nextPage/previousPage properties have to be used.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#PagedCollection">PagedCollection</a>
     */
    public static final URI PagedCollection;

    /**
     * previous page
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#previousPage}.
     * <p>
     * The page preceding the current instance in an interlinked set of
	 * PagedCollections
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#previousPage">previousPage</a>
     */
    public static final URI previousPage;

    /**
     * property
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#property}.
     * <p>
     * A property
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#property">property</a>
     */
    public static final URI property;

    /**
     * ready-only
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#readonly}.
     * <p>
     * True if the property is read-only, false otherwise.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#readonly">readonly</a>
     */
    public static final URI readonly;

    /**
     * ReplaceResourceOperation
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#ReplaceResourceOperation}.
     * <p>
     * A ReplaceResourceOperation is a HTTP operation which overwrites a
	 * resource. It expects data of the type specified in hydra:expects and
	 * results in a resource of the type specified by hydra:returns.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#ReplaceResourceOperation">ReplaceResourceOperation</a>
     */
    public static final URI ReplaceResourceOperation;

    /**
     * required
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#required}.
     * <p>
     * True if the property is required, false otherwise.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#required">required</a>
     */
    public static final URI required;

    /**
     * Hydra Resource
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#Resource}.
     * <p>
     * The class of dereferenceable resources.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#Resource">Resource</a>
     */
    public static final URI Resource;

    /**
     * returns
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#returns}.
     * <p>
     * The information returned by the Web API on success
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#returns">returns</a>
     */
    public static final URI returns;

    /**
     * search
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#search}.
     * <p>
     * A IRI template that can be used to query a collection
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#search">search</a>
     */
    public static final URI search;

    /**
     * status code
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#statusCode}.
     * <p>
     * The HTTP status code
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#statusCode">statusCode</a>
     */
    public static final URI statusCode;

    /**
     * Status code description
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#StatusCodeDescription}.
     * <p>
     * Additional information about a status code that might be returned.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#StatusCodeDescription">StatusCodeDescription</a>
     */
    public static final URI StatusCodeDescription;

    /**
     * status codes
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#statusCodes}.
     * <p>
     * Additional information about status codes that might be returned by
	 * the Web API
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#statusCodes">statusCodes</a>
     */
    public static final URI statusCodes;

    /**
     * supported classes
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#supportedClass}.
     * <p>
     * A class known to be supported by the Web API
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#supportedClass">supportedClass</a>
     */
    public static final URI supportedClass;

    /**
     * supported operation
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#supportedOperation}.
     * <p>
     * An operation supported by instances of the specific Hydra class or the
	 * target of the Hydra link
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#supportedOperation">supportedOperation</a>
     */
    public static final URI supportedOperation;

    /**
     * supported properties
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#supportedProperty}.
     * <p>
     * The properties known to be supported by a Hydra class
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#supportedProperty">supportedProperty</a>
     */
    public static final URI supportedProperty;

    /**
     * Supported Property
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#SupportedProperty}.
     * <p>
     * A property known to be supported by a Hydra class.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#SupportedProperty">SupportedProperty</a>
     */
    public static final URI SupportedProperty;

    /**
     * template
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#template}.
     * <p>
     * An IRI template as defined by RFC6570.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#template">template</a>
     */
    public static final URI template;

    /**
     * Templated Link
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#TemplatedLink}.
     * <p>
     * A templated link.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#TemplatedLink">TemplatedLink</a>
     */
    public static final URI TemplatedLink;

    /**
     * title
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#title}.
     * <p>
     * A title, often used along with a description.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#title">title</a>
     */
    public static final URI title;

    /**
     * total items
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#totalItems}.
     * <p>
     * The total number of items referenced by a collection or a set of
	 * interlinked PagedCollections.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#totalItems">totalItems</a>
     */
    public static final URI totalItems;

    /**
     * variable
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#variable}.
     * <p>
     * An IRI template variable
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#variable">variable</a>
     */
    public static final URI variable;

    /**
     * write-only
     * <p>
     * {@code http://www.w3.org/ns/hydra/core#writeonly}.
     * <p>
     * True if the property is write-only, false otherwise.
     *
     * @see <a href="http://www.w3.org/ns/hydra/core#writeonly">writeonly</a>
     */
    public static final URI writeonly;

    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();

        ApiDocumentation = factory.createURI(HYDRA.NAMESPACE, "ApiDocumentation");
        apiDocumentation = factory.createURI(HYDRA.NAMESPACE, "apiDocumentation");
        Class = factory.createURI(HYDRA.NAMESPACE, "Class");
        Collection = factory.createURI(HYDRA.NAMESPACE, "Collection");
        CreateResourceOperation = factory.createURI(HYDRA.NAMESPACE, "CreateResourceOperation");
        DeleteResourceOperation = factory.createURI(HYDRA.NAMESPACE, "DeleteResourceOperation");
        description = factory.createURI(HYDRA.NAMESPACE, "description");
        entrypoint = factory.createURI(HYDRA.NAMESPACE, "entrypoint");
        Error = factory.createURI(HYDRA.NAMESPACE, "Error");
        expects = factory.createURI(HYDRA.NAMESPACE, "expects");
        firstPage = factory.createURI(HYDRA.NAMESPACE, "firstPage");
        freetextQuery = factory.createURI(HYDRA.NAMESPACE, "freetextQuery");
        IriTemplate = factory.createURI(HYDRA.NAMESPACE, "IriTemplate");
        IriTemplateMapping = factory.createURI(HYDRA.NAMESPACE, "IriTemplateMapping");
        itemsPerPage = factory.createURI(HYDRA.NAMESPACE, "itemsPerPage");
        lastPage = factory.createURI(HYDRA.NAMESPACE, "lastPage");
        Link = factory.createURI(HYDRA.NAMESPACE, "Link");
        mapping = factory.createURI(HYDRA.NAMESPACE, "mapping");
        member = factory.createURI(HYDRA.NAMESPACE, "member");
        method = factory.createURI(HYDRA.NAMESPACE, "method");
        nextPage = factory.createURI(HYDRA.NAMESPACE, "nextPage");
        Operation = factory.createURI(HYDRA.NAMESPACE, "Operation");
        operation = factory.createURI(HYDRA.NAMESPACE, "operation");
        PagedCollection = factory.createURI(HYDRA.NAMESPACE, "PagedCollection");
        previousPage = factory.createURI(HYDRA.NAMESPACE, "previousPage");
        property = factory.createURI(HYDRA.NAMESPACE, "property");
        readonly = factory.createURI(HYDRA.NAMESPACE, "readonly");
        ReplaceResourceOperation = factory.createURI(HYDRA.NAMESPACE, "ReplaceResourceOperation");
        required = factory.createURI(HYDRA.NAMESPACE, "required");
        Resource = factory.createURI(HYDRA.NAMESPACE, "Resource");
        returns = factory.createURI(HYDRA.NAMESPACE, "returns");
        search = factory.createURI(HYDRA.NAMESPACE, "search");
        statusCode = factory.createURI(HYDRA.NAMESPACE, "statusCode");
        StatusCodeDescription = factory.createURI(HYDRA.NAMESPACE, "StatusCodeDescription");
        statusCodes = factory.createURI(HYDRA.NAMESPACE, "statusCodes");
        supportedClass = factory.createURI(HYDRA.NAMESPACE, "supportedClass");
        supportedOperation = factory.createURI(HYDRA.NAMESPACE, "supportedOperation");
        supportedProperty = factory.createURI(HYDRA.NAMESPACE, "supportedProperty");
        SupportedProperty = factory.createURI(HYDRA.NAMESPACE, "SupportedProperty");
        template = factory.createURI(HYDRA.NAMESPACE, "template");
        TemplatedLink = factory.createURI(HYDRA.NAMESPACE, "TemplatedLink");
        title = factory.createURI(HYDRA.NAMESPACE, "title");
        totalItems = factory.createURI(HYDRA.NAMESPACE, "totalItems");
        variable = factory.createURI(HYDRA.NAMESPACE, "variable");
        writeonly = factory.createURI(HYDRA.NAMESPACE, "writeonly");
    }

    private HYDRA() {
        //static access only
    }

}
