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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.client.ClientProtocolException;
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
     * Creates a new instance of ImportServices
     */
    public ImportServices() {
    }

    @PostConstruct
    public void initialize() {
        blazegraphRepRestful = new BlazegraphRepRestful(propertiesManager.getTripleStoreUrl());
        module = AuthModule.getInstance("tcp://v4e-lab.isti.cnr.it:61616");
        mdp = PublisherFactory.getMetatdaPublisher();
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
    @Path("/path")
    public Response importFilePathPOSTJSON(String jsonInput,
            @DefaultValue("") @QueryParam("token") String token) throws ParseException, IOException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String result;
        int status;
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        boolean isTokenValid = module.checkToken(authToken);
//        isTokenValid = true;
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.INSERT);
        message.setToken(authToken);
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            status = 401;
        } else if (jsonObject.size() != 3) {
            message.setMessage("JSON input message should have exactly 3 arguments.");
            message.setStatus(ResponseStatus.FAILED);
            status = 400;
        } else {
            String tripleStoreNamespace = this.namespace;
            result = importFile(jsonObject, tripleStoreNamespace);
            if (result == null) {
                result = "Error in parsing file w.r.t. the given format.";
                message.setStatus(ResponseStatus.FAILED);
                status = 500;
            } else {
                org.json.JSONObject jsonMessage = new org.json.JSONObject(result);
                message.setJsonMessage(jsonMessage);
                message.setStatus(ResponseStatus.SUCCEED);
                status = 200;
            }
            message.setMessage(result);
        }
        mdp.publish(message);
        return Response.status(status).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/path/namespace/{namespace}")
    public Response importFilePathPOSTJSONWithNS(String jsonInput,
            @PathParam("namespace") String namespace,
            @DefaultValue("") @QueryParam("token") String token) throws ParseException, IOException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String result;
        int status;
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        boolean isTokenValid = module.checkToken(authToken);
//        isTokenValid = true;
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.INSERT);
        message.setToken(authToken);
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            status = 401;
        } else if (jsonObject.size() != 3) {
            message.setMessage("JSON input message should have exactly 3 arguments.");
            message.setStatus(ResponseStatus.FAILED);
            status = 400;
        } else {
            String tripleStoreNamespace = namespace;
            result = importFile(jsonObject, tripleStoreNamespace);
            if (result == null) {
                result = "Error in parsing file w.r.t. the given format.";
                message.setStatus(ResponseStatus.FAILED);
                status = 500;
            } else {
                org.json.JSONObject jsonMessage = new org.json.JSONObject(result);
                message.setJsonMessage(jsonMessage);
                message.setStatus(ResponseStatus.SUCCEED);
                status = 200;
            }
            message.setMessage(result);
        }
        mdp.publish(message);
        return Response.status(status).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
    }

    /**
     * <b>POST</b> service which imports an RDF data String with a given format
     * in a specific named graph. The named graph will be created in a default
     * namespace which is defined in a configuration file. <br>
     * <b>URL:</b>
     * /ld-services/import?graph={graph}
     *
     * @param incomingData The String object which contains the RDF data to be
     * imported via post.
     * @param graph The named graph URI in which the data will be inserted.
     * @param contentType The mimetype of the data contained in the data String.
     * Supported formats are:
     * <ul>
     * <li><b>application/rdf+xml</b>: rdf, rdfs, owl, xml data</li>
     * <li><b>text/plain</b>: nt triples</li>
     * <li><b>application/x-turtle</b>: ttl triples </li>
     * <li>etc.</li>
     * </ul>
     * The complete list with the accepted mimetypes can be found in
     * <a href="https://wiki.blazegraph.com/wiki/index.php/REST_API#MIME_Types">https://wiki.blazegraph.com/wiki/index.php/REST_API#MIME_Types</a>.
     * @return A response from the service which denotes whether the data were
     * imported or not.
     * @throws ClientProtocolException
     * @throws IOException
     */
    @POST
    public Response importFileContentsPOSTJSON(InputStream incomingData,
            @QueryParam("graph") String graph,
            @HeaderParam("content-type") String contentType,
            @DefaultValue("") @QueryParam("token") String token) throws ClientProtocolException, IOException {
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        boolean isTokenValid = module.checkToken(authToken);
//        isTokenValid = true;
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.INSERT);
        message.setToken(authToken);
        int status = 0;
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            status = 401;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            // Triplestore Stuff
            String tripleStoreResponse = blazegraphRepRestful.importFileData(
                    stringBuilder.toString(), // String with RDF's content
                    contentType, // Content type (i.e. application/rdf+xml)
                    this.namespace, // Namespace
                    graph); // NameGraph
            if (tripleStoreResponse == null) {
                message.setMessage("Error in parsing file w.r.t. the given format.");
                message.setStatus(ResponseStatus.FAILED);
                status = 500;
            } else {
                message.setMessage(tripleStoreResponse);
                org.json.JSONObject jsonMessage = new org.json.JSONObject(tripleStoreResponse);
                message.setJsonMessage(jsonMessage);
                message.setStatus(ResponseStatus.SUCCEED);
                status = 200;
            }
        }
        mdp.publish(message);
        return Response.status(status).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
    }

    /**
     * <b>POST</b> service which imports an RDF data String with a given format
     * in a specific named graph. The named graph will be created in an
     * <b>existing</b> namespace which is given as a path parameter. <br>
     * <b>URL:</b>
     * /ld-services/import?graph={graph}
     *
     * @param namespace Path parameter which denotes the namespace in which the
     * named graph with the inserted data will be created.
     * @param incomingData The String object which contains the RDF data to be
     * imported via post.
     * @param graph The named graph URI in which the data will be inserted.
     * @param contentType The mimetype of the data contained in the data String.
     * Supported formats are:
     * <ul>
     * <li><b>application/rdf+xml</b>: rdf, rdfs, owl, xml data</li>
     * <li><b>text/plain</b>: nt triples</li>
     * <li><b>application/x-turtle</b>: ttl triples </li>
     * <li>etc.</li>
     * </ul>
     * The complete list with the accepted mimetypes can be found in
     * <a href="https://wiki.blazegraph.com/wiki/index.php/REST_API#MIME_Types">https://wiki.blazegraph.com/wiki/index.php/REST_API#MIME_Types</a>.
     * @return A response from the service which denotes whether the data were
     * imported or not.
     * @throws ClientProtocolException
     * @throws IOException
     */
    @POST
    @Path("/namespace/{namespace}")
    public Response importFileContentsPOSTJSONWithNS(InputStream incomingData,
            @PathParam("namespace") String namespace,
            @QueryParam("graph") String graph,
            @HeaderParam("content-type") String contentType,
            @DefaultValue("") @QueryParam("token") String token) throws ClientProtocolException, IOException {
        int status = 0;
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        boolean isTokenValid = module.checkToken(authToken);
//        isTokenValid = true;
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.INSERT);
        message.setToken(authToken);
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            status = 401;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            // Triplestore Stuff
            String tripleStoreResponse = blazegraphRepRestful.importFileData(
                    stringBuilder.toString(), // String with RDF's content
                    contentType, // Content type (i.e. application/rdf+xml)
                    namespace, // Namespace
                    graph); // NameGraph
            if (tripleStoreResponse == null) {
                message.setMessage("Data to be imported are not valid. ");
                message.setStatus(ResponseStatus.FAILED);
                status = 400;
            } else {
                message.setMessage(tripleStoreResponse);
                org.json.JSONObject jsonMessage = new org.json.JSONObject(tripleStoreResponse);
                message.setJsonMessage(jsonMessage);
                message.setStatus(ResponseStatus.SUCCEED);
                status = 200;
            }
        }

//        mdp.publish(message);
        return Response.status(status).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
    }

    private String importFile(JSONObject jsonObject, String tripleStoreNamespace) throws IOException {
        String filename = (String) jsonObject.get("filename");
        String format = (String) jsonObject.get("format");
        String graph = (String) jsonObject.get("graph");
        String result = blazegraphRepRestful.importFilePath(filename, format, tripleStoreNamespace, graph);
        return result;
    }

}
