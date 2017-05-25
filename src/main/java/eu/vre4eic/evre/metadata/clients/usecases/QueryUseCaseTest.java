/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.clients.usecases;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;

import org.json.simple.parser.ParseException;

public class QueryUseCaseTest {

    private Client client;
    private String baseURI;

    public QueryUseCaseTest(String baseURI) {
        this.baseURI = baseURI;
        client = ClientBuilder.newClient();
    }

    public void close() {
        client.close();
    }

    public Response executeSparqlQuery(String queryStr, String namespace, String format, String token) throws UnsupportedEncodingException {//QueryResultFormat format) throws UnsupportedEncodingException {
        //String mimetype = Utilities.fetchQueryResultMimeType(format);

        WebTarget webTarget = client.target(baseURI + "/query/namespace/" + namespace).
                queryParam("format", format).//mimetype
                queryParam("query", URLEncoder.encode(queryStr, "UTF-8").
                        replaceAll("\\+", "%20"));
        // System.out.println("----------> " + webTarget.getUri());
        Invocation.Builder invocationBuilder = webTarget.request().
                header("Authorization", token);//.request(mimetype);
        Response response = invocationBuilder.get();
        return response;
    }

    /*
     * This test class executes  the following use case:
     * 
     * 1) Creates a User profile in e-VRE
     * 2) Use the credentials of the user to login into e-VRE
     * 3) Executes a query and prints the result
     * 4) Deletes the user profile
     * 
     */
    public static void main(String[] args) throws UnsupportedEncodingException, ParseException {
        String nSBaseURI = "http://v4e-lab.isti.cnr.it:8080/NodeService";
        String baseURI = "http://v4e-lab.isti.cnr.it:8080/MetadataService";
//        baseURI = "http://139.91.183.48:8181/EVREMetadataServices";
        baseURI = "http://83.212.97.61:8080/EVREMetadataServices-1.0-SNAPSHOT";
        NSUseCaseTest ns = new NSUseCaseTest(nSBaseURI);
        QueryUseCaseTest test = new QueryUseCaseTest(baseURI);
        String query = "select * where {?s ?p ?o} limit 5";
        query = "select distinct ?g where {{graph ?g {?s ?p ?o}}}";
//        query = "SELECT * WHERE {{ ?s ?p ?o . ?s rdfs:label ?o. ?o bds:search 'Quadrelli' . }}";

        //String queryEnc = URLEncoder.encode(query2, "UTF-8").replaceAll("\\+", "%20");
        // System.out.println(queryEnc);
        //1- Create a user profile with userid="id_of_user" and 2) login into e-VRE with the user credentials
        String token = ns.createUserAndLogin();

        //3- Execute a query
        System.out.println();
        System.out.println("3) Executing the query: " + query);
        String namespace = "ekt-data";
        Response queryResponse
                = test.executeSparqlQuery(query, namespace, "application/json", token);//QueryResultFormat.JSON);
        System.out.println("Query executed, return message is: " + queryResponse.readEntity(String.class));

        //4- Remove the profile from e-VRE
        ns.removeUser(token, "id_of_user");
        test.close();
        ns.close();
    }

}
