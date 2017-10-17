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
package eu.vre4eic.evre.metadata.clients.usecases;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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

    public Response executeSparqlQueryGET(String queryStr, String namespace, String format, String token) throws UnsupportedEncodingException {//QueryResultFormat format) throws UnsupportedEncodingException {
        //String mimetype = Utilities.fetchQueryResultMimeType(format);
        WebTarget webTarget = client.target(baseURI + "/query/count/namespace/" + namespace).
                queryParam("format", format).//mimetype
                queryParam("query", URLEncoder.encode(queryStr, "UTF-8").
                        replaceAll("\\+", "%20"));
        // System.out.println("----------> " + webTarget.getUri());
        Invocation.Builder invocationBuilder = webTarget.request().
                header("Authorization", token);//.request(mimetype);
        Response response = invocationBuilder.get();
        return response;
    }

    public Response executeSparqlQueryPOST(String queryStr, String namespace, String format, String token) throws UnsupportedEncodingException {//QueryResultFormat format) throws UnsupportedEncodingException {
        //String mimetype = Utilities.fetchQueryResultMimeType(format);
        WebTarget webTarget = client.target(baseURI).path("/query/count/namespace/" + namespace);
        JSONObject json = new JSONObject();
        json.put("query", queryStr);
        json.put("format", "application/json");
        return webTarget.request(MediaType.APPLICATION_JSON).header("Authorization", token).post(Entity.json(json.toJSONString()));
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
        baseURI = "http://139.91.183.48:8181/EVREMetadataServices";
//        baseURI = "http://83.212.97.61:8080/EVREMetadataServices-1.0-SNAPSHOT";
        baseURI = "http://139.91.183.70:8080/EVREMetadataServices-1.0-SNAPSHOT"; //seistro 2
        NSUseCaseTest ns = new NSUseCaseTest(nSBaseURI);
        QueryUseCaseTest test = new QueryUseCaseTest(baseURI);
        String query = "select * from <http://fris-data> {?s ?p ?o} ";

        query = "PREFIX cerif: <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?persName ?Service (?pers as ?uri) \n"
                + "from <http://ekt-data> \n"
                + "from <http://rcuk-data> \n"
                + "from <http://fris-data> \n"
                + "from <http://epos-data> \n"
                + "from <http://envri-data>\n"
                + "where {\n"
                + "?pers cerif:is_source_of ?FLES.  \n"
                + "?FLES cerif:has_destination ?Ser.  \n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?pers a cerif:Person.  \n"
                + "?pers rdfs:label ?persName. \n"
                + "?persName bds:search ' maria'.  \n"
                + "?persName bds:matchAllTerms \"true\".  \n"
                + "?persName bds:relevance ?score. \n"
                + "}  ORDER BY desc(?score) ?pers limit 100";

        //        query = "select distinct ?g where {{graph ?g {?s ?p ?o}}}";
        //        query = "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
        //                + "select (concat(str(?pub), '#@#', str(?pubTitle)) as ?publication_title) ?pubDate (concat(str(?pers), '#@#', str(?persName)) as ?person_name)\n"
        //                + "from <http://ekt-data>\n"
        //                + "from <http://rcuk-data>\n"
        //                + "from <http://fris-data>\n"
        //                + "from <http://epos-data>\n"
        //                + "from <http://envri-data>\n"
        //                + "where {\n"
        //                + "?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
        //                + "?pub <http://eurocris.org/ontology/cerif#has_title> ?pubTitle.\n"
        //                + "?pub <http://eurocris.org/ontology/cerif#has_publicationDate> ?pubDate.\n"
        //                + "?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp.\n"
        //                + "?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n"
        //                + "?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
        //                + "?pers rdfs:label ?persName.\n"
        //                + "?persName bds:search ' Robert-C.'.\n"
        //                + "?persName bds:relevance ?score .\n"
        //                + "} ORDER BY desc(?score)";
        //        query = "SELECT * WHERE {{ ?s ?p ?o . ?s rdfs:label ?o. ?o bds:search 'Quadrelli' . }}";
        //String queryEnc = URLEncoder.encode(query2, "UTF-8").replaceAll("\\+", "%20");
        // System.out.println(queryEnc);
        //1- Create a user profile with userid="id_of_user" and 2) login into e-VRE with the user credentials
        String token = ns.createUserAndLogin();

        //3- Execute a query
        System.out.println();
        System.out.println("3) Executing the query: " + query);
        String namespace = "vre4eic";
//        Response queryResponse = test.executeSparqlQueryGET(query, namespace, "application/json", token);//QueryResultFormat.JSON);
        Response queryResponse = test.executeSparqlQueryPOST(query, namespace, "application/json", token);//QueryResultFormat.JSON);

        System.out.println("Query executed, return message is: " + queryResponse.readEntity(String.class));

        //4- Remove the profile from e-VRE
        ns.removeUser(token, "id_of_user");
        test.close();
        ns.close();
    }

}
