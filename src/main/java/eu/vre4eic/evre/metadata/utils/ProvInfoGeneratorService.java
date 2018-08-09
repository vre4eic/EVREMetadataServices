/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.utils;

import eu.vre4eic.evre.metadata.clients.usecases.QueryUseCaseTest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rousakis
 */
public class ProvInfoGeneratorService {

    private static final String VREPrefix = "http://139.91.183.70:8090/vre4eic/";
    private static final String CERIFPrefix = "http://eurocris.org/ontology/cerif#";
    private static final String VREClassifications = "http://vre/classifications";
    private static final String VREUsersProvInfo = "http://vre/users";
    //////////
    private String emailString;
    private String personName;
    private String roleString;
    private String orgUnitName;
    private String orgUrl;
    private String eAddressUri;
    private String authorizationToken;
    private String endpoint;
    private String importedByUUID;
    private final String personUri;
    private final String orgUnitUri;

    public ProvInfoGeneratorService(String personName, String emailString, String roleString, String orgName, String orgUrl, String endpoint, String authorizationToken) throws UnsupportedEncodingException {
        this.personName = personName;
        this.emailString = emailString;
        this.roleString = roleString;
        this.orgUnitName = orgName;
        this.orgUrl = orgUrl;
        this.authorizationToken = authorizationToken;
        this.endpoint = endpoint;
        this.personUri = VREPrefix + "Person." + URLEncoder.encode(emailString, "UTF-8");
        this.orgUnitUri = VREPrefix + "OrgUnit." + URLEncoder.encode(orgUrl, "UTF-8");
        this.eAddressUri = VREPrefix + "EAddress." + URLEncoder.encode(emailString, "UTF-8");
    }

    public Set<String> personTriples() {
        Set<String> triples = new LinkedHashSet<>();
        triples.add("<" + personUri + "> a <" + CERIFPrefix + "Person>. \n");
        triples.add("<" + personUri + "> <" + CERIFPrefix + "has_name> \"" + personName + "\". \n");
        triples.add("<" + personUri + "> rdfs:label \"" + personName + "\". \n");
        triples.add("<" + eAddressUri + "> a <" + CERIFPrefix + "ElectronicAddress>. \n");
        triples.add("<" + eAddressUri + "> rdfs:label \"" + emailString + "\". \n");
        triples.add("<" + eAddressUri + "> <" + CERIFPrefix + "has_URI> \"" + emailString + "\". \n");
        return triples;
    }

    public Set<String> orgTriples() {
        Set<String> triples = new LinkedHashSet<>();
        triples.add("<" + orgUnitUri + "> a <" + CERIFPrefix + "OrganisationUnit>. \n");
        triples.add("<" + orgUnitUri + "> <" + CERIFPrefix + "has_name> \"" + orgUnitName + "\". \n");
        triples.add("<" + orgUnitUri + "> rdfs:label \"" + orgUnitName + "\". \n");
        triples.add("<" + orgUnitUri + "> <" + CERIFPrefix + "has_URI> \"" + orgUnitUri + "\". \n");
        return triples;
    }

