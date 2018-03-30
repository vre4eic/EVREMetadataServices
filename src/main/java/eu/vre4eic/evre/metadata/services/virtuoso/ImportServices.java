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
package eu.vre4eic.evre.metadata.services.virtuoso;

import eu.vre4eic.evre.core.Common.MetadataOperationType;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.PublisherFactory;
import eu.vre4eic.evre.core.messages.MetadataMessage;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import eu.vre4eic.evre.metadata.utils.PropertiesManager;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import gr.forth.ics.virtuoso.SesameVirtRep;
import gr.forth.ics.virtuoso.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONObject;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("import/virtuoso")
public class ImportServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private SesameVirtRep virtuoso;
    private AuthModule module;
    private Publisher<MetadataMessage> mdp;

    /**
     * Creates a new instance of ImportServices
     */
    public ImportServices() {
    }

    @PostConstruct
    public void initialize() {
        Properties prop = propertiesManager.getProperties();
        try {
            virtuoso = new SesameVirtRep(
                    prop.getProperty("virtuoso.url"),
                    Integer.parseInt(prop.getProperty("virtuoso.port")),
                    prop.getProperty("virtuoso.username"),
                    prop.getProperty("virtuoso.password"));
        } catch (RepositoryException ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
        }
        module = AuthModule.getInstance("tcp://v4e-lab.isti.cnr.it:61616");
        mdp = PublisherFactory.getMetatdaPublisher();
    }

    /**
     * <b>POST</b> service which imports an RDF data String with a given format
     * in a specific named graph. The named graph will be created in a default
     * namespace which is defined in a configuration file. <br>
     * <b>URL:</b>
     * /ld-services/import?graph={graph}
     *
     * @param incomingData The String object which contains the RDF data to be
     * imported via post.
     * @param graph The named graph URI in which the data will be inserted.
     * @param contentType The mimetype of the data contained in the data String.
     * Supported formats are:
     * <ul>
     * <li><b>application/rdf+xml</b>: rdf, rdfs, owl, xml data</li>
     * <li><b>text/plain</b>: nt triples</li>
     * <li><b>application/x-turtle</b>: ttl triples </li>
     * <li>etc.</li>
     * </ul>
     * The complete list with the accepted mimetypes can be found in
     * <a href="https://wiki.blazegraph.com/wiki/index.php/REST_API#MIME_Types">https://wiki.blazegraph.com/wiki/index.php/REST_API#MIME_Types</a>.
     * @return A response from the service which denotes whether the data were
     * imported or not.
     * @throws ClientProtocolException
     * @throws IOException
     */
    @POST
    public Response importFileContentsPOSTJSON(InputStream incomingData,
            @QueryParam("graph") String graph,
            @HeaderParam("content-type") String contentType,
            @DefaultValue("") @QueryParam("token") String token) throws ClientProtocolException, IOException {
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        boolean isTokenValid = module.checkToken(authToken);
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.INSERT);
        message.setToken(authToken);
        int status = 0;
//        isTokenValid = true;
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            status = 401;
        } else {
            try {
                RDFFormat format = Utils.RDFFormatfromString(contentType);
                virtuoso.importInputStream(incomingData, format, graph);
                message.setStatus(ResponseStatus.SUCCEED);
                status = 200;
                message.setMessage("Data were inserted successfully.");
            } catch (Exception ex) {
                message.setMessage(ex.getMessage());
                message.setStatus(ResponseStatus.FAILED);
                status = 500;
            }
        }
        mdp.publish(message);
        JSONObject result = new JSONObject();
        result.put("response_status", message.getStatus());
        result.put("message", message.getMessage());
        mdp.publish(message);
        return Response.status(status).entity(result.toString()).header("Access-Control-Allow-Origin", "*").build();
    }

}
