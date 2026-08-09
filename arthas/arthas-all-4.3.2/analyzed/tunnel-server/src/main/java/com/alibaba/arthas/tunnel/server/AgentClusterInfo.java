package com.alibaba.arthas.tunnel.server;

/**
 * 集群场景下 agent 注册快照：记录 agent 地址及其所连 tunnel server 入口。
 *
 * @author hengyunabc 2020-10-30
 *
 */
public class AgentClusterInfo {
    /**
     * agent 进程所在主机 IP（或经代理解析后的客户端 IP）
     */
    private String host;
    private int port;
    private String arthasVersion;

    /**
     * 浏览器/agent 应连接的 tunnel server 对外 host 与端口
     */
    private String clientConnectHost;
    private int clientConnectTunnelPort;

    public AgentClusterInfo() {

    }

        /** 从在线 {@link AgentInfo} 与集群入口构造可持久化视图 */
    public AgentClusterInfo(AgentInfo agentInfo, String clientConnectHost, int clientConnectTunnelPort) {
        this.host = agentInfo.getHost();
        this.port = agentInfo.getPort();
        this.arthasVersion = agentInfo.getArthasVersion();
        this.clientConnectHost = clientConnectHost;
        this.clientConnectTunnelPort = clientConnectTunnelPort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getArthasVersion() {
        return arthasVersion;
    }

    public void setArthasVersion(String arthasVersion) {
        this.arthasVersion = arthasVersion;
    }

    public String getClientConnectHost() {
        return clientConnectHost;
    }

    public void setClientConnectHost(String clientConnectHost) {
        this.clientConnectHost = clientConnectHost;
    }

    public int getClientConnectTunnelPort() {
        return clientConnectTunnelPort;
    }

    public void setClientConnectTunnelPort(int clientConnectTunnelPort) {
        this.clientConnectTunnelPort = clientConnectTunnelPort;
    }

}
