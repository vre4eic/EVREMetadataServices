/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.services;

import eu.vre4eic.evre.blazegraph.BlazegraphRepRestful;
import eu.vre4eic.evre.core.Common;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import eu.vre4eic.evre.metadata.utils.Authorization;
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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

    /**
     * Creates a new instance of UpdateClasses
     */
    public UpdateServices() {
    }

    @PostConstruct
    public void initialize() {
        blazegraphRepRestful = new BlazegraphRepRestful(propertiesManager.getTripleStoreUrl());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response updateExecPOSTJSON(String jsonInput) throws IOException, ParseException {
        String token = requestContext.getHeader("Authorization");
        Response resp = Authorization.checkAuthorization(module, token);
        if (resp != null) {
            return resp;
        }
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(Common.MetadataOperationType.UPDATE);
        message.setToken(token);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        int status;
        if (jsonObject.size() != 1) {
            message.setMessage("JSON input message should have exactly 1 arguments.");
            message.setStatus(ResponseStatus.FAILED);
            status = 400;
        } else {
            String q = (String) jsonObject.get("query");
            String tripleStoreNamespace = propertiesManager.getTripleStoreNamespace();
            Response resp1 = blazegraphRepRestful.executeUpdateSparqlQuery(q, tripleStoreNamespace);
            status = resp1.getStatus();
            if (status == 200) {
                message.setStatus(ResponseStatus.SUCCEED);
            } else {
                message.setStatus(ResponseStatus.FAILED);
            }
            message.setMessage(resp1.readEntity(String.class));
        }
        return Response.status(status).entity(message.toJSON().toString()).header("Access-Control-Allow-Origin", "*").build();
    }
}
