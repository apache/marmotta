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
package org.apache.marmotta.platform.core.test.resource;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import com.googlecode.jatl.Html;
import org.apache.marmotta.platform.core.test.base.LMFBaseTest;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Resource service rest tests
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class ResourceServiceTest extends LMFBaseTest {

    @Rule
    public ConcurrentRule concurrently = new ConcurrentRule();

    @Rule
    public RepeatingRule repeatedly = new RepeatingRule();

    private final static int THREADS = 5;
    private final static int REPETITIONS = 10;

    @Ignore
    @Test
    public void testCreateResource() {
        
        String resourceId = UUID.randomUUID().toString();

        // try creating the resource
        expect().
                statusCode(201).
        when().
                post("/resource/"+resourceId);


        // check whether resource exists (without accept header), should return 406 and a Content-Type header with
        // possible types
        given().
                header("Accept","type/subtype").
        expect().
                statusCode(406).
                header("Content-Type", Matchers.notNullValue()).
                header("Content-Type", containsString("application/json")).
                header("Content-Type", containsString("application/rdf+xml")).
        when().
                get("/resource/"+resourceId);

        // check redirect
        given().
                redirects().follow(false).
                header("Accept", "application/json").
        expect().
                statusCode(isOneOf(300, 303)).
                header("Location", endsWith("meta/application/json/" + resourceId)).
        when().
                get("/resource/" + resourceId);

        // check the content after manually carrying out the redirect
        given().
                header("Accept","application/json").
        expect().
                statusCode(200).
                header("Content-Type",containsString("application/json")).
                body(equalTo("{}")).
        when().
                get("/meta/application/json/" + resourceId);

        // delete the resource
        expect().
                statusCode(200).
        when().
                delete("/resource/"+resourceId);
    }

    @Ignore
    @Test
    @Concurrent(count = THREADS)
    @Repeating(repetition = REPETITIONS)
    public void testUploadMetadata() {
        String subjectId = UUID.randomUUID().toString();
        String predicateId1 = UUID.randomUUID().toString();
        String predicateId2 = UUID.randomUUID().toString();
        String objectId1 = UUID.randomUUID().toString();
        String objectValue2 = UUID.randomUUID().toString();

        // create resources
        for(String uuid : new String[]{subjectId,predicateId1,predicateId2,objectId1}) {
            expect().
                    statusCode(201).
            when().
                    post("/resource/" + uuid);
        }

        // configure redirects
        given().
                header("Content-Type","application/json").
                content("[true]").
        expect().
                statusCode(200).
        when().
                post("/config/data/linkeddata.redirect.put");


        // create some RDF data
        String rdfData = String.format(
                "<" + baseResource + " + %s> <" + baseResource + " + %s> <" + baseResource + " + %s> . " +
                "<" + baseResource + " + %s> <" + baseResource + " + %s> \"%s\" .",
                subjectId,predicateId1,objectId1,
                subjectId,predicateId2,objectValue2);

        // upload to resource, expect 300/303 redirect and a Location header
        given().
                redirects().follow(false).
                header("Content-Type", "text/rdf+n3").
                body(rdfData).
        expect().
                statusCode(isOneOf(300, 303)).
                header("Location", endsWith("meta/text/rdf+n3/" + subjectId)).
        when().
                put("/resource/" + subjectId);

        // execute redirect and upload data to resource, expect 200 ok
        given().
                header("Content-Type", "text/rdf+n3").
                body(rdfData).
        expect().
                statusCode(200).
        when().
                put("/meta/text/rdf+n3/" + subjectId);

        // check redirect
        given().
                redirects().follow(false).
                header("Accept", "application/json").
        expect().
                statusCode(isOneOf(300, 303)).
                header("Location", endsWith("meta/application/json/" + subjectId)).
        when().
                get("/resource/" + subjectId);

        // check the RDF/JSON content after manually carrying out the redirect
        given().
                header("Accept", "application/json").
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/json")).
                body(baseResource + subjectId + "." + baseResource  + predicateId1 + ".type", hasItems("uri")).
                body(baseResource + subjectId + "." + baseResource + predicateId1 + ".value", hasItems(baseResource + objectId1)).
                body(baseResource + subjectId + "." + baseResource + predicateId2 + ".type", hasItems("literal")).
                body(baseResource + subjectId + "." + baseResource  + predicateId2 + ".value", hasItems(objectValue2)).
        when().
                get("/meta/application/json/" + subjectId);

        // delete the resources
        for(String uuid : new String[]{subjectId,predicateId1,predicateId2,objectId1}) {
            expect().
                    statusCode(200).
            when().
                    delete("/resource/" + uuid);
        }
    }


    /**
     * Test uploading of content to a resource
     * 1. creates a resource
     * 2. uploads content to the resource using PUT with Content-Type header text/html; rel=content; checks for redirect
     *    and successful upload
     * 3. retrieves the content from the resource using GET with Accept: image/jpeg; rel=content and checks for 406 error
     * 4. retrieves the content from the resource using GET with Accept: text/html; rel=content; checks for redirect and
     *    successful retrieval of content
     * 5. deletes resource
     *
     */
    @Ignore
    @Test
    @Concurrent(count = THREADS)
    @Repeating(repetition = REPETITIONS)
    public void testUploadTripleContent() {
        final String subjectId = UUID.randomUUID().toString();

        // create resources
        expect().
                statusCode(201).
        when().
                post("/resource/" + subjectId);

        // configure redirects
        given().
                header("Content-Type","application/json").
                content("[true]").
                expect().
                statusCode(200).
                when().
                post("/config/data/linkeddata.redirect.put");


        // create some HTML content
        StringWriter writer = new StringWriter();
        new Html(writer) {{
            html().
                    head().title().text(subjectId).end().
                    body().
                        h1().text(subjectId).end().
                        p().text("Lorem Ipsum Dolor Sit Amet").end().
            endAll();
        }};
        String htmlContent = writer.toString();


        // upload to resource, expect 300/303 redirect and a Location header
        given().
                redirects().follow(false).
                header("Content-Type","text/html; rel=content").
                body(htmlContent).
        expect().
                statusCode(isOneOf(300, 303)).
                header("Location", endsWith("content/text/html/" + subjectId)).
        when().
                put("/resource/" + subjectId);

        // execute redirect and upload data to resource, expect 200 ok
        given().
                header("Content-Type", "text/html; rel=content").
                body(htmlContent).
        expect().
                statusCode(200).
        when().
                put("/content/text/html/" + subjectId);


        // check redirect for unsupported image/jpeg, should return 406 (unsupported media type)
        given().
                header("Accept", "image/jpeg; rel=content").
        expect().
                statusCode(406).
        when().
                get("/resource/" + subjectId);

        // check redirect for supported HTML
        given().
                redirects().follow(false).
                header("Accept", "text/html; rel=content").
        expect().
                statusCode(isOneOf(300, 303)).
                header("Location", endsWith("content/text/html/" + subjectId)).
        when().
                get("/resource/" + subjectId);

        // check the text/html content after manually carrying out the redirect
        given().
                header("Accept", "text/html; rel=content").
        expect().
                statusCode(200).
                header("Content-Type", containsString("text/html")).
                body("html.head.title",equalToIgnoringWhiteSpace(subjectId)).
                body("html.body.h1",equalToIgnoringWhiteSpace(subjectId)).
        when().
                get("/content/text/html/" + subjectId);

        // delete the resources
        expect().
                statusCode(200).
                when().
                delete("/resource/" + subjectId);
    }


    /**
     * Test uploading of content to a resource
     * 1. creates a resource
     * 2. uploads content to the resource using PUT with Content-Type header text/html; rel=content; checks for redirect
     *    and successful upload
     * 3. retrieves the content from the resource using GET with Accept: image/jpeg; rel=content and checks for 406 error
     * 4. retrieves the content from the resource using GET with Accept: text/html; rel=content; checks for redirect and
     *    successful retrieval of content
     * 5. deletes resource
     *
     */
    @Ignore
    @Test
    @Concurrent(count = THREADS)
    @Repeating(repetition = REPETITIONS)
    public void testUploadFileContent() throws UnsupportedEncodingException {
        final String subjectId = "file://" + basePath + "/resources/testdata/"+UUID.randomUUID().toString()+".html";

        // create resources
        given().
                queryParam("uri",subjectId).
        expect().
                statusCode(201).
        when().
                post("/resource");


        // configure redirects
        given().
                header("Content-Type","application/json").
                content("[true]").
        expect().
                statusCode(200).
        when().
                post("/config/data/linkeddata.redirect.put");




        // create some HTML content
        StringWriter writer = new StringWriter();
        new Html(writer) {{
            html().
                    head().title().text(subjectId).end().
                    body().
                        h1().text(subjectId).end().
                        p().text("Lorem Ipsum Dolor Sit Amet").end().
            endAll();
        }};
        String htmlContent = writer.toString();


        // upload to resource, expect 300/303 redirect and a Location header
        given().
                redirects().follow(false).
                header("Content-Type","text/html; rel=content").
                body(htmlContent).
        expect().
                statusCode(isOneOf(300, 303)).
                header("Location", endsWith("content/text/html?uri=" + URLEncoder.encode(subjectId,"UTF-8"))).
        when().
                put("/resource?uri=" + subjectId);

        // execute redirect and upload data to resource, expect 200 ok
        given().
                header("Content-Type", "text/html; rel=content").
                body(htmlContent).
        expect().
                statusCode(200).
        when().
                put("/content/text/html?uri=" + URLEncoder.encode(subjectId,"UTF-8"));


        // check redirect for unsupported image/jpeg, should return 406 (unsupported media type)
        given().
                header("Accept", "image/jpeg; rel=content").
        expect().
                statusCode(406).
        when().
                get("/resource?uri=" + URLEncoder.encode(subjectId,"UTF-8"));

        // check redirect for supported HTML
        given().
                redirects().follow(false).
                header("Accept", "text/html; rel=content").
        expect().
                statusCode(isOneOf(300, 303)).
                header("Location", endsWith("content/text/html?uri=" + URLEncoder.encode(subjectId,"UTF-8"))).
        when().
                get("/resource?uri=" + URLEncoder.encode(subjectId,"UTF-8"));

        // check the text/html content after manually carrying out the redirect
        given().
                header("Accept", "text/html; rel=content").
        expect().
                statusCode(200).
                header("Content-Type", containsString("text/html")).
                body("html.head.title",equalToIgnoringWhiteSpace(subjectId)).
                body("html.body.h1",equalToIgnoringWhiteSpace(subjectId)).
        when().
                get("/content/text/html?uri=" + URLEncoder.encode(subjectId,"UTF-8"));

        // delete the resources
        /*
        expect().
                statusCode(200).
        when().
                delete("/resource?uri=" + URLEncoder.encode(subjectId,"UTF-8"));
        */
    }

}
