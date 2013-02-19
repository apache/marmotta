/*
 * Copyright 2012 Salzburg Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.newmedialab.lmf.client.test.classification;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.clients.ClassificationClient;
import at.newmedialab.lmf.client.clients.ResourceClient;
import at.newmedialab.lmf.client.model.classification.Classification;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ClassificationIT {


    private static ClientConfiguration config;

    @BeforeClass
    public static void init() {
        config = new ClientConfiguration("http://localhost:8080/LMF");

    }


    @Ignore
    @Test
    public void testCreateListDelete() throws Exception {
        ClassificationClient client = new ClassificationClient(config);

        String id = "testclassify";
        client.createClassifier(id.toString());

        Assert.assertThat(client.listClassifiers(), Matchers.hasItem(id.toString()));

        client.removeClassifier(id.toString(),true);

        Assert.assertTrue(client.listClassifiers().size() == 0);
    }

    @Ignore
    @Test
    public void testCreateAndTrainClassifier() throws Exception {
        ClassificationClient client = new ClassificationClient(config);
        ResourceClient resourceClient = new ResourceClient(config);

        resourceClient.createResource("http://www.example.com/Concept1");
        resourceClient.createResource("http://www.example.com/Concept2");

        String id = "testclassify";
        client.createClassifier(id.toString());

        client.trainClassifier(id,"http://www.example.com/Concept1","Major acquisitions that have a lower gross margin than the existing network also " +
                "           had a negative impact on the overall gross margin, but it should improve following " +
                "           the implementation of its integration strategies .");
        client.trainClassifier(id,"http://www.example.com/Concept2","The upward movement of gross margin resulted from amounts pursuant to adjustments " +
                "           to obligations towards dealers .");

        client.retrainClassifier(id);

        Thread.sleep(10000);
        client.removeClassifier(id,true);
    }


    @Ignore
    @Test
    public void testCreateAndTrainAndClassifyClassifier() throws Exception {
        ClassificationClient client = new ClassificationClient(config);
        ResourceClient resourceClient = new ResourceClient(config);

        resourceClient.createResource("http://www.example.com/Concept1");
        resourceClient.createResource("http://www.example.com/Concept2");

        String id = "testclassify";
        client.createClassifier(id.toString());

        client.trainClassifier(id,"http://www.example.com/Concept1","Major acquisitions that have a lower gross margin than the existing network also " +
                "           had a negative impact on the overall gross margin, but it should improve following " +
                "           the implementation of its integration strategies .");
        client.trainClassifier(id,"http://www.example.com/Concept2","The upward movement of gross margin resulted from amounts pursuant to adjustments " +
                "           to obligations towards dealers .");

        client.retrainClassifier(id);


        List<Classification> result = client.getAllClassifications(id,"Major acquisitions that have a lower gross margin than the existing network");
        Assert.assertTrue(result.size()==2);
        Assert.assertTrue(result.get(0).getCategory().getUri().equals("http://www.example.com/Concept1"));


        client.removeClassifier(id,true);
    }

}
