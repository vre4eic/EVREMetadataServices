/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.clients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
     * @param content A String holding the path of the file, the contents of
     * which will be uploaded.
     * @param format	The RDF format
     * @param namespace A String representation of the nameSpace
     * @param namedGraph A String representation of the nameGraph
     * @return A response from the service.
     */
    public Response importFile(String content, String format, String namespace, String namedGraph)
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
            restURL = restURL + "?graph=" + namedGraph;
        }
        String mimeType = format;
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(restURL).queryParam("graph", namedGraph);
        Response response = webTarget.request().post(Entity.entity(content, mimeType));
        return response;
    }

    public static String readFileData(String filename) {
        File f = new File(filename);
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage() + " occured .");
            return null;
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        String baseURI = "http://83.212.97.61:8080/ld-services-1.0-SNAPSHOT";
//        baseURI = "http://localhost:8181/ld-services";
        String folder = "C:/RdfData";
        ImportTest imp = new ImportTest(baseURI);
        JSONObject request = new JSONObject();
        ///
        request.put("filename", folder + "/cidoc_v3.2.1.rdfs");
        request.put("format", "application/rdf+xml");
        request.put("graph", "http://cidoc_2");
        Response importResponse = imp.importFilePOSTJSON(request.toString());
        ///

        //Vangelis service test
//        String namespace = "test";
//        Response importResponse = imp.importFile(readFileData(
//                folder + "/cidoc_v3.2.1.rdfs"), // file
//                "application/rdf+xml", // content type
//                namespace, // namespace
//                "http://cidoc_3"); // nameGraph
//        System.out.println("--- Trying to import from file ---");
//        System.out.println("Status: " + importResponse.getStatus() + " " + importResponse.getStatusInfo());
        System.out.println(importResponse.readEntity(String.class));
        imp.close();
    }

}
