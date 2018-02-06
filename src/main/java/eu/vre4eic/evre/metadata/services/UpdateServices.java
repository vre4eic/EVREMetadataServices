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
package eu.vre4eic.evre.metadata.services;

import eu.vre4eic.evre.blazegraph.BlazegraphRepRestful;
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
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("update")
public class UpdateServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    String namespace = propertiesManager.getTripleStoreNamespace();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private BlazegraphRepRestful blazegraphRepRestful;
    private AuthModule module;
    private Publisher<MetadataMessage> mdp;

    /**
     * Creates a new instance of UpdateClasses
     */
    public UpdateServices() {
    }

    @PostConstruct
    public void initialize() {
        blazegraphRepRestful = new BlazegraphRepRestful(propertiesManager.getTripleStoreUrl());
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
            return updateExecBlazegraph(q, this.namespace, authToken, message);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}")
    public Response updateExecPOSTJSONWithNS(String jsonInput,
            @PathParam("namespace") String namespace,
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
            return updateExecBlazegraph(q, namespace, authToken, message);
        }
    }

    private Response updateExecBlazegraph(String q, String namespace, String authToken, MetadataMessageImpl message) throws IOException, UnsupportedEncodingException, ParseException {
        boolean isTokenValid = module.checkToken(authToken);
        int statusInt;
//        isTokenValid = true;
        JSONObject message2 = new JSONObject();
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            statusInt = 401;
        } else {
            Response resp1 = blazegraphRepRestful.executeSparqlUpdateQuery(q, namespace);
            statusInt = resp1.getStatus();
            if (statusInt == 200) {
                message.setStatus(ResponseStatus.SUCCEED);
            } else {
                message.setStatus(ResponseStatus.FAILED);
            }
            String updateMessage = resp1.readEntity(String.class);
            message.setMessage(updateMessage);
            JSONParser parser = new JSONParser();
            message2 = (JSONObject) parser.parse(message.toJSON());
            message2.put("message", updateMessage);
        }
        mdp.publish(message);
        if (statusInt == 200) {
            return Response.status(statusInt).entity(message2.toString()).header("Access-Control-Allow-Origin", "*").build();
        } else {
            return Response.status(statusInt).entity(message.toString()).header("Access-Control-Allow-Origin", "*").build();
        }
    }

}
