package com.longdai.discovery.em;

/**
 * Created by karl on 2016/5/5.
 */
public enum Command {

    PING,//PING
    PONG,//PONG
    SEARCH,//搜索请求
    ONLINE,//服务上线
    OFFLINE,//服务离线
    UNKNOWN;//未知

    static
    public Command parse(String cmd) {
        try {
            return Command.valueOf(cmd);
        }catch (Exception e){}
        return Command.UNKNOWN;
    }

}
