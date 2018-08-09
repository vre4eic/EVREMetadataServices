/**
 * *****************************************************************************
 * Copyright (c) 2018 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************
 */
package eu.vre4eic.evre.metadata.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.vre4eic.evre.core.Common;
import eu.vre4eic.evre.core.Common.MetadataOperationType;
import eu.vre4eic.evre.core.comm.MessageListener;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.Subscriber;
import eu.vre4eic.evre.core.comm.SubscriberFactory;
import eu.vre4eic.evre.core.messages.MetadataMessage;

public class WorkflowListener {

    private static Logger log = LoggerFactory.getLogger(WorkflowListener.class);

    private static WorkflowListener instance = null;
    private Hashtable<MetadataOperationType, List<MetadataMessage>> mdTable;
    Publisher<MetadataMessage> mdp;

    protected WorkflowListener(String brokerURL) throws JMSException {
        //initialize data structure for tokens
        mdTable = new Hashtable<Common.MetadataOperationType, List<MetadataMessage>>();

        log.info(" Connecting to Broker:: " + brokerURL);

        //subscribe Metadata_channel
        doSubcribe();
        log.info(" #### Listener Module ready ####");
    }

    public static WorkflowListener getInstance(String brokerURL) {
        if (instance == null) {
            try {
                instance = new WorkflowListener(brokerURL);
            } catch (JMSException e) {
                // TODO Auto-generated catch block
                log.info(e.getMessage());
                e.printStackTrace();
            }
        }
        return instance;

    }

    /**
     * It is a private method invoked during the class instantiation to register
     * a listener to the authentication channel
     *
     * @throws JMSException - JMS interfaces are used to connect to the provider
     */
    private void doSubcribe() throws JMSException {
        Subscriber<MetadataMessage> subscriber = SubscriberFactory.getMetadataSubscriber();

        subscriber.setListener(new MessageListener<MetadataMessage>() {
            //@Override
            public void onMessage(MetadataMessage mdm) {
//                addMessageToLocalTable(mdm);
                processMessage(mdm);
            }
        });

        // Forces thread switch to receive early notification on Auth_channel
        // TODO improve handshake
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }

    }

    /**
     * Method to process the message - CHANGE THIS METHOD
     *
     * @param mdm - MetadataMessage
     */
    protected void processMessage(MetadataMessage mdm) {
//        System.out.println("Message received with operation: " + mdm.getOperation().toString());
        if (mdm.getOperation() == MetadataOperationType.INSERTWORKFLOW) {
            try {
                WorkflowData workflowObj = new WorkflowData(mdm.getJsonMessage());
                workflowObj.storeWorkflowData();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    /**
     * utility to print the table of the operation on the metadata catalogue,
     * for testing purposes only
     */
    public void listMessages() {
        log.info("");
        log.info("");

        //add your logic here
        for (Entry<Common.MetadataOperationType, List<MetadataMessage>> tokenEntry : mdTable.entrySet()) {
            log.info("------------------ Token " + tokenEntry.getKey() + "  ----------------------");
            for (MetadataMessage entry : tokenEntry.getValue()) {
                log.info(entry.getMessage().toString() + ", " + entry.getOperation().toString());
            }
            log.info("---------------------------------------------------------------------- ");

        }

        log.info("#### ------------------MetaData actions listed by operations---------------------- ####");
    }

    protected void addMessageToLocalTable(MetadataMessage mdm) {
        synchronized (mdTable) {
            List<MetadataMessage> list = mdTable.get(mdm.getOperation());
            if (list == null) {
                list = new ArrayList<MetadataMessage>();
                mdTable.put(mdm.getOperation(), list);
            }
            list.add(mdm);
        }
    }
}
