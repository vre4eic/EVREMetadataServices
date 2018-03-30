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

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author rousakis
 */
@javax.ws.rs.ApplicationPath("")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(eu.vre4eic.evre.metadata.services.ExportServices.class);
        resources.add(eu.vre4eic.evre.metadata.services.ImportServices.class);
        resources.add(eu.vre4eic.evre.metadata.services.InfoService.class);
        resources.add(eu.vre4eic.evre.metadata.services.QueryServices.class);
        resources.add(eu.vre4eic.evre.metadata.services.UpdateServices.class);
        resources.add(eu.vre4eic.evre.metadata.services.virtuoso.ExportServices.class);
        resources.add(eu.vre4eic.evre.metadata.services.virtuoso.ImportServices.class);
        resources.add(eu.vre4eic.evre.metadata.services.virtuoso.QueryServices.class);
        resources.add(eu.vre4eic.evre.metadata.services.virtuoso.UpdateServices.class);
    }
    
}
