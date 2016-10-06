/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.ld.services;

import forth.ics.blazegraphutils.BlazegraphRepRestful;
import forth.ics.ld.utils.PropertiesManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
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

    @Context
    private UriInfo context;
    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();

    /**
     * Creates a new instance of UpdateClasses
     */
    public UpdateServices() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response updateExecPOSTJSON(String jsonInput) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        if (jsonObject.size() != 1) {
            String message = "JSON input message should have exactly 1 arguments.";
            String json = "{ \"success\" : false, "
                    + "\"message\" : \"" + message + "\" }";
            return Response.status(400).entity(json).header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            return updateExecBlazegraph(q);
        }
    }

    private Response updateExecBlazegraph(String q) throws IOException, UnsupportedEncodingException {
        String tripleStoreUrl = propertiesManager.getTripleStoreUrl();
        String tripleStoreNamespace = propertiesManager.getTripleStoreNamespace();
        BlazegraphRepRestful blaze = new BlazegraphRepRestful(tripleStoreUrl);
        Response resp = blaze.executeUpdateSparqlQuery(q, tripleStoreNamespace);
        return Response.status(resp.getStatus()).entity(resp.readEntity(String.class)).header("Access-Control-Allow-Origin", "*").build();
    }
}
