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

import gr.forth.ics.virtuoso.Utils;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.LoggerFactory;

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
public class ImportTest {

    private WebTarget webTarget;
    private Client client;
    private String baseURI;

    public ImportTest(String baseURI) {
        this.baseURI = baseURI;
        client = ClientBuilder.newClient();
        webTarget = client.target(baseURI).path("import");
    }

    public Response importFilePath(String requestEntity, String token) throws ClientErrorException {
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
    public Response importFileDataBlazegraph(String content, String format,
            String namespace, String namedGraph, String token)
            throws ClientProtocolException, IOException {
        String restURL;
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

    public Response importFileDataVirtuoso(String content, String format,
            String namedGraph, String token)
            throws ClientProtocolException, IOException {
        String restURL;
        Client client = ClientBuilder.newClient();
        String mimeType = format;
        // Taking into account nameGraph in the construction of the URL
        restURL = baseURI + "/import/virtuoso";
        WebTarget webTarget = client.target(restURL);
        if (namedGraph != null) {
            restURL = baseURI + "?graph=" + namedGraph;
            webTarget = webTarget.queryParam("graph", namedGraph);
        }
        Response response = webTarget.request().header("Authorization", token).post(Entity.entity(content, mimeType));
        return response;
    }

    public static void main(String[] args) throws IOException {
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
        String baseURI = "http://139.91.183.48:8181/EVREMetadataServices";
//        baseURI = "http://139.91.183.70:8080/EVREMetadataServices-1.0-SNAPSHOT";
//        baseURI = "http://v4e-lab.isti.cnr.it:8080/MetadataService";
        String folder = "E:/RdfData";
//        folder = "/home/rousakis/RdfData";   //seistro 2 path
        ImportTest imp = new ImportTest(baseURI);
        ////////
        String token = "rous";
        //this service works only if the file to be imported is in the same machine with the tomcat
//        JSONObject request = new JSONObject();
//        request.put("filename", folder + "/cidoc_v3.2.1.rdfs");
//        request.put("format", "application/rdf+xml");
//        request.put("graph", "http://cidoc_1");
//        Response importResponse = imp.importFilePath(request.toString(), token);
        ///////
        //this service works in all cases 
        Response importResponse = imp.importFileDataVirtuoso(
//                Utils.readFileData(folder + "\\VREData\\EKT RDF\\organizations_with_synthetic_geo_data\\organizationUnits2.ntriples"), // file
                Utils.readFileData(folder + "\\VREData\\EKT RDF\\postalAddresses\\postalAddresses1.n3"), // file
                "text/rdf+n3", // content type
                //                namespace, // namespace
                "http://efo2.48", // nameGraph
                token);
        System.out.println(importResponse.readEntity(String.class));
        imp.close();
    }

}
