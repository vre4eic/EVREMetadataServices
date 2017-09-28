/* 
 * Copyright 2017 rousakis.
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
import org.openrdf.rio.RDFFormat;

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

    public Response exportFileGETJSON(String graph, String format, String token) throws ClientErrorException, UnsupportedEncodingException {
        WebTarget MSwebTarget = client.target(baseURI).path("export");
        MSwebTarget = MSwebTarget.queryParam("format", format).//mimetype
                queryParam("graph", graph);
        Invocation.Builder invocationBuilder = MSwebTarget.request().
                header("Authorization", token);//.request(mimetype);
        Response response = invocationBuilder.get();
        return response;
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
        String graph = "http://cidoc_2";
        System.out.println();
        System.out.println("3) Exporting data from graph: " + graph);
        JSONObject request = new JSONObject();
        ///
        String format = Utils.fetchDataImportMimeType(RDFFormat.TURTLE);
        request.put("format", format);
        request.put("graph", graph);
        Response resp = test.exportFileGETJSON(graph, format, token);
        System.out.println("Export executed, return message is: " + resp.readEntity(String.class));

        //4- Remove the profile from e-VRE
        ns.removeUser(token, "id_of_user");
        test.close();
        ns.close();
    }

}
