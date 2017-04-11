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
import io.swagger.annotations.Api;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
@Path("query")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/test", description = "test")
public class QueryServices {

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
     * Creates a new instance of QueryServices
     */
    public QueryServices() {
    }

    @PostConstruct
    public void initialize() {
        blazegraphRepRestful = new BlazegraphRepRestful(propertiesManager.getTripleStoreUrl());
        module = AuthModule.getInstance("tcp://v4e-lab.isti.cnr.it:61616");
        mdp = PublisherFactory.getMetatdaPublisher();
    }

    /**
     * <b>GET</b> service which executes a SPARQL query and returns the results
     * in various formats. The query is applied on a default namespace defined
     * in the configuration file. <br>
     * <b>URL:</b> /ld-services/query?query={query1}&format={format1}
     *
     * @param query Query parameter which has a string value representing the
     * SPARQL query which will be applied.
     * @param format Query parameter which refers on the requested
     * mimetype-format of the results. The formats which are supported are:
     * <b>text/csv</b>,
     * <b>application/json</b>, <b>text/tab-separated-values</b>,
     * <b>application/sparql-results+xml.</b>
     * @return A Response instance which has an entity content with the query
     * results and the required format. <br>
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryExecGETJSON(
            @DefaultValue("application/json") @QueryParam("format") String f,
            @QueryParam("query") String q,
            @DefaultValue("") @QueryParam("token") String token) throws IOException {
        String authToken = requestContext.getHeader("Authorization");
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.QUERY);
        if (authToken == null) {
            authToken = token;
        }
        message.setToken(authToken);
        return queryExecBlazegraph(q, namespace, f, authToken, message);
    }

    /**
     * <b>GET</b> service which executes a SPARQL query and returns the results
     * in various formats. The query is applied on a namespace provided as a
     * path parameter. <br>
     * <b>URL:</b>
     * /ld-services/query/namespace/{namespace}?query={query1}&format={format1}
     *
     * @param namespace Path parameter which denotes the namespace in which the
     * query will be applied.
     * @param format Query parameter which refers on the requested
     * mimetype-format of the results. The formats which are supported are:
     * <b>text/csv</b>,
     * <b>application/json</b>, <b>text/tab-separated-values</b>,
     * <b>application/sparql-results+xml.</b>
     * @param query Query parameter which has a string value representing the
     * SPARQL query which will be applied.
     * @return A Response instance which has an entity content with the query
     * results and the required format. <br>
     */
    @GET
    @Path("/namespace/{namespace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryExecGETJSONWithNS(
            @PathParam("namespace") String namespace,
            @DefaultValue("application/json") @QueryParam("format") String format,
            @QueryParam("query") String query,
            @DefaultValue("") @QueryParam("token") String token) throws UnsupportedEncodingException, IOException {
        String authToken = requestContext.getHeader("Authorization");
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.QUERY);
        if (authToken == null) {
            authToken = token;
        }
        message.setToken(authToken);
        return queryExecBlazegraph(format, query, namespace, authToken, message);
    }

    /**
     * <b>POST</b> service which executes a SPARQL query and returns the results
     * in various formats. The query is applied on a default namespace defined
     * in the configuration file. <br>
     * <b>URL:</b>
     * /ld-services/query
     *
     * @param jsonInput A JSON-encoded string which has the following form: <br>
     * { <br>
     * "query" : "select ?s ?p ...", <br>
     * "format" : "text/csv" <br>
     * } <br>
     * where
     * <ul>
     * <li>query - A string which represents the SPARQL query which will be
     * applied.<br>
     * <li>format - The format(MIME type) of the query results. <b>text/csv</b>,
     * <b>application/json</b>, <b>text/tab-separated-values</b>,
     * <b>application/sparql-results+xml.</b>
     * </ul>
     * @return A Response instance which has a JSON-encoded entity content with
     * the query results in the requested format.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryExecPOSTJSON(String jsonInput,
            @DefaultValue("") @QueryParam("token") String token) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String authToken = requestContext.getHeader("Authorization");
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.QUERY);
        if (authToken == null) {
            authToken = token;
        }
        message.setToken(authToken);
        if (jsonObject.size() != 2) {
            message.setMessage("JSON input message should have exactly 2 arguments.");
            message.setStatus(ResponseStatus.FAILED);
            return Response.status(400).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            String f = (String) jsonObject.get("format");
            return queryExecBlazegraph(f, q, namespace, authToken, message);
        }
    }

    /**
     * <b>POST</b> service which executes a SPARQL query and returns the results
     * in various formats. The query is applied on a namespace provided as a
     * path parameter. <br>
     * <b>URL:</b>
     * /ld-services/query/namespace/{namespace}
     *
     * @param namespace Path parameter which denotes the namespace in which the
     * query will be applied.
     * @param jsonInput A JSON-encoded string which has the following form: <br>
     * { <br>
     * "query" : "select ?s ?p ...", <br>
     * "format" : "text/csv" <br>
     * } <br>
     * where
     * <ul>
     * <li>query - A string which represents the SPARQL query which will be
     * applied.<br>
     * <li>format - The format(MIME type) of the query results. <b>text/csv</b>,
     * <b>application/json</b>, <b>text/tab-separated-values</b>,
     * <b>application/sparql-results+xml.</b>
     * </ul>
     * @return A Response instance which has a JSON-encoded entity content with
     * the query results in the requested format.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}")
    public Response queryExecPOSTJSONWithNS(String jsonInput,
            @PathParam("namespace") String namespace,
            @DefaultValue("") @QueryParam("token") String token) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String authToken = requestContext.getHeader("Authorization");
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.QUERY);
        if (authToken == null) {
            authToken = token;
        }
        message.setToken(authToken);
        if (jsonObject.size() != 2) {
            message.setMessage("JSON input message should have exactly 2 arguments.");
            message.setStatus(ResponseStatus.FAILED);
            return Response.status(400).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            String f = (String) jsonObject.get("format");
            return queryExecBlazegraph(f, q, namespace, authToken, message);
        }
    }

    private Response queryExecBlazegraph(String f, String q, String namespace, String authToken, MetadataMessageImpl message) throws IOException, UnsupportedEncodingException {
        boolean isTokenValid = module.checkToken(authToken);
        int statusInt;
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            statusInt = 401;
        } else if (f == null) {
            message.setMessage("Error in the provided format.");
            message.setStatus(ResponseStatus.FAILED);
            statusInt = 500;
        } else {
            String result = blazegraphRepRestful.executeSparqlQuery(q, namespace, f);
            message.setMessage(result);
            message.setStatus(ResponseStatus.SUCCEED);
            statusInt = 200;
        }
        mdp.publish(message);
        return Response.status(statusInt).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
    }
}
