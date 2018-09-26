/*
 * Copyright 2018 rousakis.
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
package eu.vre4eic.evre.metadata.utils;

import eu.vre4eic.evre.metadata.clients.usecases.UpdateUseCaseTest;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rousakis
 */
public class WorkflowData {

    private String cerifNS = "http://eurocris.org/ontology/cerif#";
    private String vreNS = "http://139.91.183.70:8090/vre4eic/";
    private String wfName, wfCreator, wfDescription, wfUrl;
    private ProvInfoGeneratorService provInfo;
    private String metadataEndpoint, token;
    private String workflowsGraph;

    public WorkflowData(JSONObject workflowObj) throws UnsupportedEncodingException {
        this.wfName = (String) workflowObj.get("wf_name");
        this.wfCreator = (String) workflowObj.get("wf_creator");
        this.wfDescription = (String) workflowObj.get("wf_description");
        this.wfUrl = (String) workflowObj.get("url");
        PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
        Properties prop = propertiesManager.getProperties();
        this.metadataEndpoint = prop.getProperty("metadata.endpoint");
        this.workflowsGraph = prop.getProperty("workflows.graph");
        ///
        this.token = (String) workflowObj.get("token");
        String username = (String) workflowObj.get("user_name");
        String nodeservice = prop.getProperty("nodeservice.endpoint");
        Client client = ClientBuilder.newClient();
        Response response = client.target(nodeservice + "/user/getprofile").
                queryParam("token", token).//mimetype
                queryParam("userLogin", username).request().get();
        JSONObject userObj = new JSONObject(response.readEntity(String.class));
        provInfo = new ProvInfoGeneratorService(
                (String) userObj.get("name"), (String) userObj.get("email"), (String) userObj.get("role"),
                (String) userObj.get("organization"), (String) userObj.get("organizationURL"),
                metadataEndpoint, token
        );
        client.close();
    }

