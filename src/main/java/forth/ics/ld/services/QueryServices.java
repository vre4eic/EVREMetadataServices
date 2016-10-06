/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.ld.services;

import forth.ics.blazegraphutils.BlazegraphRepRestful;
import forth.ics.blazegraphutils.QueryResultFormat;
import forth.ics.blazegraphutils.Utils;
import forth.ics.ld.utils.PropertiesManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
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
@Path("query")
public class QueryServices {

    @Context
    private UriInfo context;

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();

    /**
     * Creates a new instance of QueryServices
     */
    public QueryServices() {
    }

    /**
     * Retrieves representation of an instance of
     * forth.ics.ld.services.QueryServices
     *
     * @param q
     * @return an instance of java.lang.String
     */
    @GET
    public Response queryExecGETJSON(@QueryParam("q") String q,
            @QueryParam("f") String f) throws IOException {
        String jsonMessage;

        if (f == null) {
            String message = "Error in the provided format.";
            jsonMessage = "{ \"success\" : false, "
                    + "\"result\" : \"" + message + "\" }";
            return Response.status(500).entity(jsonMessage).header("Access-Control-Allow-Origin", "*").build();
        } else {
            return queryExecBlazegraph(f, q);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryExecPOSTJSON(String jsonInput) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        if (jsonObject.size() != 2) {
            String message = "JSON input message should have exactly 2 arguments.";
            String json = "{ \"success\" : false, "
                    + "\"message\" : \"" + message + "\" }";
            return Response.status(400).entity(json).header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            String f = (String) jsonObject.get("format");
            return queryExecBlazegraph(f, q);
        }
    }

    private Response queryExecBlazegraph(String f, String q) throws IOException, UnsupportedEncodingException {
        String tripleStoreUrl = propertiesManager.getTripleStoreUrl();
        String tripleStoreNamespace = propertiesManager.getTripleStoreNamespace();
        BlazegraphRepRestful blaze = new BlazegraphRepRestful(tripleStoreUrl);
        String jsonMessage;
        if (f == null) {
            String message = "Error in the provided format.";
            jsonMessage = "{ \"success\" : false, "
                    + "\"result\" : \"" + message + "\" }";
            return Response.status(500).entity(jsonMessage).header("Access-Control-Allow-Origin", "*").build();
        } else {
            return Response.status(200).entity(blaze.executeSparqlQuery(q, tripleStoreNamespace, f)).header("Access-Control-Allow-Origin", "*").build();
        }
    }
}
