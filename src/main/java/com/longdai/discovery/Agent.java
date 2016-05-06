package com.longdai.discovery;

import com.longdai.discovery.beans.ServerInfo;

import java.util.List;

/**
 * Created by karl on 2016/5/5.
 */
public interface Agent {

    public static final String GROUP = "239.125.100.211";
    public static final int    PORT  = 50211;
    public static final int    TTL   = 63;

    public static final int    COMMEND_LEN   = 8;
    public static final int    AGENTID_LEN   = 64;
    public static final int    UUID_LEN      = 64;

    void start();

    void stop();

    void broadcast();

    List<ServerInfo> search(String serverType);

    void subscribe(ServerListener listener);

    void subscribe(ServerListener listener, String serverType);

    void unsubscribe(ServerListener listener);

    void unsubscribe(ServerListener listener, String serverType);

}
