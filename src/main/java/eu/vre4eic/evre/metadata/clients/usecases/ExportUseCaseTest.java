/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.clients.usecases;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ws.rs.ClientErrorException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;

import org.json.simple.parser.ParseException;

public class ExportUseCaseTest {

    private Client client;
    private String baseURI;

    public ExportUseCaseTest(String baseURI) {
        this.baseURI = baseURI;
        client = ClientBuilder.newClient();
    }

    public void close() {
        client.close();
    }

    public Response exportFilePOSTJSON(String requestEntity, String token) throws ClientErrorException {
        WebTarget MSwebTarget = client.target(baseURI).path("export");
        return MSwebTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", token).post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON), Response.class);
    }

    /*
     * This test class executes  the following use case:
     * 
     * 1) Creates a User profile in e-VRE
     * 2) Use the credentials of the user to login into e-VRE
     * 3) Executes an export operation of a specific named graph and returns the exported data
     * 4) Deletes the user profile
     * 
     */
    public static void main(String[] args) throws UnsupportedEncodingException, ParseException {
        String nSBaseURI = "http://v4e-lab.isti.cnr.it:8080/NodeService";
        String baseURI = "http://v4e-lab.isti.cnr.it:8080/MetadataService";
        baseURI = "http://139.91.183.48:8181/EVREMetadataServices";
        NSUseCaseTest ns = new NSUseCaseTest(nSBaseURI);
        ExportUseCaseTest test = new ExportUseCaseTest(baseURI);

        // System.out.println(queryEnc);
        //1- Create a user profile with userid="id_of_user" and 2) login into e-VRE with the user credentials
        String token = ns.createUserAndLogin();

        //3- Export data
        String graph = "http://cidoc_1";
        System.out.println();
        System.out.println("3) Exporting data from graph: " + graph);
        JSONObject request = new JSONObject();
        ///
        request.put("format", "application/rdf+xml");
        request.put("graph", graph);
        Response resp = test.exportFilePOSTJSON(request.toString(), token);
        System.out.println("Export executed, return message is: " + resp.readEntity(String.class));

        //4- Remove the profile from e-VRE
        ns.removeUser(token, "id_of_user");
        test.close();
        ns.close();
    }

}
