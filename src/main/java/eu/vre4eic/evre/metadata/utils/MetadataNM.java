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

import eu.vre4eic.evre.core.Common;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.springframework.context.annotation.Configuration;

import eu.vre4eic.evre.core.comm.NodeLinker;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import java.util.Properties;

/**
 *
 * @author rousakis
 */
@Configuration
@WebListener
public class MetadataNM implements ServletContextListener {

    private static AuthModule module = null;
    private static NodeLinker node = null;
    private static WorkflowListener wfModule = null;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("******************MetadataService context stopping ");
        NodeLinker.close();
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        System.out.println("******************MetadataService context initialized " + this);
        //ZKServer.init();
        PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
        Properties prop = propertiesManager.getProperties();
        //init the e-VRE communication infrastructure bus
        node = NodeLinker.init(prop.getProperty("nodelink.url"));
        //register the service, this operation adds a new entry in the service registry
        System.out.println("--> ");
        node.addService(Common.BuildingBlocks.MetadataService.toString(),
                "The e-VRE building block that manages the Metadata Repository",
                prop.getProperty("metadata.endpoint"));
        String brokerURL = node.getMessageBrokerURL();
        /*
		 * 1 access Authentication Messages 
         */
        //initialize the asynchronous communication module used to access the authentication messages 
        module = AuthModule.getInstance(brokerURL);

        /*
		 * 2 read and process  Metadata Messages 
         */
        //initialize the asynchronous communication module used to access Metadata messages, it will also contain the 
        // logic to process messages
        wfModule = WorkflowListener.getInstance(brokerURL);

        //the following lists all the valid token
        wfModule.listMessages();
    }

    public static AuthModule getModule() {
        return module;
    }

    public static WorkflowListener getWfModule() {
        return wfModule;
    }

}
