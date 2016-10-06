/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.ld.clients;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.openrdf.rio.RDFFormat;

/**
 * Jersey REST client generated for REST resource:ImportServices [import]<br>
 * USAGE:
 * <pre>
 *        ImportTest client = new ImportTest();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author rousakis
 */
public class ImportTest {

    private WebTarget webTarget;
    private Client client;
    private String baseURI;

    public ImportTest(String baseURI) {
        this.baseURI = baseURI;
        client = ClientBuilder.newClient();
        webTarget = client.target(baseURI).path("import");
    }

    public Response importFilePOSTJSON(String requestEntity) throws ClientErrorException {
        return webTarget.request(MediaType.APPLICATION_JSON).post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON), Response.class);
    }

    public void close() {
        client.close();
    }

    public static void main(String[] args) {
        String baseURI = "http://83.212.97.61:8080/ld-services-1.0-SNAPSHOT";
//        baseURI = "http://localhost:8181/ld-services";
        String folder = "C:/RdfData";
        ImportTest imp = new ImportTest(baseURI);
        JSONObject request = new JSONObject();
        ///
        request.put("filename", folder + "/cidoc_v3.2.1.rdfs");
        request.put("format", "" + "application/rdf+xml");
        request.put("graph", "http://cidoc_2");
        ///
//        request.put("filename", folder + "/_diachron_efo-2.48.nt");
//        request.put("format", "" + RDFFormat.RDFXML);
//        request.put("graph", "http://efo");
//        System.out.println(request);
        Response response = imp.importFilePOSTJSON(request.toString());
        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));

        imp.close();
    }

}
