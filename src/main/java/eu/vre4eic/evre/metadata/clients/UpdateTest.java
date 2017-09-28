/* 
 * Copyright 2017 rousakis.
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
package eu.vre4eic.evre.metadata.clients;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;

/**
 * Jersey REST client generated for REST resource:UpdateServices [update]<br>
 * USAGE:
 * <pre>
 *        UpdateTest client = new UpdateTest();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author rousakis
 */
public class UpdateTest {

    private WebTarget webTarget;
    private Client client;
    private String baseURI;

    public UpdateTest(String baseUri) {
        this.baseURI = baseUri;
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(baseURI).path("update");
    }

    public Response updateExecPOSTJSON(String requestEntity, String token) throws ClientErrorException {
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", token).post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON), Response.class);
    }

    public void close() {
        client.close();
    }

    public static void main(String[] args) {
        String baseURI = "http://139.91.183.48:8181/EVREMetadataServices";
//        baseURI = "http://v4e-lab.isti.cnr.it:8080/MetadataService";
        UpdateTest test = new UpdateTest(baseURI);
        String query = "insert data {graph <http://test2> {<http://a3> <http://p3> <http://b3>.} }";
        JSONObject json = new JSONObject();
        json.put("query", query);
        String token = "rous";
        Response resp = test.updateExecPOSTJSON(json.toJSONString(), token);
        System.out.println(resp.readEntity(String.class));
        System.out.println(resp.getStatus());
        test.close();

    }

}
