/* 
 * Copyright 2017 rousakis.
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

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rousakis
 */
public class PropertiesManager {

    private static PropertiesManager propManager = null;
    private static final String initFilePath = "config.properties";
    private static Properties prop;

    public static PropertiesManager getPropertiesManager() {
        if (propManager == null) {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream input = classLoader.getResourceAsStream(initFilePath);
                prop = new Properties();
                if (input != null) {
                    prop.load(input);
                }
                propManager = new PropertiesManager();
            } catch (IOException ex) {
                Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return propManager;
    }

    public String getPropertyValue(String key) {
        return prop.getProperty(key);
    }

    public Properties getProperties() {
        return prop;
    }

    public String getTripleStoreUrl() {
        return prop.getProperty("triplestore.url");
    }

    public String getTripleStoreNamespace() {
        return prop.getProperty("triplestore.namespace");
    }
}
