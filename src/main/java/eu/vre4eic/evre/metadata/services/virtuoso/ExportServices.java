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

import eu.vre4eic.evre.blazegraph.BlazegraphRepRestful;
import eu.vre4eic.evre.core.Common.MetadataOperationType;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.PublisherFactory;
import eu.vre4eic.evre.core.messages.MetadataMessage;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import eu.vre4eic.evre.metadata.utils.MetadataNM;
import eu.vre4eic.evre.metadata.utils.PropertiesManager;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import gr.forth.ics.virtuoso.SesameVirtRep;
import gr.forth.ics.virtuoso.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.parser.ParseException;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("export/virtuoso")
public class ExportServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    String namespace = propertiesManager.getTripleStoreNamespace();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private SesameVirtRep virtuoso;
    private AuthModule module;
    private Publisher<MetadataMessage> mdp;

    /**
     * Creates a new instance of ExportServices
     */
    public ExportServices() {
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
        module = MetadataNM.getModule();
        mdp = PublisherFactory.getMetatdaPublisher();
    }

    @GET
    public Response exportFileGETJSON(@QueryParam("graph") String graph,
            @QueryParam("format") String contentType,
            @DefaultValue("") @QueryParam("token") String token) throws ParseException, IOException {
        String namespace = this.namespace;
        return execExportGET(token, contentType, graph, namespace);
    }

    public Response execExportGET(String token, String contentType, String graph, String namespace) throws UnsupportedEncodingException {
        int status;
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        boolean isTokenValid = module.checkToken(authToken);
//        isTokenValid = true;
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.READ);
        message.setToken(authToken);
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            status = 401;
        } else if (contentType == null) {
            message.setStatus(ResponseStatus.FAILED);
            status = 400;
            message.setMessage("Error in the provided format.");
        } else {
            RDFFormat format = Utils.RDFFormatfromString(contentType);
            OutputStream output = new OutputStream() {
                private StringBuilder string = new StringBuilder();

                @Override
                public void write(int b) throws IOException {
                    this.string.append((char) b);
                }

                public String toString() {
                    return this.string.toString();
                }
            };
            try {
                RDFWriter writer = Rio.createWriter(format, output);
                virtuoso.getCon().export(writer, new URIImpl(graph));
                message.setStatus(ResponseStatus.SUCCEED);
                message.setMessage("Data were exported successfully. ");
                mdp.publish(message);
                return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin", "*").build();
            } catch (RepositoryException | RDFHandlerException ex) {
                message.setStatus(ResponseStatus.FAILED);
                message.setMessage(ex.getMessage());
                status = 500;
            }
        }
        mdp.publish(message);
        virtuoso.terminate();
        return Response.status(status).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
    }
}
