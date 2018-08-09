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
package eu.vre4eic.evre.metadata.clients.usecases;

import java.io.UnsupportedEncodingException;
import javax.ws.rs.ClientErrorException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;

public class UpdateUseCaseTest {

    private Client client;
    private String baseURI;

    public UpdateUseCaseTest(String baseURI) {
        this.baseURI = baseURI;
        client = ClientBuilder.newClient();
    }

    public void close() {
        client.close();
    }

    public Response executeUpdatePOSTJSON(String update, String token) throws ClientErrorException {
        JSONObject json = new JSONObject();
        json.put("query", update);
        WebTarget webTarget = client.target(baseURI + "/update/virtuoso");
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", token).post(Entity.json(json.toJSONString()));
    }

    /*
     * This test class executes  the following use case:
     * 
     * 1) Creates a User profile in e-VRE
     * 2) Use the credentials of the user to login into e-VRE
     * 3) Executes an update query and prints the result
     * 4) Deletes the user profile
     * 
     */
    public static void main(String[] args) throws UnsupportedEncodingException, ParseException {
        String nSBaseURI = "http://v4e-lab.isti.cnr.it:8080/NodeService";
        String baseURI = "http://v4e-lab.isti.cnr.it:8080/MetadataService";
        baseURI = "http://139.91.183.48:8181/EVREMetadataServices";

//        baseURI = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
//        baseURI = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT"; //celsius
        NSUseCaseTest ns = new NSUseCaseTest(nSBaseURI);
        UpdateUseCaseTest test = new UpdateUseCaseTest(baseURI);
        String update = "insert data {graph <http://test2> {<http://a3> <http://p3> <http://b3>.} }";

//        query = "WITH <http://ekt-data>\n"
//                + "INSERT {\n"
//                + "  ?org ?project_pub ?pub.\n"
//                + "  ?pub ?pub_project ?org.\n"
//                + "} WHERE {\n"
//                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
//                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
//                + "\n"
//                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?op.\n"
//                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?op.\n"
//                + "     \n"
//                + "  ?op <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
//                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
//                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
//                + "  Bind( IRI( concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Publication/\",encode_for_uri(?role) )) as ?orgunit_pub ).\n"
//                + "  Bind( IRI( concat(\"http://eurocris.org/ontology/cerif#Publication-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?pub_orgunit ).\n"
//                + "}";
        //1- Create a user profile with userid="id_of_user" and 2) login into e-VRE with the user credentials
        String token = ns.createUserAndLogin();

        //3- Execute a query
        System.out.println();
        System.out.println("3) Executing the update query: " + update);
        Response updateResponse = test.executeUpdatePOSTJSON(update, token);
        JSONParser parser = new JSONParser();

//        System.out.println(updateResponse.readEntity(String.class));
        JSONObject message = (JSONObject) parser.parse(updateResponse.readEntity(String.class));
        System.out.println("Update executed, return message is: " + message.get("message"));
        //4- Remove the profile from e-VRE
        ns.removeUser(token, "id_of_user");
        test.close();
        ns.close();
    }

}
