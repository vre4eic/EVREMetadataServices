/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.ld.services;

import forth.ics.blazegraphutils.BlazegraphRepRestful;
import forth.ics.ld.utils.PropertiesManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.client.ClientProtocolException;
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
    private PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    private BlazegraphRepRestful blazegraphRepRestful;

    /**
     * Creates a new instance of ImportServices
     */
    public ImportServices() {
    }

    @PostConstruct
    public void initialize() {
        blazegraphRepRestful = new BlazegraphRepRestful(propertiesManager.getTripleStoreUrl());
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
            String tripleStoreNamespace = propertiesManager.getTripleStoreNamespace();
            System.out.println(jsonObject.toString());
            Response result = blazegraphRepRestful.importFile(filename, format, tripleStoreNamespace, graph);
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

    @POST
    @Path("/namespace/{namespace}")
    //@Consumes("application/rdf+xml")//RDFFormat.RDFXML//{MediaType.MULTIPART_FORM_DATA //application/rdf+xml
    public Response uploadFileWithData(InputStream incomingData,
            @PathParam("namespace") String namespace,
            @QueryParam("namegraph") String namegraph,
            @HeaderParam("content-type") String contentType) throws ClientProtocolException, IOException {

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
        String line = null;
        while ((line = in.readLine()) != null) {
            stringBuilder.append(line);
        }
        // Triplestore Stuff
        Response tripleStoreResponse = blazegraphRepRestful.importDataString(
                stringBuilder.toString(), // String with RDF's content
                contentType, // Content type (i.e. application/rdf+xml)
                namespace, // Namespace
                namegraph); // NameGraph
        return Response.status(tripleStoreResponse.getStatus()).entity(tripleStoreResponse.readEntity(String.class)).build();
    }

}
