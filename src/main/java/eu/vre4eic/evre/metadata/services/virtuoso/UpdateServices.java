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

import java.util.logging.Level;
import java.util.logging.Logger;
import gr.forth.ics.virtuoso.SesameVirtRep;
import eu.vre4eic.evre.core.Common;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.PublisherFactory;
import eu.vre4eic.evre.core.messages.MetadataMessage;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import eu.vre4eic.evre.metadata.utils.PropertiesManager;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.repository.RepositoryException;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("update/virtuoso")
public class UpdateServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private SesameVirtRep virtuoso;
    private AuthModule module;
    private Publisher<MetadataMessage> mdp;

    /**
     * Creates a new instance of UpdateClasses
     */
    public UpdateServices() {
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateExecPOSTJSON(String jsonInput,
            @DefaultValue("") @QueryParam("token") String token) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String authToken = requestContext.getHeader("Authorization");
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(Common.MetadataOperationType.UPDATE);
        if (authToken == null) {
            authToken = token;
        }
        message.setToken(authToken);
        if (jsonObject.size() != 1) {
            message.setMessage("JSON input message should have exactly 1 argument.");
            message.setStatus(ResponseStatus.FAILED);
            return Response.status(400).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            return updateExecVirtuoso(q, authToken, message);
        }
    }

    private Response updateExecVirtuoso(String q, String authToken, MetadataMessageImpl message) throws IOException, UnsupportedEncodingException, ParseException {
        boolean isTokenValid = module.checkToken(authToken);
        int statusInt;
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            statusInt = 401;
        } else {
            boolean result = virtuoso.executeUpdateQuery(q, false);
            if (result) {
                message.setMessage("Update query was applied successfully.");
                statusInt = 200;
                message.setStatus(ResponseStatus.SUCCEED);
            } else {
                statusInt = 500;
                message.setMessage("Error during update query: " + q);
                message.setStatus(ResponseStatus.FAILED);
            }
        }
        mdp.publish(message);
        return Response.status(statusInt).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
    }

}
