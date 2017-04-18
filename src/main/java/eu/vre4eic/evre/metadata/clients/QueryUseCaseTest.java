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
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.vre4eic.evre.core.Common.UserRole;


public class QueryUseCaseTest {

    private WebTarget MSwebTarget;
   
    private javax.ws.rs.client.Client client;
    private String baseURI;

    public QueryUseCaseTest(String baseURI, String nSBaseURI) {
        this.baseURI = baseURI;
        client = ClientBuilder.newClient();
        MSwebTarget = client.target(baseURI).path("query");
      
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

    /**
     * Creates a user profile on the server and log in e-VRE with the user credentials
     *
     * @return The token of the logged-in user
     */
    public String createUserAndLogin(String nSBaseURI) throws UnsupportedEncodingException {
    	 
    	 String token="";
    	 
         
         Form myForm = new Form();
         myForm.param("userid", "id_of_user");
         myForm.param("name", "name_of_the_user");
         myForm.param("email", "email@_of_the_user");
         myForm.param("organization", "organization_the_user_belongs");
         myForm.param("role", eu.vre4eic.evre.core.Common.UserRole.RESEARCHER.toString());
         myForm.param("password", "pwd_of_the_user");
       
         System.out.println("1) Creating a User Profile...");
         WebTarget nSwebTarget = client.target(nSBaseURI + "/user/createprofile");
         
        
         //create a user profile
         
         Response prResponse=nSwebTarget.request(MediaType.APPLICATION_JSON).post(Entity.form(myForm));
         
         System.out.println("... done, result message is: "+prResponse.readEntity(String.class));
         System.out.println();
         
         System.out.println("2) Executing login... ");

         //log in
         WebTarget webTarget = client.target(nSBaseURI + "/user/login").
                 queryParam("username", "id_of_user").//mimetype
                 queryParam("pwd", "pwd_of_the_user");
         Invocation.Builder invocationBuilder = webTarget.request();
         Response response = invocationBuilder.get();

         try {
			JSONObject resJO = (JSONObject) new JSONParser().parse(response.readEntity(String.class));
			System.out.println("... done, the the result message is: "+resJO.toJSONString());
			if (resJO.get("status").equals(eu.vre4eic.evre.core.Common.ResponseStatus.SUCCEED.toString()))
				token=(String) resJO.get("token");
			System.out.println("the vaild token is: "+resJO.get("token"));
			System.out.println();
			token=(String) resJO.get("token");
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
         
    	return (token);
    }
    
    /**
     * Removes  a user profile on the e-VRE server
     *
     * @return The output of the query
     */
    
    public void removeUser(String nSBaseURI, String token, String id) throws UnsupportedEncodingException {
    	
    	System.out.println();
        System.out.println("4) Remove the User Profile... ");
    	 WebTarget webTarget = client.target(nSBaseURI + "/user/removeprofile").
                 queryParam("token", token).//mimetype
                 queryParam("id",id);
         Invocation.Builder invocationBuilder = webTarget.request();
         Response response = invocationBuilder.get();
       
         System.out.println("removed, result message is: "+response.readEntity(String.class));
    	
    	
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
        QueryUseCaseTest test = new QueryUseCaseTest(baseURI, nSBaseURI);
        String query = "select * where {?s ?p ?o} limit 10";
        String query2 = "SELECT * WHERE {{ ?s ?p ?o . ?s rdfs:label ?o. ?o bds:search 'Quadrelli' . }}";

        //String queryEnc = URLEncoder.encode(query2, "UTF-8").replaceAll("\\+", "%20");
       // System.out.println(queryEnc);

        //1- Create a user profile with userid="id_of_user" and 2) login into e-VRE with the user credentials
        String token = test.createUserAndLogin(nSBaseURI);
        
        //3- Execute a query
        System.out.println();
        System.out.println("3) Executing the query: "+query2);
        String namespace = "ekt-demo";
        Response queryResponse
                = test.executeSparqlQuery(query2, namespace, "text/tab-separated-values", token);//QueryResultFormat.JSON);
        System.out.println("Query executed, return message is: "+queryResponse.readEntity(String.class));
        
        //4- Remove the profile from e-VRE
        
        test.removeUser(nSBaseURI, token, "id_of_user");
        
        test.close();
    }

}
