/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.ld.services;

import forth.ics.blazegraphutils.BlazegraphRepRestful;
import forth.ics.blazegraphutils.Utils;
import forth.ics.ld.utils.PropertiesManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ExportServices
     */
    public ExportServices() {
    }

    @GET
    public Response exportFileGETJSON(@QueryParam("g") String g,
            @QueryParam("f") String f) throws ParseException, IOException {
        String format = f;
        String graph = g;
        if (format == null) {
            String message = "Error in the provided format.";
            String jsonMessage = "{ \"success\" : false, "
                    + "\"result\" : \"" + message + "\" }";
            return Response.status(500).entity(jsonMessage).header("Access-Control-Allow-Origin", "*").build();
        } else {
            return exportBlazegraph(format, graph);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exportFilePOSTJSON(String jsonInput) throws ParseException, IOException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String jsonResult;
        int status;
        if (jsonObject.size() != 2) {
            String message = "JSON input message should have exactly 2 arguments.";
            status = 400;
            jsonResult = "{ \"success\" : false, "
                    + "\"message\" : \"" + message + "\" }";
            return Response.status(status).entity(jsonResult).header("Access-Control-Allow-Origin", "*").build();
        } else {
            String format = (String) jsonObject.get("format");
            String graph = (String) jsonObject.get("graph");
            if (format == null) {
                String message = "Error in the provided format.";
                String jsonMessage = "{ \"success\" : false, "
                        + "\"result\" : \"" + message + "\" }";
                return Response.status(500).entity(jsonMessage).header("Access-Control-Allow-Origin", "*").build();
            } else {
                return exportBlazegraph(format, graph);
            }
        }
    }

    public Response exportBlazegraph(String format, String graph) throws IOException, UnsupportedEncodingException {
        String tripleStoreUrl = propertiesManager.getTripleStoreUrl();
        String tripleStoreNamespace = propertiesManager.getTripleStoreNamespace();
        BlazegraphRepRestful blaze = new BlazegraphRepRestful(tripleStoreUrl);
        return Response.status(200).entity(blaze.exportFile(format, tripleStoreNamespace, graph)).header("Access-Control-Allow-Origin", "*").build();
    }
}
