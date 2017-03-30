/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.utils;

import eu.vre4eic.evre.core.Common;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import javax.ws.rs.core.Response;

/**
 *
 * @author rousakis
 */
public class Authorization {

    public static Response checkAuthorization(AuthModule module, String token) {
        return null;
//        boolean auth = module.checkToken(token);
//        int status;
//        MetadataMessageImpl message = new MetadataMessageImpl();
//        if (!auth) {
//            message.setMessage("User not authenticated!");
//            message.setStatus(ResponseStatus.FAILED);
//            message.setToken(token);
//            status = 401;
//            return Response.status(status).entity(message.toJSON().toString()).header("Access-Control-Allow-Origin", "*").build();
//        }
//        return null;
    }

}
