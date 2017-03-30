/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.core.messages.impl;

import eu.vre4eic.evre.core.Common.MetadataOperationType;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.messages.MetadataMessage;
import org.json.simple.JSONObject;

/**
 *
 * @author rousakis
 */
public class MetadataMessageImpl extends MessageImpl implements MetadataMessage {

    String token;
    MetadataOperationType operation;

    public MetadataMessageImpl() {
        super();
    }

    public MetadataMessageImpl(String message, ResponseStatus status, String token, MetadataOperationType operation) {
        super(message, status);
        this.token = token;
        this.operation = operation;
    }

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public MetadataOperationType getOperation() {
        return this.operation;
    }

    @Override
    public void setOperation(MetadataOperationType op) {
        this.operation = op;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put("token", this.token);
        object.put("operation", this.operation.toString());
        object.put("message", this.message);
        object.put("status", ""+this.status);
        return object;
    }

}
