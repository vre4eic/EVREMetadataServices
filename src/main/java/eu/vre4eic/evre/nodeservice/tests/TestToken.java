/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
