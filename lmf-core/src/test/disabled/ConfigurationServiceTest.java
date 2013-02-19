/**
 *  Copyright (c) 2012 Salzburg Research.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kiwi.core.test.config;

import com.google.common.collect.Lists;
import kiwi.core.test.base.LMFBaseTest;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class ConfigurationServiceTest extends LMFBaseTest {


    @Ignore
    @Test
    public void testSetConfiguration() throws IOException {
        // set my.key to the values "value1" and "value2"
        given().
                header("Content-Type","application/json").
                content(mapper.writeValueAsString(Lists.newArrayList("value1", "value2"))).
        expect().
                statusCode(200).
        when().
                post("/config/data/mykey");


        // test whether values appear when retrieving the key
        expect().
                statusCode(200).
                body("mykey",hasItems("value1","value2")).
        when().
                get("/config/data/mykey");

        // test whether values appear in full list
        expect().
                statusCode(200).
                body("mykey.value",hasItems("value1","value2")).
        when().
                get("/config/list");


        // test whether deleting returns OK
        expect().
                statusCode(200).
        when().
                delete("/config/data/mykey");


        // test whether values appear when retrieving the key
        expect().
                statusCode(404).
        when().
                get("/config/data/mykey");
    }
}
