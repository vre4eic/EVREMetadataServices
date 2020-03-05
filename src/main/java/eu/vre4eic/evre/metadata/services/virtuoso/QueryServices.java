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
package eu.vre4eic.evre.metadata.services.virtuoso;

import eu.vre4eic.evre.core.Common.MetadataOperationType;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.PublisherFactory;
import eu.vre4eic.evre.core.messages.MetadataMessage;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import eu.vre4eic.evre.metadata.utils.MetadataNM;
import eu.vre4eic.evre.metadata.utils.PropertiesManager;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import gr.forth.ics.virtuoso.RestVirtRep;
import gr.forth.ics.virtuoso.SesameVirtRep;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
//import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
//import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.RepositoryException;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("query/virtuoso")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class QueryServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private SesameVirtRep virtuoso;
    private RestVirtRep restVirtuoso;
    private AuthModule module;
    private Publisher<MetadataMessage> mdp;

    /**
     * Creates a new instance of QueryServices
     */
    public QueryServices() {
    }

    @PostConstruct
    public void initialize() {
        Properties prop = propertiesManager.getProperties();
        try {
            virtuoso = new SesameVirtRep(
                    prop.getProperty("virtuoso.url"),
                    Integer.parseInt(prop.getProperty("virtuoso.port")),
                    prop.getProperty("virtuoso.username"),
                    prop.getProperty("virtuoso.password"));
        } catch (RepositoryException ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
        }
        restVirtuoso = new RestVirtRep(prop.getProperty("virtuoso.rest.url"));
        module = MetadataNM.getModule();
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
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
//        return queryExecVirtuoso(f, q, authToken, message);
        return queryExecVirtuoso2(timeout, f, q, authToken, message);
    }

    @GET
    @Path("/sesame")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response queryExecSesameGETJSON(
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

        return queryExecPlainVirtuoso(f, q, authToken, message);
//        return queryExecVirtuoso2(timeout, f, q, authToken, message);
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
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
//        return queryExecVirtuoso(f, ConvertToCountQuery(q), authToken, message);
        return queryExecVirtuoso2(timeout, f, ConvertToCountQuery(q), authToken, message);
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
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
            return Response.status(400).entity(message.toJSON()).
                    //header("Content-Type", "UTF-8").
                    header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            int timeout = 0;
            if (jsonObject.get("timeout") == null) {
                timeout = 0;
            } else {
                timeout = (int) jsonObject.get("timeout");
            }
            String f = (String) jsonObject.get("format");
//            return queryExecVirtuoso(f, ConvertToCountQuery(q), authToken, message);
            return queryExecVirtuoso2(timeout, f, q, authToken, message);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/count")
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
            return Response.status(400).entity(message.toJSON()).
                    //header("Content-Type", "UTF-8").
                    header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            String f = (String) jsonObject.get("format");
            int timeout;
            if (jsonObject.get("timeout") == null) {
                timeout = 0;
            } else {
                timeout = (int) jsonObject.get("timeout");
            }
//            return queryExecVirtuoso(f, ConvertToCountQuery(q), authToken, message);
            return queryExecVirtuoso2(timeout, f, ConvertToCountQuery(q), authToken, message);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/batch")
    public Response batchQueryExecPOSTJSONWithNS(String jsonInput,
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
            return Response.status(400).entity(message.toJSON()).
                    //header("Content-Type", "UTF-8").
                    header("Access-Control-Allow-Origin", "*").build();
        } else {
            String queriesStr = (String) jsonObject.get("query");
            JSONArray queries = (JSONArray) jsonParser.parse(queriesStr);
            String f = (String) jsonObject.get("format");
            for (int i = 0; i < queries.size(); i++) {
                String query = (String) queries.get(i);
//                Response resp = queryExecPlainVirtuoso(f, query);
                restVirtuoso.setTimeout(0);
                Response resp = restVirtuoso.executeSparqlQuery(query, f);
                status = resp.getStatus();
                String data = resp.readEntity(String.class);
                if (status != 200) {
                    return Response.status(status).entity(resp.readEntity(String.class)).
                            //header("Content-Type", "UTF-8").
                            header("Access-Control-Allow-Origin", "*").build();
                }
                result.add(data);
            }
        }
        return Response.status(status).entity(result.toJSONString()).
                //header("Content-Type", "UTF-8").
                header("Access-Control-Allow-Origin", "*").build();
    }

    public static String ConvertToCountQuery(String query) {
        String queryTmp = query.toLowerCase();
        int end = queryTmp.indexOf("from");
        if (end == -1) {
            end = queryTmp.indexOf("where");
        }
        int selectStart = queryTmp.indexOf("select");
        int distinctStart = queryTmp.indexOf("distinct");
        StringBuilder finalQuery = new StringBuilder();
        if (distinctStart != -1) {
            finalQuery.append(queryTmp.substring(0, distinctStart));
            finalQuery.append(" (count(distinct *) as ?count) ").append(query.substring(end));
        } else {
            finalQuery.append(queryTmp.substring(0, selectStart + "select".length()));
            finalQuery.append(" (count(*) as ?count) ").append(query.substring(end));
        }
        return finalQuery.toString();
    }

    private Response queryExecVirtuoso(String f, String q, String authToken, MetadataMessageImpl message) throws IOException, UnsupportedEncodingException {
//        boolean isTokenValid = module.checkToken(authToken);
        boolean isTokenValid = true;
        System.out.println("--using virtuoso sesame--");
        System.out.println(q);
        int statusInt;
        OutputStream output = null;
        String responseData = "";
        message.setJsonMessage(new org.json.JSONObject());
        try {
            if (!isTokenValid) {
                message.setMessage("User not authenticated!");
                message.setStatus(ResponseStatus.FAILED);
                statusInt = 401;
            } else if (f == null) {
                message.setMessage("Error in the provided format.");
                message.setStatus(ResponseStatus.FAILED);
                statusInt = 500;
            } else {
                TupleQuery tupleQuery = virtuoso.getCon().prepareTupleQuery(QueryLanguage.SPARQL, q);
                output = new OutputStream() {
                    private StringBuilder string = new StringBuilder();

                    @Override
                    public void write(int b) throws IOException {
                        this.string.append((char) b);
                    }

                    public String toString() {
                        return this.string.toString();
                    }
                };
                TupleQueryResultHandler writer;
                switch (f) {
                    case "application/sparql-results+xml":
                        writer = new SPARQLResultsXMLWriter(output);
                        break;
                    case "text/csv":
                        writer = new SPARQLResultsCSVWriter(output);
                        break;
                    case "text/tab-separated-values":
                        writer = new SPARQLResultsTSVWriter(output);
                        break;
                    case "application/json":
                        writer = new SPARQLResultsJSONWriter(output);
                        break;
                    default:
                        String result = "Invalid results format given.";
                        return Response.status(406).entity(result).header("Access-Control-Allow-Origin", "*").build();
                }
                tupleQuery.evaluate(writer);
                statusInt = 200;
            }
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException | TupleQueryResultHandlerException ex) {
            statusInt = 500;
        }
        if (statusInt == 200) {
            message.setStatus(ResponseStatus.SUCCEED);
            message.setMessage("Query was executed successfully.");
        } else {
            message.setStatus(ResponseStatus.FAILED);
            message.setMessage(responseData);
        }
        mdp.publish(message);
        if (statusInt == 200) {
            return Response.status(200).entity(output.toString()).
                    //header("Content-Type", "UTF-8").
                    header("Access-Control-Allow-Origin", "*").build();
        } else {
            JSONObject result = new JSONObject();
            result.put("response_status", message.getStatus().toString());
            result.put("message", message.getMessage());
            return Response.status(statusInt).entity(result.toString()).
                    //header("Content-Type", "UTF-8").
                    header("Access-Control-Allow-Origin", "*").build();
        }
    }

    private Response queryExecVirtuoso2(int timeout, String f, String q, String authToken, MetadataMessageImpl message) throws IOException, UnsupportedEncodingException {
        boolean isTokenValid = module.checkToken(authToken);
        message.setJsonMessage(new org.json.JSONObject());
//        boolean isTokenValid = true;
        System.out.println("--using virtuoso rest api --");
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
            restVirtuoso.setTimeout(timeout);
            response = restVirtuoso.executeSparqlQuery(q, f);
            statusInt = response.getStatus();
            responseData = response.readEntity(String.class);
        }
        if (statusInt == 200) {
            message.setStatus(ResponseStatus.SUCCEED);
            message.setMessage("Query was executed successfully.");
        }
        virtuoso.terminate();
        mdp.publish(message);
        if (statusInt == 200) {
            return Response.status(statusInt).entity(responseData).
                    //header("Content-Type", "UTF-8").
                    header("Access-Control-Allow-Origin", "*").build();
        } else {
            JSONObject result = new JSONObject();
            result.put("response_status", message.getStatus().toString());
            result.put("message", message.getMessage());
            return Response.status(statusInt).entity(result.toString()).
                    header("Access-Control-Allow-Origin", "*").
                    //header("Content-Type", "UTF-8").
                    build();
        }
    }

    private Response queryExecPlainVirtuoso(String f, String q, String authToken, MetadataMessageImpl message) throws IOException, UnsupportedEncodingException {
//        boolean isTokenValid = module.checkToken(authToken);
        boolean isTokenValid = true;
        int statusInt;
        System.out.println("--using sesame api --");
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            statusInt = 401;
        } else if (f == null) {
            message.setMessage("Error in the provided format.");
            message.setStatus(ResponseStatus.FAILED);
            statusInt = 500;
        } else {
            try {
                TupleQuery tupleQuery = virtuoso.getCon().prepareTupleQuery(QueryLanguage.SPARQL, q);
                OutputStream output = new OutputStream() {
                    private StringBuilder string = new StringBuilder();

                    @Override
                    public void write(int b) throws IOException {
                        this.string.append((char) b);
                    }

                    public String toString() {
                        return this.string.toString();
                    }
                };
                TupleQueryResultHandler writer;
                switch (f) {
                    case "application/sparql-results+xml":
                        writer = new SPARQLResultsXMLWriter(output);
                        break;
                    case "text/csv":
                        writer = new SPARQLResultsCSVWriter(output);
                        break;
                    case "text/tab-separated-values":
                        writer = new SPARQLResultsTSVWriter(output);
                        break;
                    case "application/json":
                        writer = new SPARQLResultsJSONWriter(output);
                        break;
                    default:
                        String result = "Invalid results format given.";
                        return Response.status(406).entity(result).build();
                }
                tupleQuery.evaluate(writer);
                String result = output.toString();
                virtuoso.terminate();
                return Response.status(200).entity(result).
                        header("Access-Control-Allow-Origin", "*").
                        //header("Content-Type", "UTF-8").
                        build();
            } catch (RepositoryException | MalformedQueryException | QueryEvaluationException | TupleQueryResultHandlerException ex) {
                message.setStatus(ResponseStatus.FAILED);
                message.setMessage(ex.getMessage());
                statusInt = 500;
                JSONObject result = new JSONObject();
                result.put("response_status", message.getStatus().toString());
                result.put("message", message.getMessage());
                return Response.status(statusInt).entity(result.toString()).
                        //header("Content-Type", "UTF-8").
                        header("Access-Control-Allow-Origin", "*").build();
            }
        }
        JSONObject result = new JSONObject();
        result.put("response_status", message.getStatus().toString());
        result.put("message", message.getMessage());
        return Response.status(statusInt).entity(result.toString()).
                //header("Content-Type", "UTF-8").
                header("Access-Control-Allow-Origin", "*").build();
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

        System.out.println(service.ConvertToCountQuery(query));

    }
}
