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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("import")
public class ImportServices {

    @Context
    private UriInfo context;
    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();

    /**
     * Creates a new instance of ImportServices
     */
    public ImportServices() {
    }

    /**
     * Retrieves representation of an instance of
     * forth.ics.ld.services.ImportServices
     *
     * @return an instance of java.lang.String
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importFilePOSTJSON(String jsonInput) throws ParseException, IOException {
        
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String jsonResult;
        int status;
        if (jsonObject.size() != 3) {
            String message = "JSON input message should have exactly 3 arguments.";
            status = 400;
            jsonResult = "{ \"success\" : false, "
                    + "\"message\" : \"" + message + "\" }";
        } else {
            String filename = (String) jsonObject.get("filename");
            String format = (String) jsonObject.get("format");
            String graph = (String) jsonObject.get("graph");
            String tripleStoreUrl = propertiesManager.getTripleStoreUrl();
            String tripleStoreNamespace = propertiesManager.getTripleStoreNamespace();
            BlazegraphRepRestful blaze = new BlazegraphRepRestful(tripleStoreUrl);
            System.out.println(jsonObject.toString());
            Response result = blaze.importFile(filename, format, tripleStoreNamespace, graph);
            status = result.getStatus();
            String resText = result.readEntity(String.class);
            String msg = "";
            boolean success = false;
            if (status == 200) {
                success = true;
                msg = XML.toJSONObject(resText).toString();
            } else {
                success = false;
                msg = "Error in parsing file w.r.t. the given format.";
            }
            jsonResult = "{ \"success\" : " + success + ", "
                    + "\"result\" : \"" + msg + "\" }";
        }
        return Response.status(status).entity(jsonResult).header("Access-Control-Allow-Origin", "*").build();
    }
}
