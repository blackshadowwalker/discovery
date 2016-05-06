package com.longdai.discovery.beans;

import com.longdai.discovery.util.DateUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by karl on 2016/5/5.
 */
public class ServerInfo implements Serializable{
    private static long serialVersionUID = -1L;

    String type;//服务类型
    String subType;//服务子类型
    String name;//服务名称
    String version;

    String hostname;
    String ip;
    Integer port;
    String serverUrl;
    Long   timestamp;
    Long   startTime;
    String startTimeString;

    public String getStartTimeString() {
        if (startTimeString==null && startTime != null ) {
            startTimeString = DateUtils.yyyy_MM_dd_HH_mm_ss.format(new Date(startTime));
        }
        return startTimeString;
    }

    public Long getTimestamp() {
        if (timestamp==null) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerInfo{");
        sb.append("type='").append(type).append('\'');
        sb.append(", subType='").append(subType).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", port=").append(port);
        sb.append(", serverUrl='").append(serverUrl).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", startTime=").append(startTime);
        sb.append(", startTimeString='").append(startTimeString).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
}
