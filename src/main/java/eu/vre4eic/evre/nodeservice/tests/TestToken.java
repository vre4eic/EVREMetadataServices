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
package eu.vre4eic.evre.nodeservice.tests;

import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;

/**
 *
 * @author rousakis
 */
public class TestToken {

    public static void main(String[] args) {
        AuthModule module = AuthModule.getInstance("tcp://v4e-lab.isti.cnr.it:61616");
        String token = "rous";
        Boolean auth = module.checkToken(token);
        String result = auth ? "User authenticated, operation permitted." : "User not authenticated!";
        System.out.println(result + " Token: " + token);
    }
}
