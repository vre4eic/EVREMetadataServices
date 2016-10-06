/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.ld.clients;

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
        client = javax.ws.rs.client.ClientBuilder.newClient();
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
    public Response executeSparqlQuery(String queryStr, String namespace, String format) throws UnsupportedEncodingException {//QueryResultFormat format) throws UnsupportedEncodingException {
        //String mimetype = Utilities.fetchQueryResultMimeType(format);
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(baseURI + "/query/namespace/" + namespace).
                queryParam("format", format).//mimetype
                queryParam("query", URLEncoder.encode(queryStr, "UTF-8").
                        replaceAll("\\+", "%20"));
        System.out.println("----------> " + webTarget.getUri());
        Invocation.Builder invocationBuilder = webTarget.request();//.request(mimetype);
        Response response = invocationBuilder.get();
        return response;
    }

    public static void main(String[] args) throws UnsupportedEncodingException, ParseException {
        String baseURI = "http://83.212.97.61:8080/ld-services-1.0-SNAPSHOT";
        baseURI = "http://localhost:8181/ld-services";
        QueryTest test = new QueryTest(baseURI);
        String query = "select * from <http://cidoc/3.2.1> where {?s ?p ?o} limit 5";
        String query2 = "select distinct ?g where { graph ?g {?s ?p ?o} }";

        String queryEnc = URLEncoder.encode(query2, "UTF-8").replaceAll("\\+", "%20");
        String format = "application/json";
        JSONObject json = new JSONObject();
        json.put("query", query);
        json.put("format", format);
//        System.out.println(test.queryExecGETJSON(queryEnc, format));
//        System.out.println(test.queryExecPOSTJSON(json.toJSONString()));

        String namespace = "quads_repo";

        //test query service by Vangelis
        Response queryResponse
                = test.executeSparqlQuery(query, namespace, null);//QueryResultFormat.JSON);
        System.out.println(queryResponse.readEntity(String.class));
        test.close();
    }

}
