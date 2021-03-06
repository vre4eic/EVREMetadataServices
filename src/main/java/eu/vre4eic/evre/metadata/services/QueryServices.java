/* 
 * Copyright 2017 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static eu.vre4eic.evre.metadata.services.virtuoso.QueryServices.ConvertToCountQuery;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("query")
@Produces(MediaType.APPLICATION_JSON)
public class QueryServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    String namespace = propertiesManager.getTripleStoreNamespace();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private BlazegraphRepRestful blazegraphRepRestful;
//    private AuthModule module;
//    private Publisher<MetadataMessage> mdp;

    /**
     * Creates a new instance of QueryServices
     */
    public QueryServices() {
    }

    @PostConstruct
    public void initialize() {
        blazegraphRepRestful = new BlazegraphRepRestful(propertiesManager.getTripleStoreUrl());
//        module = AuthModule.getInstance("tcp://v4e-lab.isti.cnr.it:61616");
//        mdp = PublisherFactory.getMetatdaPublisher();
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
            @DefaultValue("0") @QueryParam("timeout") int timeout,
            @DefaultValue("") @QueryParam("token") String token) throws IOException {
        String authToken = requestContext.getHeader("Authorization");
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.QUERY);
        if (authToken == null) {
            authToken = token;
        }
        message.setToken(authToken);
        return queryExecBlazegraph(timeout, f, q, namespace, authToken, message);
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryCountExecGETJSON(
            @DefaultValue("application/json") @QueryParam("format") String f,
            @QueryParam("query") String q,
            @DefaultValue("0") @QueryParam("timeout") int timeout,
            @DefaultValue("") @QueryParam("token") String token) throws IOException {
        String authToken = requestContext.getHeader("Authorization");
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.QUERY);
        if (authToken == null) {
            authToken = token;
        }
        message.setToken(authToken);
        return queryExecBlazegraph(timeout, f, ConvertToCountQuery(q), namespace, authToken, message);
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
     * @param f Query parameter which refers on the requested mimetype-format of
     * the results. The formats which are supported are:
     * <b>text/csv</b>,
     * <b>application/json</b>, <b>text/tab-separated-values</b>,
     * <b>application/sparql-results+xml.</b>
     * @param q Query parameter which has a string value representing the SPARQL
     * query which will be applied.
     * @return A Response instance which has an entity content with the query
     * results and the required format. <br>
     */
    @GET
    @Path("/namespace/{namespace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryExecGETJSONWithNS(
            @PathParam("namespace") String namespace,
            @DefaultValue("application/json") @QueryParam("format") String f,
            @QueryParam("query") String q,
            @DefaultValue("0") @QueryParam("timeout") int timeout,
            @DefaultValue("") @QueryParam("token") String token) throws UnsupportedEncodingException, IOException {
        String authToken = requestContext.getHeader("Authorization");
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.QUERY);
        if (authToken == null) {
            authToken = token;
        }
        message.setToken(authToken);
        return queryExecBlazegraph(timeout, f, q, namespace, authToken, message);
    }

    @GET
    @Path("/count/namespace/{namespace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryCountExecGETJSONWithNS(
            @PathParam("namespace") String namespace,
            @DefaultValue("application/json") @QueryParam("format") String f,
            @QueryParam("query") String q,
            @DefaultValue("0") @QueryParam("timeout") int timeout,
            @DefaultValue("") @QueryParam("token") String token) throws IOException {
        String authToken = requestContext.getHeader("Authorization");
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.QUERY);
        if (authToken == null) {
            authToken = token;
        }
        message.setToken(authToken);
        return queryExecBlazegraph(timeout, f, ConvertToCountQuery(q), namespace, authToken, message);
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
            int timeout = 0;
            if (jsonObject.get("timeout") == null) {
                timeout = 0;
            } else {
                timeout = (int) jsonObject.get("timeout");
            }
            String f = (String) jsonObject.get("format");
            return queryExecBlazegraph(timeout, f, ConvertToCountQuery(q), namespace, authToken, message);
        }
    }

    @POST
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryCountExecPOSTJSON(String jsonInput,
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
            int timeout;
            if (jsonObject.get("timeout") == null) {
                timeout = 0;
            } else {
                timeout = (int) jsonObject.get("timeout");
            }
            return queryExecBlazegraph(timeout, f, ConvertToCountQuery(q), namespace, authToken, message);
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
            int timeout;
            if (jsonObject.get("timeout") == null) {
                timeout = 0;
            } else {
                timeout = (int) jsonObject.get("timeout");
            }
            return queryExecBlazegraph(timeout, f, q, namespace, authToken, message);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("batch/namespace/{namespace}")
    public Response batchQueryExecPOSTJSONWithNS(String jsonInput,
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
        JSONArray result = new JSONArray();
        int status = 0;
        if (jsonObject.size() != 2) {
            message.setMessage("JSON input message should have exactly 2 arguments.");
            message.setStatus(ResponseStatus.FAILED);
            return Response.status(400).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
        } else {
            String queriesStr = (String) jsonObject.get("query");
            JSONArray queries = (JSONArray) jsonParser.parse(queriesStr);
            String f = (String) jsonObject.get("format");
            for (int i = 0; i < queries.size(); i++) {
                String query = (String) queries.get(i);
                Response resp = blazegraphRepRestful.executeSparqlQuery(query, namespace, f);
                status = resp.getStatus();
                String data = resp.readEntity(String.class);
                if (status != 200) {
                    return Response.status(status).entity(resp.readEntity(String.class)).header("Access-Control-Allow-Origin", "*").build();
                }
                result.add(data);
            }
        }
        return Response.status(status).entity(result.toJSONString()).header("Access-Control-Allow-Origin", "*").build();
    }

//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Path("/namespace/{namespace}")
//    public Response multiQueryExecPOSTJSONWithNS(String jsonInput,
//            @PathParam("namespace") String namespace,
//            @DefaultValue("") @QueryParam("token") String token) throws IOException, ParseException {
//        JSONParser jsonParser = new JSONParser();
//        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
//        String authToken = requestContext.getHeader("Authorization");
//        MetadataMessageImpl message = new MetadataMessageImpl();
//        message.setOperation(MetadataOperationType.QUERY);
//        if (authToken == null) {
//            authToken = token;
//        }
//        message.setToken(authToken);
//        if (jsonObject.size() != 2) {
//            message.setMessage("JSON input message should have exactly 2 arguments.");
//            message.setStatus(ResponseStatus.FAILED);
//            return Response.status(400).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
//        } else {
//            String q = (String) jsonObject.get("query");
//            String f = (String) jsonObject.get("format");
//            return queryExecBlazegraph(f, q, namespace, authToken, message);
//        }
//    }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/count/namespace/{namespace}")
    public Response queryCountExecPOSTJSONWithNS(String jsonInput,
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
            int timeout;
            if (jsonObject.get("timeout") == null) {
                timeout = 0;
            } else {
                timeout = (int) jsonObject.get("timeout");
            }
            return queryExecBlazegraph(timeout, f, ConvertToCountQuery(q), namespace, authToken, message);
        }
    }