    public Set<String> getTriples() throws Exception {
        Set<String> triples = new LinkedHashSet<>();
        String workflowUri = vreNS + "Workflow." + UUID.randomUUID().toString();
        String creatorUri = vreNS + "Person." + UUID.nameUUIDFromBytes(wfCreator.getBytes("UTF-8")).toString();
        String aaUUID = "urn:uuid:" + UUID.randomUUID().toString();
        String acUUID = "urn:uuid:" + UUID.randomUUID().toString();
        //
//        String workflowProvUri = vreNS + "Workflow.Provenance." + uuid;
        triples.add("<" + workflowUri + "> a <" + cerifNS + "Workflow>. \n");
        triples.add("<" + workflowUri + "> <" + cerifNS + "has_URI" + "> <" + wfUrl + ">. \n");
        triples.add("<" + workflowUri + "> <http://searchable_text> \"" + wfName + " " + wfDescription + "\". \n");
        triples.add("<" + workflowUri + "> <" + cerifNS + "has_name" + "> \"" + wfName + "\". \n");
        triples.add("<" + workflowUri + "> rdfs:label \"" + wfName + "\". \n");
        triples.add("<" + workflowUri + "> <" + cerifNS + "has_description> \"" + wfDescription + "\". \n");
        triples.add("<" + workflowUri + "> <" + cerifNS + "is_destination_of> <" + acUUID + ">. \n");
        //
        triples.add("<" + workflowUri + "> <" + cerifNS + "is_source_of> <" + aaUUID + ">. \n");
        triples.add("<" + aaUUID + "> a <" + cerifNS + "SimpleLinkEntity>. \n");
        triples.add("<" + aaUUID + "> rdfs:label \"has_type\". \n");
        triples.add("<" + aaUUID + "> <" + cerifNS + "has_classification> <" + vreNS + "Classification.Taverna%20workflow>. \n");
        triples.add("<" + aaUUID + "> <" + cerifNS + "has_source> <" + workflowUri + ">. \n");
        //
        triples.add("<" + creatorUri + "> a <" + cerifNS + "Person" + ">. \n");
        triples.add("<" + creatorUri + "> <" + cerifNS + "has_name" + "> \"" + wfCreator + "\". \n");
        triples.add("<" + creatorUri + "> rdfs:label \"" + wfCreator + "\". \n");
        //
        triples.add("<" + creatorUri + "> <" + cerifNS + "is_source_of> <" + acUUID + ">. \n");
        triples.add("<" + acUUID + "> <" + cerifNS + "has_source> <" + creatorUri + ">. \n");
        triples.add("<" + acUUID + "> <" + cerifNS + "has_destination> <" + workflowUri + ">. \n");
        triples.add("<" + acUUID + "> a <" + cerifNS + "FullLinkEntity>. \n");
        String providedByClassifUri = provInfo.findClassifFromRoleExpr("provided");
        triples.add("<" + acUUID + "> <" + cerifNS + "has_classification> <" + providedByClassifUri + ">. \n");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String curDate = format.format(new Date()) + "T00:00:00";
        triples.add("<" + acUUID + "> <" + cerifNS + "has_startDate> \"" + curDate + "\". \n");
        triples.add("<" + acUUID + "> rdfs:label \"is provided by\". \n");
        triples.add("<" + acUUID + "> <" + cerifNS + "has_endDate> \"" + curDate + "\". \n");

//        triples.add("<" + creatorUri + ">  rdfs:label \"" + wfCreator + "\". \n");
//        triples.add("<" + workflowUri + "> <" + cerifNS + "is_source_of> <" + workflowProvUri + ">. \n");
//        triples.add("<" + workflowProvUri + "> <" + cerifNS + "has_source> <" + workflowUri + ">. \n");
//        triples.add("<" + workflowProvUri + "> a <" + cerifNS + "FullLinkEntity>. \n");
//        triples.add("<" + workflowProvUri + "> rdfs:label \"has provenance info\". \n");
//        triples.add("<" + workflowProvUri + "> <" + cerifNS + "has_classification> <" + vreNS + "Classification.Provenance>. \n");
//        triples.add("<" + workflowProvUri + "> <" + cerifNS + "has_destination> <" + vreNS + "Taverna.Workflow.Service>. \n");
//        triples.add("<" + vreNS + "Taverna.Workflow.Service> <" + cerifNS + "is_destination_of> <" + workflowProvUri + ">. \n");
        //
//        triples.add("<" + vreNS + "Taverna.Workflow.Service> a <" + cerifNS + "Service>. \n");
//        triples.add("<" + vreNS + "Taverna.Workflow.Service> <" + cerifNS + "has_name" + "> \"Taverna Workflow Generation Service\". \n");
        //
//        String acUUID = "urn:uuid:" + UUID.randomUUID().toString();
//        String aeUUID = "urn:uuid:" + UUID.randomUUID().toString();
//        String adUUID = "urn:uuid:" + UUID.randomUUID().toString();
//        triples.add("<" + vreNS + "Taverna.Workflow.Service> <" + cerifNS + "is_destination_of> <" + acUUID + ">. \n");
//        triples.add("<" + acUUID + "> <" + cerifNS + "has_destination> <" + vreNS + "Taverna.Workflow.Service>. \n");
//        triples.add("<" + vreNS + "Taverna.Workflow.Service> <" + cerifNS + "is_destination_of> <" + aeUUID + ">. \n");
////        triples.add("<" + aeUUID + "> <" + cerifNS + "has_destination> <" + vreNS + "Taverna.Workflow.Service>. \n");
//        triples.add("<" + vreNS + "Taverna.Workflow.Service> <" + cerifNS + "is_source_of> <" + adUUID + ">. \n");
//        triples.add("<" + adUUID + "> <" + cerifNS + "has_source> <" + vreNS + "Taverna.Workflow.Service>. \n");
//        triples.add("<" + acUUID + "> a <" + cerifNS + "FullLinkEntity>. \n");
//        triples.add("<" + acUUID + "> rdfs:label \"is provided by\". \n");
//        String providedByClassifUri = provInfo.findClassifFromRoleExpr("is provided by");
//        triples.add("<" + acUUID + "> <" + cerifNS + "has_classification> <" + providedByClassifUri + ">. \n");
//        String creatorUri = vreNS + "Person." + UUID.nameUUIDFromBytes(wfCreator.getBytes("UTF-8")).toString();
//        triples.add("<" + acUUID + "> <" + cerifNS + "has_source> <" + creatorUri + ">. \n");
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        String curDate = format.format(new Date()) + "T00:00:00";
//        triples.add("<" + acUUID + "> <" + cerifNS + "has_startDate> \"" + curDate + "\". \n");
//        triples.add("<" + acUUID + "> <" + cerifNS + "has_endDate> \"" + curDate + "\". \n");
//        //
//        triples.add("<" + adUUID + "> a <" + cerifNS + "SimpleLinkEntity>. \n");
//        triples.add("<" + adUUID + "> rdfs:label \"has_type\". \n");
//        triples.add("<" + adUUID + "> <" + cerifNS + "has_classification> <" + vreNS + "Classification.VRE4EIC%202%20workflow>. \n");
        return triples;
    }

