/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        resources.add(eu.vre4eic.evre.metadata.services.QueryServices.class);
        resources.add(eu.vre4eic.evre.metadata.services.UpdateServices.class);
    }
    
}
