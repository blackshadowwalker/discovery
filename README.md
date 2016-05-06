# Discovery

## 简介

局域网服务发现，适用于开发环境，方便开发调试。

在开发环境，每次手机客户端需要连接某个人的电脑的时候，都需要告知IP和端口，然后客户端在注释代码，然后编译，然后运行。
使用次组件可以帮助客户端快速发现服务，无须修改代码和重新编译。

## 实现方式

利用组播发布和搜索服务

* 组播地址: `239.125.100.211:50211`  
* TTL: 63 

一般的路由都阻断了跨网组播，所以一般仅适用于局域网。


## 使用方法


### 服务发布服务
 
如使用spring开发的项目，在服务启动的时候启动启动 discovery.Bootstrap 即可

```java
import com.gozap.bean.Version;
import com.longdai.discovery.Bootstrap;
import com.longdai.discovery.beans.ServerInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ServerService {
    private static Log log = LogFactory.getLog(ServerService.class);

    @Autowired
    Version version;

    Bootstrap bootstrap;

    @PostConstruct
    public void init() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setStartTime(System.currentTimeMillis());
        serverInfo.setType("longdai");
        serverInfo.setSubType("API");
        serverInfo.setVersion(version.version);
        serverInfo.setName("龙贷API");
        try {
            bootstrap = new Bootstrap();
            bootstrap.init(serverInfo);
            bootstrap.start();
        } catch (Exception e) {
            log.error("", e);
        }
    }

}
```

### 客户端服务发现


### UDP数据包定义

| 数据包字段 | 类型|  长度(byte)   |     说明  |
|--------|--------|--------------|----------|
|totalLen| int    |  4           | 后面数据总长度，采用大尾字节序(Big Endian) |
| cmdLen | int    |  4           | 命令长度，采用大尾字节序(Big Endian) |
| cmd    | string | 由cmdLen确定  |   命令    |
|agentIdLen| int  |  4           | agentId的长度  |
| agentId  |string| 由agentId确定 | agent实例唯一ID  |
|requestIdLen| int|  4           | 请求id长度   |
|requestId |string| 由requestIdLen确定| 请求id|
|dataLen | int    |  4           | 服务数据长度 |
|data    | string | 由dataLen确定 | 服务数据(serverInfo)Json |


**命令定义**

```
PING,//PING
PONG,//PONG
SEARCH,//搜索请求
ONLINE,//服务上线
OFFLINE,//服务离线
UNKNOWN;//未知
```

**服务描述定义(serverInfo)**

该数据是json格式字符串

```java
class ServerInfo {
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
}
```

如Java代码解析：

```java
ChannelBuffer buf = ChannelBuffers.copiedBuffer(data);
int totalLen = buf.readInt();

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
```

RequestData

```java
class ServerRequest {
    Command command;
    String agentID;
    String requestId;
    ServerInfo data;
}
```

####　示例客户端代码

客户端打开监听组播(239.125.100.211:50211)，然后发送搜索命令(SEARCH)UDP数据包并等待5秒钟基本就可以发现所有服务, 然后将 hostname 和 serverUrl　记录

* hostname 获取的开发服务主机的hostname,用来辨别电脑  
* serverUrl 提供服务的地址, 如: http://10.10.1.220:8080  

如java:

```java
ServerInfo serverInfo = new ServerInfo();
serverInfo.setType("test");
serverInfo.setSubType("search");
serverInfo.setName("AgentMain");

final Bootstrap bootstrap = new Bootstrap();
bootstrap.init(serverInfo);
bootstrap.start();//如果需要搜索也需要启动

List<ServerInfo> list = bootstrap.search("longdai");
log.info("Search:-------------------------");
for (ServerInfo info : list) {
    log.info("ServerInfo: " + info.getSubType() +  " "+ info.getServerUrl() + " at " + info.getHostname());
}
```

