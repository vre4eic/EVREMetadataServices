/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.services;

import eu.vre4eic.evre.blazegraph.BlazegraphRepRestful;
import eu.vre4eic.evre.core.Common.MetadataOperationType;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import eu.vre4eic.evre.metadata.utils.Authorization;
import eu.vre4eic.evre.metadata.utils.PropertiesManager;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
@Path("export")
public class ExportServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    String namespace = propertiesManager.getTripleStoreNamespace();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private BlazegraphRepRestful blazegraphRepRestful;
    private AuthModule module;

    /**
     * Creates a new instance of ExportServices
     */
    public ExportServices() {
    }

    @PostConstruct
    public void initialize() {
        blazegraphRepRestful = new BlazegraphRepRestful(propertiesManager.getTripleStoreUrl());
        module = AuthModule.getInstance("tcp://v4e-lab.isti.cnr.it:61616");
    }

    @GET
    public Response exportFileGETJSON(@QueryParam("g") String g,
            @QueryParam("f") String f) throws ParseException, IOException {
        String format = f;
        String graph = g;
        int status;
        String token = requestContext.getHeader("Authorization");
        Response resp = Authorization.checkAuthorization(module, token);
        if (resp != null) {
            return resp;
        }
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.READ);
        message.setToken(token);
        if (format == null) {
            message.setStatus(ResponseStatus.FAILED);
            status = 400;
            message.setMessage("Error in the provided format.");
        } else {
            status = 200;
            message.setStatus(ResponseStatus.SUCCEED);
            message.setMessage(blazegraphRepRestful.exportFile(format, namespace, graph).readEntity(String.class));
        }
        return Response.status(status).entity(message.toJSON().toString()).header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exportFilePOSTJSON(String jsonInput) throws ParseException, IOException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        int status;
        String token = requestContext.getHeader("Authorization");
        Response resp = Authorization.checkAuthorization(module, token);
        if (resp != null) {
            return resp;
        }
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.READ);
        message.setToken(token);
        if (jsonObject.size() != 2) {
            message.setMessage("JSON input message should have exactly 2 arguments.");
            message.setStatus(ResponseStatus.FAILED);
            status = 400;
        } else {
            String format = (String) jsonObject.get("format");
            String graph = (String) jsonObject.get("graph");
            if (format == null) {
                message.setStatus(ResponseStatus.FAILED);
                status = 400;
                message.setMessage("Error in the provided format.");
            } else {
                status = 200;
                message.setStatus(ResponseStatus.SUCCEED);
                message.setMessage(blazegraphRepRestful.exportFile(format, namespace, graph).readEntity(String.class));
            }
        }
        return Response.status(status).entity(message.toJSON().toString()).header("Access-Control-Allow-Origin", "*").build();
    }
}
