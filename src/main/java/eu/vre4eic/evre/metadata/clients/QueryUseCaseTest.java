/* 
 * Copyright 2017 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.vre4eic.evre.metadata.clients;

import eu.vre4eic.evre.metadata.clients.usecases.*;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.LoggerFactory;

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
    public Response executeSparqlQueryVirtuoso(String queryStr, String namespace, String format, String token) throws UnsupportedEncodingException {//QueryResultFormat format) throws UnsupportedEncodingException {
        WebTarget webTarget = client.target(baseURI + "/query/virtuoso").
                queryParam("format", format).//mimetype
                queryParam("query", URLEncoder.encode(queryStr, "UTF-8").
                        replaceAll("\\+", "%20"));
        Invocation.Builder invocationBuilder = webTarget.request().
                header("Authorization", token);
        Response response = invocationBuilder.get();
        return response;
    }

    public Response executeSparqlQueryBlazegraph(String queryStr, String namespace, String format, String token) throws UnsupportedEncodingException {//QueryResultFormat format) throws UnsupportedEncodingException {
        WebTarget webTarget = client.target(baseURI + "/query/namespace/" + namespace).
                queryParam("format", format).//mimetype
                queryParam("query", URLEncoder.encode(queryStr, "UTF-8").replaceAll("\\+", "%20"));
        Invocation.Builder invocationBuilder = webTarget.request().
                header("Authorization", token);//.request(mimetype);
        Response response = invocationBuilder.get();
        return response;
    }

    /**
     * Creates a user profile on the server and log in e-VRE with the user
     * credentials
     *
     * @return The token of the logged-in user
     */
    public String createUserAndLogin(String nSBaseURI) throws UnsupportedEncodingException {

        String token = "";

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
        Response prResponse = nSwebTarget.request(MediaType.APPLICATION_JSON).post(Entity.form(myForm));

        System.out.println("... done, result message is: " + prResponse.readEntity(String.class));
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
            System.out.println("... done, the the result message is: " + resJO.toJSONString());
            if (resJO.get("status").equals(eu.vre4eic.evre.core.Common.ResponseStatus.SUCCEED.toString())) {
                token = (String) resJO.get("token");
            }
            System.out.println("the vaild token is: " + resJO.get("token"));
            System.out.println();
            token = (String) resJO.get("token");
        } catch (ParseException e) {

            e.printStackTrace();
        }

        return (token);
    }

    /**
     * Removes a user profile on the e-VRE server
     *
     * @return The output of the query
     */
    public void removeUser(String nSBaseURI, String token, String id) throws UnsupportedEncodingException {

        System.out.println();
        System.out.println("4) Remove the User Profile... ");
        WebTarget webTarget = client.target(nSBaseURI + "/user/removeprofile").
                queryParam("token", token).//mimetype
                queryParam("id", id);
        Invocation.Builder invocationBuilder = webTarget.request();
        Response response = invocationBuilder.get();

        System.out.println("removed, result message is: " + response.readEntity(String.class));

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
        Set<String> loggers = new HashSet<>(Arrays.asList(
                "org.openrdf.rio",
                "org.apache.http",
                "groovyx.net.http",
                "org.eclipse.jetty.client",
                "org.eclipse.jetty.io",
                "org.eclipse.jetty.http",
                "o.e.jetty.util",
                "o.e.j.u.component",
                "org.openrdf.query.resultio"));
        for (String log : loggers) {
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(log);
            logger.setLevel(ch.qos.logback.classic.Level.INFO);
            logger.setAdditive(false);
        }
        String nSBaseURI = "http://v4e-lab.isti.cnr.it:8080/NodeService";
        String baseURI = "http://v4e-lab.isti.cnr.it:8080/MetadataService";
        baseURI = "http://139.91.183.48:8181/EVREMetadataServices";
        QueryUseCaseTest test = new QueryUseCaseTest(baseURI, nSBaseURI);
        String query = "select distinct (?persName as ?name) ?Service (?pers as ?uri) ?org_0Name from <http://ekt-data> from <http://rcuk-data> from <http://fris-data> from <http://epos-data> from <http://envri-data>  where {\n"
                + "?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "?pers  <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?pers rdfs:label ?persName. \n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?pers <http://eurocris.org/ontology/cerif#Person-OrganisationUnit/is%20member%20of> ?org_0.\n"
                + "?org_0 <http://eurocris.org/ontology/cerif#has_name> ?org_0Name. \n"
                //                + "?persName bds:search \"maria\". \n"
                //                + "?org_0Name bds:search \"european\".\n"
                + "?persName bif:contains \"maria\". \n"
                + "?org_0Name bif:contains  \"european\".\n"
                + "} ";

        //String queryEnc = URLEncoder.encode(query2, "UTF-8").replaceAll("\\+", "%20");
        // System.out.println(queryEnc);
        //1- Create a user profile with userid="id_of_user" and 2) login into e-VRE with the user credentials
        String token = test.createUserAndLogin(nSBaseURI);

        //3- Execute a query
        System.out.println();
        System.out.println("3) Executing the query: " + query);
        String namespace = "vre4eic";
        long start = System.currentTimeMillis();
        Response queryResponse = test.executeSparqlQueryVirtuoso(query, namespace, "application/json", token);
//        Response queryResponse = test.executeSparqlQueryBlazegraph(query, namespace, "application/json", token);
        System.out.println("Query executed, return message is: " + queryResponse.readEntity(String.class));
        System.out.println("Duration: " + (System.currentTimeMillis() - start));
        //4- Remove the profile from e-VRE
        test.removeUser(nSBaseURI, token, "id_of_user");

        test.close();
    }

}
