package com.longdai.discovery;

import com.longdai.discovery.beans.ServerInfo;
import com.longdai.discovery.udp.UDPAgent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

/**
 * Created by karl on 2016/5/5.
 */
public class Bootstrap {
    private Log log = LogFactory.getLog(Bootstrap.class);

    public static void main(String [] args) throws Exception {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setType("longdai");
        serverInfo.setSubType("api");
        serverInfo.setName("AgentMain");

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.init(serverInfo);
        bootstrap.start();

        bootstrap.subscribe(new ServerListener() {
            @Override
            public void notify(ServerInfo serverInfo, String agentID, String responseId) {
                System.out.println("Find ServerInfo: " + serverInfo.getServerUrl() + " at " + serverInfo.getHostname());
            }
        });


        List<ServerInfo> list = bootstrap.search("longdai");
        System.out.println("Search:");
        for (ServerInfo info : list) {
            System.out.println("ServerInfo: " + info.getSubType() +  " "+ info.getServerUrl() + " at " + info.getHostname());
        }


        Thread hoodThread = new Thread(){
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(1000 * 5);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        hoodThread.setDaemon(false);
        hoodThread.setName("Discovery Bootstrap");
        hoodThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                bootstrap.stop();
            }
        });
        System.out.println("--------------------------------------");
    }

    Agent agent;
    InetAddress local;

    public void init(final ServerInfo serverInfo) throws Exception {
        local = InetAddress.getLocalHost();
        String hostName = System.getenv("HOSTNAME");
        if (hostName==null){
            hostName = System.getenv("COMPUTERNAME");
        }
        if (hostName == null){
            hostName = local.getHostName();
        }

        int port = 8080;
        Set<String> keys = System.getProperties().stringPropertyNames();
        for (String key: keys){
            if(key.endsWith("port")){
                String value = System.getProperties().getProperty(key);
                log.info(key + "=" + value);
                port = Integer.parseInt(value);
                break;
            }
        }
        serverInfo.setHostname(hostName);
        serverInfo.setIp(local.getHostAddress());
        serverInfo.setPort(port);
        serverInfo.setServerUrl("http://" + local.getHostAddress() + ":" + port);
        agent = new UDPAgent(serverInfo);
    }

    public void start(){
        agent.start();
        agent.broadcast();
    }

    List<ServerInfo> search(String serverType) {
        return agent.search(serverType);
    }

    public void subscribe(ServerListener listener){
        agent.subscribe(listener);
    }

    public void stop(){
        agent.stop();
    }

}
