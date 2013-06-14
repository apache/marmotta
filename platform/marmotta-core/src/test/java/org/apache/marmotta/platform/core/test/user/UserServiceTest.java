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
package org.apache.marmotta.platform.core.test.user;

import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.UserExistsException;
import org.apache.marmotta.platform.core.test.base.EmbeddedMarmotta;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class UserServiceTest {

    private static EmbeddedMarmotta marmotta;
    private static UserService userService;

    @BeforeClass
    public static void setUp() {
        marmotta = new EmbeddedMarmotta();
        userService = marmotta.getService(UserService.class);
    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testAnonymousUser() {
        Assert.assertNotNull(userService.getAnonymousUser());
        Assert.assertTrue(userService.isAnonymous(userService.getAnonymousUser()));
        Assert.assertTrue(userService.getAnonymousUser().stringValue().endsWith("anonymous"));
    }

    @Test
    public void testAdminUser() {
        Assert.assertNotNull(userService.getAdminUser());
        Assert.assertFalse(userService.isAnonymous(userService.getAdminUser()));
        Assert.assertTrue(userService.getAdminUser().stringValue().endsWith("admin"));
    }

    @Test
    public void testUserExists() {
        String login = RandomStringUtils.randomAlphabetic(8);

        Assert.assertFalse(userService.userExists(login));
        Assert.assertNull(userService.getUser(login));
    }

    @Test
    public void testCreateUser() {
        String login = RandomStringUtils.randomAlphabetic(8);

        try {
            URI user = userService.createUser(login);
            Assert.assertNotNull(user);
            Assert.assertFalse(userService.isAnonymous(user));
            Assert.assertTrue(user.stringValue().endsWith(login));
            Assert.assertTrue(userService.userExists(login));
            Assert.assertNotNull(userService.getUser(login));
        } catch (UserExistsException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
