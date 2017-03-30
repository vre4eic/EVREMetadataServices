/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.clients;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author rousakis
 */
public class RestClient {
    
    private String serviceUrl;
    private String namespace;
    
    public RestClient(String serviceUrl, String namespace) throws IOException {
        this.serviceUrl = serviceUrl;
        this.namespace = namespace;
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
    public Response importFile(String content, String format, String namespace, String namedGraph)
            throws ClientProtocolException, IOException {
        String restURL = serviceUrl + "/import/namespace/" + namespace;

        // Taking into account nameSpace in the construction of the URL
        if (namespace != null) {
            restURL = serviceUrl + "/import/namespace/" + namespace;
        } else {
            restURL = serviceUrl + "/import";
        }
        // Taking into account nameGraph in the construction of the URL
        if (namedGraph != null) {
            restURL = restURL + "?namegraph=" + namedGraph;
        }
        System.out.println("restURL: " + restURL);
        
        String mimeType = format;
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(restURL).queryParam("namegraph", namedGraph);
        
        Response response = webTarget.request().post(Entity.entity(content, mimeType));
        return response;
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
        //WebTarget webTarget = client.target(serviceUrl + "/namespace/" + namespace + "/" + stringToQueryResultFormat(format))//mimetype)
        //        .queryParam("query", URLEncoder.encode(queryStr, "UTF-8").replaceAll("\\+", "%20"));
        WebTarget webTarget = client.target(serviceUrl + "/query/namespace/" + namespace)
                .queryParam("format", format)
                .queryParam("query", URLEncoder.encode(queryStr, "UTF-8").replaceAll("\\+", "%20"));
        Invocation.Builder invocationBuilder = webTarget.request();
        Response response = invocationBuilder.get();
        return response;
    }
    
    public static void main(String[] args) throws IOException {
        String baseURI = "http://83.212.97.61:8080/ld-services-1.0-SNAPSHOT";
        String namespace = "quads_repo";
        RestClient restClient = new RestClient(baseURI, namespace);
        String query = "select * from <http://cidoc/3.2.1> where {?s ?p ?o} limit 5";
        Response serviceResponce = restClient.executeSparqlQuery(query, namespace, "application/json");
        System.out.println(serviceResponce.readEntity(String.class));
    }
}
