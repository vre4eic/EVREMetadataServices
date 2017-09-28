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
import java.net.URLEncoder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rousakis
 */
public class NSUseCaseTest {

    private Client client;
    private String nSBaseURI;

    public NSUseCaseTest(String nSBaseURI) {
        client = ClientBuilder.newClient();
        this.nSBaseURI = nSBaseURI;
    }

    public void close() {
        client.close();
    }

    /**
     * Creates a user profile on the server and log in e-VRE with the user
     * credentials
     *
     * @return The token of the logged-in user
     */
    public String createUserAndLogin() throws UnsupportedEncodingException {

        String token = "";

        Form myForm = new Form();
        myForm.param("userid", "id_of_user");
        myForm.param("name", "name_of_the_user");
        myForm.param("email", "email@_of_the_user");
        myForm.param("organization", "organization_the_user_belongs");
        myForm.param("role", eu.vre4eic.evre.core.Common.UserRole.RESEARCHER.toString());
        myForm.param("password", "pwd_of_the_user");

        System.out.println("1) Creating a User Profile...");
        WebTarget nSwebTarget = client.target(nSBaseURI + "/user/createprofile");

        //create a user profile
        Response prResponse = nSwebTarget.request(MediaType.APPLICATION_JSON).post(Entity.form(myForm));

        System.out.println("... done, result message is: " + prResponse.readEntity(String.class));
        System.out.println();

        System.out.println("2) Executing login... ");

        //log in
        WebTarget webTarget = client.target(nSBaseURI + "/user/login").
                queryParam("username", "id_of_user").//mimetype
                queryParam("pwd", "pwd_of_the_user");
        Invocation.Builder invocationBuilder = webTarget.request();
        Response response = invocationBuilder.get();

        try {
            JSONObject resJO = (JSONObject) new JSONParser().parse(response.readEntity(String.class));
            System.out.println("... done, the the result message is: " + resJO.toJSONString());
            if (resJO.get("status").equals(eu.vre4eic.evre.core.Common.ResponseStatus.SUCCEED.toString())) {
                token = (String) resJO.get("token");
            }
            System.out.println("the vaild token is: " + resJO.get("token"));
            System.out.println();
            token = (String) resJO.get("token");
        } catch (ParseException e) {

            e.printStackTrace();
        }

        return (token);
    }

    /**
     * Removes a user profile on the e-VRE server
     *
     * @return The output of the query
     */
    public void removeUser(String token, String id) throws UnsupportedEncodingException {

        System.out.println();
        System.out.println("4) Remove the User Profile... ");
        WebTarget webTarget = client.target(nSBaseURI + "/user/removeprofile").
                queryParam("token", token).//mimetype
                queryParam("id", id);
        Invocation.Builder invocationBuilder = webTarget.request();
        Response response = invocationBuilder.get();

        System.out.println("removed, result message is: " + response.readEntity(String.class));

    }

}
