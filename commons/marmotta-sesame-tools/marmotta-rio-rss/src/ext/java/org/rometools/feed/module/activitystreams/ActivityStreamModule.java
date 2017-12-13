/*
 *  Copyright 2011 robert.cooper.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.rometools.feed.module.activitystreams;

import org.rometools.feed.module.activitystreams.types.ActivityObject;
import org.rometools.feed.module.activitystreams.types.Mood;
import org.rometools.feed.module.activitystreams.types.Verb;

/**
 *
 * @author robert.cooper
 */
public interface ActivityStreamModule {

    Verb getVerb();
    void setVerb(Verb verb);

    ActivityObject getObject();
    void setObject(ActivityObject object);

    ActivityObject getTarget();
    void setTarget(ActivityObject object);

    Mood getMood();
    void setMood(Mood mood);


}