//    private String ConvertToCountQuery(String query) {
//        String queryTmp = query.toLowerCase();
//        int end = queryTmp.indexOf("from");
//        if (end == -1) {
//            end = queryTmp.indexOf("where");
//        }
//        int selectStart = queryTmp.indexOf("select");
//        int distinctStart = queryTmp.indexOf("distinct");
//        StringBuilder finalQuery = new StringBuilder();
//        if (distinctStart != -1) {
//            finalQuery.append(queryTmp.substring(0, distinctStart + "distinct".length()));
//        } else {
//            finalQuery.append(queryTmp.substring(0, selectStart + "select".length()));
//        }
//        finalQuery.append(" (count(*) as ?count) ").append(query.substring(end));
//        return finalQuery.toString();
//    }
    private Response queryExecBlazegraph(int timeout, String f, String q, String namespace, String authToken, MetadataMessageImpl message) throws IOException, UnsupportedEncodingException {
//        boolean isTokenValid = module.checkToken(authToken);
        boolean isTokenValid = true;
        System.out.println(q);
        int statusInt;
        Response response = null;
        String responseData = "";
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            statusInt = 401;
        } else if (f == null) {
            message.setMessage("Error in the provided format.");
            message.setStatus(ResponseStatus.FAILED);
            statusInt = 500;
        } else {
            blazegraphRepRestful.setTimeout(timeout);
            response = blazegraphRepRestful.executeSparqlQuery(q, namespace, f);
            statusInt = response.getStatus();
            responseData = response.readEntity(String.class);
            if (responseData.contains("com.bigdata.bop.engine.QueryTimeoutException")) {
                statusInt = 408;
                message.setStatus(ResponseStatus.FAILED);
                message.setMessage("The query was not finished due to a timeout error.");
            } else if (statusInt == 200) {
                message.setStatus(ResponseStatus.SUCCEED);
                message.setMessage("Query was executed successfully.");
            } else {
                message.setStatus(ResponseStatus.FAILED);
                message.setMessage(responseData);
            }
        }
//        mdp.publish(message);
        if (statusInt == 200) {
            return Response.status(statusInt).entity(responseData).header("Access-Control-Allow-Origin", "*").build();
        } else {
            JSONObject result = new JSONObject();
            result.put("response_status", message.getStatus().toString());
            result.put("message", message.getMessage());
            return Response.status(statusInt).entity(result.toString()).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    private Response queryExecPlainBlazegraph(int timeout, String f, String q, String namespace) throws IOException, UnsupportedEncodingException {
        Response response = null;
        blazegraphRepRestful.setTimeout(timeout);
        response = blazegraphRepRestful.executeSparqlQuery(q, namespace, f);
        int statusInt = response.getStatus();
        return Response.status(statusInt).entity(response.readEntity(String.class)).header("Access-Control-Allow-Origin", "*").build();
    }

    public static void main(String[] args) {

        QueryServices service = new QueryServices();
        String query = "PREFIX cerif: <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?persName ?Service (?pers as ?uri) \n"
                + "from <http://ekt-data> \n"
                + "from <http://rcuk-data> \n"
                + "from <http://fris-data> \n"
                + "from <http://epos-data> \n"
                + "from <http://envri-data>\n"
                + "where {\n"
                + "?pers cerif:is_source_of ?FLES.  \n"
                + "?FLES cerif:has_destination ?Ser.  \n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?pers a cerif:Person.  \n"
                + "?pers rdfs:label ?persName. \n"
                + "?persName bds:search ' maria'.  \n"
                + "?persName bds:matchAllTerms \"true\".  \n"
                + "?persName bds:relevance ?score. \n"
                + "}  ORDER BY desc(?score) ?pers limit 100";
        System.out.println(ConvertToCountQuery(query));

    }

}
