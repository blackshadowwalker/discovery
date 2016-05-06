package com.longdai.discovery;

import com.longdai.discovery.beans.ServerInfo;
import com.longdai.discovery.beans.ServerResponse;
import com.longdai.discovery.em.Command;
import com.longdai.discovery.udp.UDPAgent;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Created by karl on 2016/5/6.
 */
public class TestMain {

    public static void main(String[] args) throws Exception {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setName("testMain");
        ServerResponse response = new ServerResponse(Command.ONLINE, UUID.randomUUID().toString(), UUID.randomUUID().toString(), serverInfo);
        byte[] sendData = UDPAgent.buildResponseBuffer(response);
        InetAddress local = InetAddress.getLocalHost();
        System.out.println(local);
        final DatagramPacket pack = new DatagramPacket(sendData, sendData.length, local, Agent.PORT);
        final DatagramSocket socket = new DatagramSocket();

        new Thread(){
            @Override
            public void run() {
                while(true) {
                    try {
                        socket.send(pack);
                        Thread.sleep(1000*5);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();


    }

}
