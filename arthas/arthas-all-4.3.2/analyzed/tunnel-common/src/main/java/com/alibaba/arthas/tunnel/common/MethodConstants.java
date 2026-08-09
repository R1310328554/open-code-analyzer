package com.alibaba.arthas.tunnel.common;

/**
 * tunnel client 与 server 通过 URI 通信，query 参数 method 定义各类交互行为
 * 
 * @author hengyunabc 2020-10-22
 *
 */
public class MethodConstants {

    /**
     * 
     * <pre>
     * tunnel client 启动时注册的 method
     * 
     * ws://192.168.1.10:7777/ws?method=agentRegister
     * 
     * tunnel server 回应：
     * 
     * response:/?method=agentRegister&id=bvDOe8XbTM2pQWjF4cfw
     * 
     * 未指定 id 时由服务端随机生成
     * </pre>
     */
    public static final String AGENT_REGISTER = "agentRegister";

    /**
     * <pre>
     * tunnel server 通知 tunnel client 再建 WebSocket 用于浏览器与 agent 中继
     * 
     * response:/?method=startTunnel&id=bvDOe8XbTM2pQWjF4cfw&clientConnectionId=AMku9EFz2gxeL2gedGOC
     * </pre>
     */
    public static final String START_TUNNEL = "startTunnel";
    /**
     * <pre>
     * 浏览器通知 tunnel server 连接指定 id 的 arthas agent
     * 
     * ws://192.168.1.10:7777/ws?method=connectArthas&id=bvDOe8XbTM2pQWjF4cfw
     * </pre>
     */
    public static final String CONNECT_ARTHAS = "connectArthas";

    /**
     * <pre>
     * tunnel client 收到 startTunnel 后，以如下 URI 新建连接并完成隧道打通：
     * 
     * ws://127.0.0.1:7777/ws/?method=openTunnel&clientConnectionId=AMku9EFz2gxeL2gedGOC&id=bvDOe8XbTM2pQWjF4cfw
     * </pre>
     */
    public static final String OPEN_TUNNEL = "openTunnel";
    
    /**
     * <pre>
     * tunnel server 向 tunnel client 发起 HTTP 中转，例如访问 http://localhost:3658/arthas-output/xxx.html
     * </pre>
     */
    public static final String HTTP_PROXY = "httpProxy";

}
