package com.longdai.discovery.udp;

import com.alibaba.fastjson.JSON;
import com.longdai.discovery.Agent;
import com.longdai.discovery.ServerListener;
import com.longdai.discovery.beans.ServerInfo;
import com.longdai.discovery.beans.ServerRequest;
import com.longdai.discovery.beans.ServerResponse;
import com.longdai.discovery.em.Command;
import com.longdai.discovery.exception.AgentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by karl on 2016/5/5.
 */
public class UDPAgent implements Agent {
    private static Log log = LogFactory.getLog(UDPAgent.class);

    MulticastSocket multicastSocket;
    InetAddress group;
    Thread pingThread;
    Thread listenThread;
    volatile boolean run = false;
    volatile boolean listenrun = false;

    String agentID;
    ServerInfo serverInfo;

    public UDPAgent(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public void start() {
        try {
            if (run){
                return ;
            }
            agentID = UUID.randomUUID().toString();
            group = InetAddress.getByName(GROUP);
            multicastSocket = new MulticastSocket(PORT);
            multicastSocket.setTimeToLive(TTL);
            multicastSocket.setSoTimeout(1000*2);
            multicastSocket.joinGroup(group);

            run = true;
            listenrun = true;

            startListen();

            log.info("started");
        } catch (Exception e) {
            throw new AgentException(e.getMessage(), e);
        }
    }


    @Override
    synchronized public void stop() {
        try {
            log.info("stopping");
            run = false;
            listenrun = false;
            if (multicastSocket != null) {
                String requestId = UUID.randomUUID().toString();
                ServerResponse response = new ServerResponse(Command.OFFLINE, UDPAgent.this.agentID, requestId, this.serverInfo);
                byte[] sendData = UDPAgent.this.buildResponseBuffer(response);
                DatagramPacket pack = new DatagramPacket(sendData, sendData.length, group, PORT);
                multicastSocket.send(pack);
                log.info("Publish Offline: " + serverInfo.getServerUrl());

                multicastSocket.leaveGroup(group);
                multicastSocket.close();
                multicastSocket = null;
            }
            pingThread = null;
            listenerList.clear();
            listenerMap.clear();
            log.info("stopped");
        } catch (Exception e) {
            throw new AgentException(e.getMessage(), e);
        }
    }

    @Override
    public void broadcast() {
        try {
            String requestId = UUID.randomUUID().toString();
            ServerResponse response = new ServerResponse(Command.ONLINE, UDPAgent.this.agentID, requestId, this.serverInfo);
            byte[] sendData = UDPAgent.this.buildResponseBuffer(response);
            DatagramPacket pack = new DatagramPacket(sendData, sendData.length, group, PORT);
            multicastSocket.send(pack);
            log.info("Publish: " + serverInfo.getServerUrl());
        } catch (Exception e) {
            throw new AgentException(e.getMessage(), e);
        }
    }

    void startListen() throws Exception {
        if (listenThread != null)
            return;
        listenThread = new Thread() {
            @Override
            public void run() {
                byte buf[] = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                while (listenrun) {
                    try {
                        packet.setLength(buf.length);
                        multicastSocket.receive(packet);

                        ServerRequest request = parseRequestData(packet.getData(), packet.getLength());
                        if (request == null) {
                            continue;
                        }
                        if (request.getAgentID().equals(UDPAgent.this.agentID)) {
                            //this agent
                            continue;
                        }

                        String remoteHost = packet.getAddress().getHostAddress();
                        int remotePort = packet.getPort();
                        String remote = remoteHost + ":" + remotePort;

                        ServerResponse response = null;
                        ServerInfo server = request.getData();
                        switch (request.getCommand()) {
                            case PING:
                                log.debug("PING " + request.getRequestId() + " from " + remote);
                                response = new ServerResponse(Command.PONG, UDPAgent.this.agentID, request.getRequestId(), UDPAgent.this.serverInfo);
                                break;
                            case PONG:
                                log.debug("PONG " + request.getRequestId() + " from " + remote);
                                break;
                            case SEARCH:
                                log.info("SEARCH " + request.getRequestId() + " " + server.getType() + "/" + server.getSubType() + " from " + remote);
                                response = new ServerResponse(Command.ONLINE, UDPAgent.this.agentID, request.getRequestId(), UDPAgent.this.serverInfo);
                                break;
                            case ONLINE:
                                log.info("ONLINE " + request.getRequestId() + " [" + server.getName() + "/" +server.getHostname()+ "] " + server.getType() + "/"+ server.getSubType() + " Url:" + request.getData().getServerUrl());
                                notifyOnlineMsgListener(request);
                                break;
                            case OFFLINE:
                                break;
                            case UNKNOWN:
                                break;
                        }
                        if (response != null) {
                            byte[] sendData = UDPAgent.this.buildResponseBuffer(response);
                            DatagramPacket pack = new DatagramPacket(sendData, sendData.length, group, PORT);
                            multicastSocket.send(pack);
                        }
                    } catch (SocketTimeoutException e){
                        continue;
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        };

        listenThread.setDaemon(true);
        listenThread.setName("Discovery-Listen" + group.toString());
        listenThread.start();
    }

    ServerRequest parseRequestData(byte[] data, int dataLen) {
        try {
            ChannelBuffer buf = ChannelBuffers.copiedBuffer(data);
            int totalLen = buf.readInt();
            if (dataLen != totalLen) {
                //bad data
                // return null;
            }

            ServerRequest request = new ServerRequest();
            int cmdLen = buf.readInt();
            Command cmd = Command.parse(buf.readBytes(cmdLen).toString(Charset.forName("utf-8")));
            request.setCommand(cmd);

            int agentIdLen = buf.readInt();
            request.setAgentID(buf.readBytes(agentIdLen).toString(Charset.forName("utf-8")));

            int requestIdLen = buf.readInt();
            request.setRequestId(buf.readBytes(requestIdLen).toString(Charset.forName("utf-8")));

            int requestDataLen = buf.readInt();
            String temp = buf.readBytes(requestDataLen).toString(Charset.forName("utf-8"));
            request.setData(JSON.parseObject(temp, ServerInfo.class));

            return request;
        } catch (Exception e) {

        }
        return null;
    }

    byte[] buildResponseBuffer(ServerResponse serverPackage) {
        if (serverPackage == null)
            return null;
        byte[] cmd = serverPackage.getCommand().name().getBytes();
        byte[] agentId = serverPackage.getAgentID().getBytes();
        byte[] msgId = serverPackage.getResponseId().getBytes();
        byte[] tempSendJson = JSON.toJSONString(serverPackage.getData()).getBytes();

        int totalLength = cmd.length + agentId.length + msgId.length + tempSendJson.length;
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

        buffer.writeInt(totalLength);

        buffer.writeInt(cmd.length);
        buffer.writeBytes(cmd);

        buffer.writeInt(agentId.length);
        buffer.writeBytes(agentId);

        buffer.writeInt(msgId.length);
        buffer.writeBytes(msgId);

        buffer.writeInt(tempSendJson.length);
        buffer.writeBytes(tempSendJson);

        return buffer.array();
    }

    @Override
    synchronized public List<ServerInfo> search(String serverType) {
        try {
            final String requestId = UUID.randomUUID().toString();

            ServerResponse response = new ServerResponse(Command.SEARCH, UDPAgent.this.agentID, requestId, this.serverInfo);
            byte[] sendData = UDPAgent.this.buildResponseBuffer(response);
            final DatagramPacket pack = new DatagramPacket(sendData, sendData.length, group, PORT);

            final List<ServerInfo> serverInfoList = new ArrayList<ServerInfo>();
            ServerListener serverListener = new ServerListener() {
                @Override
                public void notify(ServerInfo serverInfo, String agentID, String responseId) {
                    if (requestId.equals(responseId)){
                        serverInfoList.add(serverInfo);
                    }
                }
            };
            this.subscribe(serverListener);
            multicastSocket.send(pack);

            Thread.sleep(5000);

            return serverInfoList;
        } catch (Exception e) {
            throw new AgentException(e.getMessage(), e);
        }
    }

    //发布消息，服务上线
    void notifyOnlineMsgListener(ServerRequest serverRequest) {
        ServerInfo serverInfo = serverRequest.getData();
        for (ServerListener listener : listenerList) {
            listener.notify(serverInfo, serverRequest.getAgentID(), serverRequest.getRequestId());
        }
        if (listenerMap.containsKey(serverInfo.getType())) {
            for (ServerListener listener : listenerMap.get(serverInfo.getType())) {
                listener.notify(serverInfo, serverRequest.getAgentID(), serverRequest.getRequestId());
            }
        }
    }

    Set<ServerListener> listenerList = new HashSet<ServerListener>();
    Map<String, Set<ServerListener>> listenerMap = new HashMap<String, Set<ServerListener>>();

    @Override
    public void subscribe(ServerListener listener) {
        if (listener == null) {
            throw new AgentException("listener cannot be null");
        }
        listenerList.add(listener);
    }

    @Override
    synchronized public void subscribe(ServerListener listener, String serverType) {
        if (serverType == null) {
            throw new AgentException("serverType cannot be null");
        }
        if (listener == null) {
            throw new AgentException("listener cannot be null");
        }
        Set<ServerListener> list = listenerMap.get(serverType);
        if (list == null) {
            list = new HashSet<ServerListener>();
            listenerMap.put(serverType, list);
        }
        list.add(listener);
    }

    @Override
    public void unsubscribe(ServerListener listener) {
        if (listenerList.contains(listener)) {
            listenerList.remove(listener);
        }

    }

    @Override
    public void unsubscribe(ServerListener listener, String serverType) {
        if (listenerMap.containsKey(serverType)){
            Set<ServerListener> list = listenerMap.get(serverType);
            if (list.contains(listener)) {
                list.remove(listener);
            }
        }
    }

}
