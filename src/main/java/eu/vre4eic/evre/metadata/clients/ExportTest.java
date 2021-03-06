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

import eu.vre4eic.evre.blazegraph.Utils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;

/**
 * Jersey REST client generated for REST resource:ExportServices [export]<br>
 * USAGE:
 * <pre>
 *        ExportTest client = new ExportTest();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author rousakis
 */
public class ExportTest {

    private WebTarget webTarget;
    private Client client;
    private String baseURI;

    public ExportTest(String baseURI) {
        this.baseURI = baseURI;
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(baseURI).path("export");
    }

    public Response exportFilePOSTJSON(String requestEntity, String token) throws ClientErrorException {
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", token).post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON), Response.class);
    }

    public Response exportFileGETJSON(String graph, String format, String token) throws ClientErrorException, UnsupportedEncodingException {
        webTarget = webTarget.queryParam("format", format).//mimetype
                queryParam("graph", graph);
        Invocation.Builder invocationBuilder = webTarget.request().
                header("Authorization", token);//.request(mimetype);
        Response response = invocationBuilder.get();
        return response;
    }

    public Response exportFileVirtuoso(String graph, String format, String token) throws ClientErrorException, UnsupportedEncodingException {
        webTarget = webTarget.path("virtuoso");
        webTarget = webTarget.queryParam("format", format).//mimetype
                queryParam("graph", graph);
        Invocation.Builder invocationBuilder = webTarget.request().
                header("Authorization", token);//.request(mimetype);
        Response response = invocationBuilder.get();
        return response;
    }

    public void close() {
        client.close();
    }

    public static void main(String[] args) throws ClientErrorException, UnsupportedEncodingException {
        String baseURI = "http://139.91.183.48:8181/EVREMetadataServices";
//        baseURI = "http://v4e-lab.isti.cnr.it:8080/MetadataService";
        ExportTest exp = new ExportTest(baseURI);
        JSONObject request = new JSONObject();
        ///
//        request.put("format", "application/rdf+xml");
//        request.put("graph", " http://epos-data");
//        String token = "e5cfffef-4218-4993-8238-e97aa09d92f8";
//        Response resp = exp.exportFilePOSTJSON(request.toString(), token);

//        Response resp = exp.exportFileGETJSON("http://cidoc_2", "application/rdf+xml", token);
//        Response resp = exp.exportFileVirtuoso("http://cidoc_2", "application/rdf+xml", "test");
//        System.out.println(resp.readEntity(String.class));
//        System.out.println(resp.getStatus());
        Utils.saveResponseToFile("efo248.nt", exp.exportFileVirtuoso("http://efo2.48", "text/plain", "test"));

    }

}
