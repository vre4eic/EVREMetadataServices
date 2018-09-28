/*
 * Copyright 2018 rousakis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.vre4eic.evre.metadata.clients.usecases;

import eu.vre4eic.evre.core.Common;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.PublisherFactory;
import eu.vre4eic.evre.core.messages.MetadataMessage;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import org.json.JSONObject;

/**
 *
 * @author rousakis
 */
public class InsertWorkflowTest {

    public static void main(String[] args) {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOb2RlU2VydmljZSIsInVzZXJJZCI6Im1hdGgifQ.JK2TzOSTAX9M-90mBOBgN_AGoashunSXnhaLTPwJZnA";
        //create a INSERTWORKFLOW metadata message
        MetadataMessageImpl mmi = new MetadataMessageImpl("description of the operation, for instance: Query on the graph yy", Common.ResponseStatus.SUCCEED);
        mmi.setOperation(Common.MetadataOperationType.INSERTWORKFLOW);
        mmi.setToken(token);
        //create the workflow description
        JSONObject ob = new JSONObject();
        ob.put("wf_name", "Final Review WF");
        ob.put("wf_creator", "Maria Th");
        ob.put("wf_description", "This is the demo workflow created during the review");
        ob.put("user_name", "math");
        ob.put("url", "/Users/maria/VRE4EIC_demo_1/FR WF.evreflow");
        ob.put("token", token);// this is probably useless, can be ignored and not stored

        mmi.setJsonMessage(ob);

        //get a publisher for MetadataMessages
        Publisher<MetadataMessage> p = PublisherFactory.getMetatdaPublisher();

        //publish the message; it will be received by all services that have a listener on this asynchronous communication channel 
        p.publish(mmi);
    }

}
