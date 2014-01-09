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
package org.apache.marmotta.ldclient.test.youtube;

import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Test;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class TestYoutubeProvider extends ProviderTestBase {

    /**
     * This method tests accessing the Youtube Video service via the GData API.
     *
     * @throws Exception
     */
    @Test
    public void testVideo() throws Exception {
        testResource("http://youtu.be/_3BmNcHW4Ew", "youtube-lmf-video.sparql");
    }

    /**
     * This method tests accessing the Youtube Video service via the GData API.
     *
     * @throws Exception
     */
    @Test
    public void testVideoPage() throws Exception {
        testResource("http://www.youtube.com/watch?v=_3BmNcHW4Ew", "youtube-lmf-video-page.sparql");
    }

    /**
     * This method tests accessing the Youtube Video service via the GData API.
     *
     * @throws Exception
     */
    @Test
    public void testChannel() throws Exception {
        testResource("http://www.youtube.com/user/dieSpringer", "youtube-channel.sparql");
    }

    /**
     * This method tests accessing the Youtube Video service via the GData API.
     *
     * @throws Exception
     */
    @Test
    public void testPlaylist() throws Exception {
        testResource("http://www.youtube.com/playlist?list=FLsrORDOimfQf42SDGJgRY4g", "youtube-playlist.sparql");
    }

}
