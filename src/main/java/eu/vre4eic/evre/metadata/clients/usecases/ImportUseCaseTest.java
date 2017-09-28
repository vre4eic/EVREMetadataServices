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

import eu.vre4eic.evre.blazegraph.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
public class ImportUseCaseTest {

    private WebTarget webTarget;
    private Client client;
    private String baseURI;

    public ImportUseCaseTest(String baseURI) {
        this.baseURI = baseURI;
        client = ClientBuilder.newClient();
        webTarget = client.target(baseURI).path("import");
    }

    public Response importFilePOSTJSON(String requestEntity, String token) throws ClientErrorException {
        return webTarget.request(MediaType.APPLICATION_JSON).header("Authorization", token).post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON), Response.class);
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
    public Response importFile(String content, String format, String namespace, String namedGraph, String token)
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
        Response response = webTarget.request().header("Authorization", token).post(Entity.entity(content, mimeType));
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
            System.out.println("Exception while reading import data occured .");
            return null;
        }
        return sb.toString();
    }

    public static String readFileFromResources(String resourceFile) {
        InputStream is = ImportUseCaseTest.class.getResourceAsStream(resourceFile);
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Exception while reading import data occured .");
            return null;
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException, ParseException {
        String nSBaseURI = "http://v4e-lab.isti.cnr.it:8080/NodeService";
        String baseURI = "http://v4e-lab.isti.cnr.it:8080/MetadataService";
        baseURI = "http://139.91.183.48:8181/EVREMetadataServices";
        NSUseCaseTest ns = new NSUseCaseTest(nSBaseURI);
        ImportUseCaseTest test = new ImportUseCaseTest(baseURI);
        ////////
        //1- Create a user profile with userid="id_of_user" and 2) login into e-VRE with the user credentials
        String token = ns.createUserAndLogin();

        //3- Import the RDF data
        System.out.println("3) Importing RDF data into the triple store.");

        //this service works only if the file to be imported is in the same machine with the tomcat
//        JSONObject request = new JSONObject();
//        request.put("filename", folder + "/cidoc_v3.2.1.rdfs");
//        request.put("format", "application/rdf+xml");
//        request.put("graph", "http://cidoc_1");
//        Response importResponse = imp.importFilePOSTJSON(request.toString(), token);
        ///////
        //this service works in all cases 
        String namespace = "ekt-data";
        Response importResponse = test.importFile(readFileData("C:\\RdfData\\res-cidoc_v3.2.1.rdfs"), //small dataset
                Utils.fetchDataImportMimeType(RDFFormat.RDFXML), // content type
                namespace, // namespace
                "http://cidoc_2", // namedGraph
                token);
//        Response importResponse = test.importFile(readFileFromResources("/data/LifeWatchDatabase.ttl"), //medium dataset
//                Utils.fetchDataImportMimeType(RDFFormat.TURTLE), // content type
//                namespace, // namespace
//                "http://lifewatch", // namedGraph
//                token);

        String responseString = importResponse.readEntity(String.class);
        System.out.println(responseString);
//        JSONParser parser = new JSONParser();
//        JSONObject jsonObj = (JSONObject) parser.parse(responseString);
//        System.out.println(jsonObj.get(token));
//        String messageString = (String) jsonObj.get("message");
//        JSONObject messageObj = (JSONObject) parser.parse(messageString);
//        jsonObj.put("message", messageObj);
//        System.out.println(jsonObj.toJSONString());
//        test.close();

        //4- Remove the profile from e-VRE
        ns.removeUser(token, "id_of_user");
        test.close();
        ns.close();

    }

}