    public ProvInfoGeneratorService getProvInfo() {
        return provInfo;
    }

    public void storeWorkflowData() throws Exception {
        Set<String> triples = new LinkedHashSet<>();
//        triples.addAll(provInfo.orgTriples());
//        triples.addAll(provInfo.personTriples());
//        triples.addAll(provInfo.generateTriples());
        triples.addAll(getTriples());
        UpdateUseCaseTest test = new UpdateUseCaseTest(metadataEndpoint);
        String workflowQuery = ProvInfoGeneratorService.CreateInsertQuery(triples, workflowsGraph);
        Response resp = test.executeUpdatePOSTJSON(workflowQuery, token);
        test.close();
        if (resp.getStatus() == 200) {
            System.out.println("Provenance and Workflow Triples were inserted.");
        }
        ///
//        test = new UpdateUseCaseTest(metadataEndpoint);
//        String linkTriples = provInfo.createLinkingInsertQuery(workflowsGraph);
//        resp = test.executeUpdatePOSTJSON(linkTriples, token);
//        if (resp.getStatus() == 200) {
//            System.out.println("Linking Triples were inserted.");
//        }
//        test.close();
        ///
        test = new UpdateUseCaseTest(metadataEndpoint);
        resp = test.executeUpdatePOSTJSON(SPARQLUpdates.matPersonWorkflow.replace("@#$%FROM%$#@", "<" + workflowsGraph + ">"), token);
        if (resp.getStatus() == 200) {
            System.out.println("Person-Workflow relation was materialized.");
        }
        test.close();

    }

    public static void main(String[] args) throws Exception {
        Set<String> loggers = new HashSet<>(Arrays.asList(
                "org.openrdf.rio",
                "org.apache.http",
                "groovyx.net.http",
                "org.eclipse.jetty.client",
                "org.eclipse.jetty.io",
                "org.eclipse.jetty.http",
                "o.e.jetty.util",
                "o.e.j.u.component",
                "org.openrdf.query.resultio"));
        for (String log : loggers) {
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(log);
            logger.setLevel(ch.qos.logback.classic.Level.INFO);
            logger.setAdditive(false);
        }

        JSONObject workflow = new JSONObject();
        workflow.put("wf_name", "name_of_the_workflow2");
        workflow.put("wf_creator", "Creator Name2");
        workflow.put("wf_description", "Description of the workflow2");
        workflow.put("user_name", "math");
        workflow.put("url", "scheme://url?path");
        workflow.put("token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOb2RlU2VydmljZSIsInVzZXJJZCI6Im1hdGgifQ.JK2TzOSTAX9M-90mBOBgN_AGoashunSXnhaLTPwJZnA");

        WorkflowData workflowObj = new WorkflowData(workflow);
        workflowObj.storeWorkflowData();

//        for (String triple : workflowObj.getTriples()) {
//            System.out.println(triple + ".");
//        }
        //exec q1
//        String q1 = workflowObj.getProvInfo().createProvTriplesInsertQuery(workflowsGraph);
//        System.out.println(q1 + "\n---------");
        //import workflow data
//        String q2 = ProvInfoGeneratorService.CreateInsertQuery(workflowObj.getTriples(), workflowsGraph);
//        System.out.println(q2 + "\n---------");
        //exec q3
//        String q3 = workflowObj.getProvInfo().createLinkingInsertQuery(workflowsGraph);
//        System.out.println(q3 + "\n---------");
    }

}
