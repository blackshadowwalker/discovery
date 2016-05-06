package com.longdai.discovery.beans;

import com.longdai.discovery.em.Command;

/**
 * Created by karl on 2016/5/5.
 */
public class ServerResponse {

    Command command;
    String agentID;
    String responseId;
    ServerInfo data;

    public ServerResponse(Command command, String agentID, String responseId, ServerInfo data) {
        this.command = command;
        this.agentID = agentID;
        this.responseId = responseId;
        this.data = data;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public ServerInfo getData() {
        return data;
    }

    public void setData(ServerInfo data) {
        this.data = data;
    }

}
