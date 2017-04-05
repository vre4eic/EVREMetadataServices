/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.services;

import eu.vre4eic.evre.blazegraph.BlazegraphRepRestful;
import eu.vre4eic.evre.core.Common.MetadataOperationType;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.PublisherFactory;
import eu.vre4eic.evre.core.messages.MetadataMessage;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import eu.vre4eic.evre.metadata.utils.PropertiesManager;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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
    private Publisher<MetadataMessage> mdp;

    /**
     * Creates a new instance of ExportServices
     */
    public ExportServices() {
    }

    @PostConstruct
    public void initialize() {
        blazegraphRepRestful = new BlazegraphRepRestful(propertiesManager.getTripleStoreUrl());
        module = AuthModule.getInstance("tcp://v4e-lab.isti.cnr.it:61616");
        mdp = PublisherFactory.getMetatdaPublisher();
    }

    @GET
    public Response exportFileGETJSON(@QueryParam("g") String g,
            @QueryParam("f") String f,
            @DefaultValue("") @QueryParam("token") String token) throws ParseException, IOException {
        String format = f;
        String graph = g;
        int status;
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        boolean isTokenValid = module.checkToken(authToken);
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.READ);
        message.setToken(authToken);
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            status = 401;
        } else if (format == null) {
            message.setStatus(ResponseStatus.FAILED);
            status = 400;
            message.setMessage("Error in the provided format.");
        } else {
            status = 200;
            message.setStatus(ResponseStatus.SUCCEED);
            message.setMessage(blazegraphRepRestful.exportFile(format, namespace, graph).readEntity(String.class));
        }
        mdp.publish(message);
        return Response.status(status).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exportFilePOSTJSON(String jsonInput,
            @DefaultValue("") @QueryParam("token") String token) throws ParseException, IOException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        int status;
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        boolean isTokenValid = module.checkToken(authToken);
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.READ);
        message.setToken(authToken);
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            status = 401;
        } else if (jsonObject.size() != 2) {
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
        mdp.publish(message);
        return Response.status(status).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
    }
}
