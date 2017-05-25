/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
