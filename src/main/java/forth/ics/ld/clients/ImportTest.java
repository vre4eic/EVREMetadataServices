/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.ld.clients;

import java.io.File;
import java.io.IOException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.client.ClientProtocolException;
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

    /**
     * Imports an RDF like file on the server using post synchronously
     *
     * @param file A String holding the path of the file, the contents of which
     * will be uploaded.
     * @param format	The RDF format
     * @param namespace A String representation of the nameSpace
     * @param namedGraph A String representation of the nameGraph
     * @return A response from the service.
     */
    public Response importFile(String file, String format, String namespace, String namedGraph)
            throws ClientProtocolException, IOException {
        String restURL = baseURI + "/import/namespace/" + namespace;
        // Taking into account nameSpace in the construction of the URL
        if (namespace != null) {
            restURL = baseURI + "/import/namespace/" + namespace;
        } else {
            restURL = baseURI + "/import";
        }
        // Taking into account nameGraph in the construction of the URL
        if (namedGraph != null) {
            restURL = restURL + "?namegraph=" + namedGraph;
        }
        String mimeType = format;
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(restURL).queryParam("namegraph", namedGraph);
        Response response = webTarget.request().post(Entity.entity(new File(file), mimeType));// .form(form));
        return response;
    }

    public static void main(String[] args) throws IOException {
        String baseURI = "http://83.212.97.61:8080/ld-services-1.0-SNAPSHOT";
        baseURI = "http://localhost:8181/ld-services";
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
//        Response importResponse = imp.importFilePOSTJSON(request.toString());

        //Vangelis service test
        String namespace = "quads_repo";
        Response importResponse = imp.importFile(
                folder + "/cidoc_v3.2.1.rdfs", // file
                "application/rdf+xml", // content type
                namespace, // namespace
                "http://vangelis"); // nameGraph
        System.out.println("--- Trying to import from file ---");
        System.out.println("Status: " + importResponse.getStatus() + " " + importResponse.getStatusInfo());
        System.out.println(importResponse.readEntity(String.class));

        imp.close();
    }

}
