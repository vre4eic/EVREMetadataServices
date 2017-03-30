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

    public Response updateExecPOSTJSON(String requestEntity) throws ClientErrorException {
        return webTarget.request(MediaType.TEXT_HTML).post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON), Response.class);
    }

    public void close() {
        client.close();
    }

    public static void main(String[] args) {
        String baseURI = "http://localhost:8181/ld-services";

        UpdateTest test = new UpdateTest(baseURI);
        String query = "insert data {graph <http://test> {<http://a3> <http://p3> <http://b3>.} }";
        JSONObject json = new JSONObject();
        json.put("query", query);
        Response resp = test.updateExecPOSTJSON(json.toJSONString());
        System.out.println(resp.readEntity(String.class));
        System.out.println(resp.getStatus());
        test.close();

    }

}
