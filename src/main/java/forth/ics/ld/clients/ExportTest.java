/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.ld.clients;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.openrdf.rio.RDFFormat;

/**
 * Jersey REST client generated for REST resource:ExportServices [export]<br>
 * USAGE:
 * <pre>
 *        ExportTest client = new ExportTest();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author rousakis
 */
public class ExportTest {

    private WebTarget webTarget;
    private Client client;
    private String baseURI;

    public ExportTest(String baseURI) {
        this.baseURI = baseURI;
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(baseURI).path("export");
    }

    public Response exportFilePOSTJSON(String requestEntity) throws ClientErrorException {
        return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), Response.class);
    }

    public void close() {
        client.close();
    }

    public static void main(String[] args) {
        String baseURI = "http://83.212.97.61:8080/ld-services-1.0-SNAPSHOT";
//        baseURI = "http://localhost:8181/ld-services";
        ExportTest exp = new ExportTest(baseURI);
        JSONObject request = new JSONObject();
        ///
        request.put("format", "application/rdf+xml");
        request.put("graph", "http://lifewatchgreece.com/vsmall");
        Response resp = exp.exportFilePOSTJSON(request.toString());
        System.out.println(resp.readEntity(String.class));
        System.out.println(resp.getStatus());

    }

}
