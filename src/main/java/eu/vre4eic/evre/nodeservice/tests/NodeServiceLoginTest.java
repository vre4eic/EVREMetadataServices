/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
