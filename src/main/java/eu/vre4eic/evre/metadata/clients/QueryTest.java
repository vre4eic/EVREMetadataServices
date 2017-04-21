/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.clients;

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

/**
 * Jersey REST client generated for REST resource:QueryServices [query]<br>
 * USAGE:
 * <pre>
 *        QueryTest client = new QueryTest();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author rousakis
 */
public class QueryTest {

    private WebTarget webTarget;
    private javax.ws.rs.client.Client client;
    private String baseURI;

    public QueryTest(String baseURI) {
        this.baseURI = baseURI;
        client = ClientBuilder.newClient();
        webTarget = client.target(baseURI).path("query");
    }

    public String queryExecGETJSON(String q, String f) throws ClientErrorException {
        WebTarget resource = webTarget;
        return resource.queryParam("query", q).queryParam("format", f).request().get().readEntity(String.class);
    }

    public String queryExecPOSTJSON(String json) throws ClientErrorException {
        WebTarget resource = webTarget;
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(json)).readEntity(String.class);
    }

    public void close() {
        client.close();
    }

    /**
     * Imports an RDF-like file on the server
     *
     * @param queryStr A String that holds the query to be submitted on the
     * server.
     * @param namespace A String representation of the nameSpace to be used
     * @param format
     * @return The output of the query
     */
    public Response executeSparqlQuery(String queryStr, String namespace, String format, String token) throws UnsupportedEncodingException {//QueryResultFormat format) throws UnsupportedEncodingException {
        //String mimetype = Utilities.fetchQueryResultMimeType(format);
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(baseURI + "/query/namespace/" + namespace).
                queryParam("format", format).//mimetype
                queryParam("query", URLEncoder.encode(queryStr, "UTF-8").
                        replaceAll("\\+", "%20"));
        System.out.println("----------> " + webTarget.getUri());
        Invocation.Builder invocationBuilder = webTarget.request().
                header("Authorization", token);//.request(mimetype);
        Response response = invocationBuilder.get();
        return response;
    }

    public static void main(String[] args) throws UnsupportedEncodingException, ParseException {
        String baseURI = "http://139.91.183.48:8181/EVREMetadataServices";
//        baseURI = "http://v4e-lab.isti.cnr.it:8080/MetadataService";
        QueryTest test = new QueryTest(baseURI);
        String query = "select * where {?s ?p ?o} limit 10";
        String query2 = "SELECT * WHERE {{ ?s ?p ?o . ?s rdfs:label ?o. ?o bds:search 'Quadrelli' . }}";

        String queryEnc = URLEncoder.encode(query2, "UTF-8").replaceAll("\\+", "%20");
        System.out.println(queryEnc);
//        String format = "application/json";
//        JSONObject json = new JSONObject();
//        json.put("query", query);
//        json.put("format", format);
//        System.out.println(test.queryExecGETJSON(query, "application/json"));
//        System.out.println(test.queryExecPOSTJSON(json.toJSONString()));
        String namespace = "ekt-demo";
        String token = "rous";
        Response queryResponse
                = test.executeSparqlQuery(query2, namespace, "text/tab-separated-values", token);//QueryResultFormat.JSON);
        System.out.println(queryResponse.readEntity(String.class));
        test.close();
    }

}