    public Set<String> generateTriples() throws IOException, ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String curDate = format.format(new Date()) + "T00:00:00";
        Set<String> triples = new LinkedHashSet<>();
        /////       
        String roleUUID = "urn:uuid:" + UUID.randomUUID().toString();
        triples.add("<" + roleUUID + "> a <" + CERIFPrefix + "SimpleLinkEntity>. \n");
        triples.add("<" + roleUUID + "> rdfs:label \"has role\". \n");
        triples.add("<" + roleUUID + "> <" + CERIFPrefix + "has_source> <" + personUri + ">. \n");
        String roleClassifUri = findClassifFromTerm(roleString);
        triples.add("<" + roleUUID + "> <" + VREPrefix + "has_classification> <" + roleClassifUri + ">. \n");
        /////
        importedByUUID = "urn:uuid:" + UUID.randomUUID().toString();
        triples.add("<" + importedByUUID + "> a <" + CERIFPrefix + "FullLinkEntity>. \n");
        triples.add("<" + importedByUUID + "> rdfs:label \"is imported by\". \n");
        String importClassifUri = findClassifFromRoleExpr("is imported by");
        triples.add("<" + importedByUUID + "> <" + CERIFPrefix + "has_classification> <" + importClassifUri + ">. \n");
        triples.add("<" + importedByUUID + "> <" + CERIFPrefix + "has_destination> <" + personUri + ">. \n");
        triples.add("<" + importedByUUID + "> <" + CERIFPrefix + "has_endDate> \"" + curDate + "\". \n");
        triples.add("<" + importedByUUID + "> <" + CERIFPrefix + "has_startDate> \"" + curDate + "\". \n");
        /////
        String hasElAddressUUID = "urn:uuid:" + UUID.randomUUID().toString();
        triples.add("<" + hasElAddressUUID + "> a <" + CERIFPrefix + "FullLinkEntity>. \n");
        triples.add("<" + hasElAddressUUID + "> rdfs:label \"has electronic address\". \n");
        String emailClassifUri = findClassifFromTerm("Email");
        triples.add("<" + hasElAddressUUID + "> <" + CERIFPrefix + "has_classification> <" + emailClassifUri + ">. \n");
        triples.add("<" + hasElAddressUUID + "> <" + CERIFPrefix + "has_destination> <" + eAddressUri + ">. \n");
        triples.add("<" + hasElAddressUUID + "> <" + CERIFPrefix + "has_source> <" + personUri + ">. \n");
        triples.add("<" + eAddressUri + "> <" + CERIFPrefix + "is_destination_of> <" + hasElAddressUUID + ">. \n");
        //////
        String persOrgUnitUUID = "urn:uuid:" + UUID.randomUUID().toString();
        triples.add("<" + persOrgUnitUUID + "> a <" + CERIFPrefix + "FullLinkEntity>. \n");
        triples.add("<" + persOrgUnitUUID + "> rdfs:label \"is member of\". \n");
        String memberOfClassifUri = findClassifFromRoleExpr("is member of");
        triples.add("<" + persOrgUnitUUID + "> <" + CERIFPrefix + "has_classification> <" + memberOfClassifUri + ">. \n");
        triples.add("<" + persOrgUnitUUID + "> <" + CERIFPrefix + "has_destination> <" + orgUnitUri + ">. \n");
        triples.add("<" + persOrgUnitUUID + "> <" + CERIFPrefix + "has_source> <" + personUri + ">. \n");
        //////
        triples.add("<" + personUri + "> <" + CERIFPrefix + "is_source_of> <" + persOrgUnitUUID + ">. \n");
        triples.add("<" + personUri + "> <" + CERIFPrefix + "is_source_of> <" + roleUUID + ">. \n");
        triples.add("<" + personUri + "> <" + CERIFPrefix + "is_source_of> <" + hasElAddressUUID + ">. \n");
        return triples;
    }

    public String findClassifFromTerm(String term) throws IOException, ParseException {
        String query = "select ?classif from <" + VREClassifications + "> where {\n"
                + "?classif <" + CERIFPrefix + "has_term> ?term. \n"
                + "filter (lcase(?term) = lcase(\"" + term + "\")).\n"
                + "}";
//        System.out.println(query);
//        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        QueryUseCaseTest test = new QueryUseCaseTest(endpoint);
        Response resp = test.executeSparqlQueryGETVirtuoso(query, "application/json", authorizationToken);
        JSONParser parser = new JSONParser();
        JSONObject result = (JSONObject) parser.parse(resp.readEntity(String.class));
        JSONArray results = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
        String uri = (String) ((JSONObject) ((JSONObject) results.get(0)).get("classif")).get("value");
        return uri;
    }

    public String findClassifFromRoleExpr(String roleExpr) throws IOException, ParseException {
        String query = "select ?classif from <" + VREClassifications + "> where {\n"
                + "  ?classif <" + CERIFPrefix + "has_roleExpression> \"" + roleExpr + "\".\n"
                + "}";
//        System.out.println(query);
        QueryUseCaseTest test = new QueryUseCaseTest(endpoint);
        Response resp = test.executeSparqlQueryGETVirtuoso(query, "application/json", authorizationToken);
        JSONParser parser = new JSONParser();
        JSONObject result = (JSONObject) parser.parse(resp.readEntity(String.class));
        JSONArray results = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
        String uri = (String) ((JSONObject) ((JSONObject) results.get(0)).get("classif")).get("value");
        return uri;
    }

    public String createProvTriplesInsertQuery(String dstGraph) throws IOException, ParseException {
        Set<String> triples = orgTriples();
        triples.addAll(personTriples());
        triples.addAll(generateTriples());
        StringBuilder query = new StringBuilder();
        query.append("insert data {graph <" + dstGraph + "> {\n");
        for (String triple : triples) {
            query.append(triple);
        }
        query.append("} \n}");
        return query.toString();
    }

    public static String CreateInsertQuery(Set<String> triples, String dstGraph) {
        StringBuilder query = new StringBuilder();
        query.append("insert data {graph <" + dstGraph + "> {\n");
        for (String triple : triples) {
            query.append(triple);
        }
        query.append("} \n}");
        return query.toString();
    }

    public String createLinkingInsertQuery(String dstGraph) {
        StringBuilder query = new StringBuilder();
        query.append("with <" + dstGraph + "> \n")
                .append("insert {\n")
                .append("?prov_service <" + CERIFPrefix + "is_source_of> <" + importedByUUID + ">. \n")
                .append("} where {\n ")
                .append("?s <" + CERIFPrefix + "is_source_of> ?FLE1.\n")
                .append("?FLE1 <" + CERIFPrefix + "has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n")
                .append("?FLE1 <" + CERIFPrefix + "has_destination> ?ProvService.\n")
                .append("}");
        return query.toString();
    }

    public static void main(String[] args) throws UnsupportedEncodingException, IOException, ParseException {
        String personName = "Vangelis Kritsotakis";
        String emailString = "vkrits@ics.forth.gr";
        String role = "Admin";
        String orgName = "FORTH-ICS-ISL";
        String orgUrl = "https://www.ics.forth.gr/";
        String endpoint = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
        String namespace = "rous";
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOb2RlU2VydmljZSIsInVzZXJJZCI6ImNlc2FyZSJ9.nU12Aeitg1cpNZPhG67di6PxONfVv4HU-IYPylNGiYw";
        ProvInfoGeneratorService info = new ProvInfoGeneratorService(personName, emailString, role, orgName, orgUrl,
                endpoint, token);

//        System.out.println(info.orgTriples());
//        System.out.println(info.personTriples());
        System.out.println(info.generateTriples());
//        RestClient client = new RestClient(endpoint, namespace, token);
//        Response resp = client.executeUpdatePOSTJSON(query.toString(), "rous", token);
//        System.out.println(resp.readEntity(String.class));
    }

}
