/* 
 * Copyright 2017 VRE4EIC Consortium
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
package eu.vre4eic.evre.nodeservice.tests;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rousakis
 */
public class NodeServiceLoginTest {

    public static void main(String[] args) throws ParseException {
        String service = "http://v4e-lab.isti.cnr.it:8080/NodeService/user/login";
        String username = "rous";
        String pwd = "rous";
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(service).queryParam("username", username).queryParam("pwd", pwd);
        Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.readEntity(String.class));
        System.out.println(jsonObject);
        System.out.println("Token: " + jsonObject.get("token"));
    }

}
