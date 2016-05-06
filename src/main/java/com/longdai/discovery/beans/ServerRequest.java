package com.longdai.discovery.beans;

import com.longdai.discovery.em.Command;

/**
 * Created by karl on 2016/5/5.
 */
public class ServerRequest {

    Command command;
    String agentID;
    String requestId;
    ServerInfo data;

    public ServerRequest() {
    }

    public ServerRequest(Command command, String agentID, String requestId, ServerInfo data) {
        this.command = command;
        this.agentID = agentID;
        this.requestId = requestId;
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestData{");
        sb.append("command=").append(command);
        sb.append(", agentID='").append(agentID).append('\'');
        sb.append(", requestId='").append(requestId).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ServerInfo getData() {
        return data;
    }

    public void setData(ServerInfo data) {
        this.data = data;
    }
}
