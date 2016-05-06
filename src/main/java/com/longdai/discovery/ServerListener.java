package com.longdai.discovery;

import com.longdai.discovery.beans.ServerInfo;

/**
 * Created by karl on 2016/5/5.
 */
public interface ServerListener {

    void notify(ServerInfo serverInfo, String agentID, String responseId);

}
