package com.taobao.arthas.core.command.model;

/**
 * Session 命令的结果模型，描述 Agent 与 Tunnel 的连接与会话元信息。
 *
 * @author gongdewei 2020/03/27
 */
public class SessionModel extends ResultModel {

    /** 目标 JVM 进程 PID。 */
    private long javaPid;
    /** Arthas 会话 ID。 */
    private String sessionId;
    /** Agent 实例标识。 */
    private String agentId;
    /** Tunnel Server 地址。 */
    private String tunnelServer;
    /** 统计信息 URL。 */
    private String statUrl;
    /** 当前登录用户 ID。 */
    private String userId;

    /** Tunnel 通道是否已连接。 */
    private boolean tunnelConnected;

    @Override
    public String getType() {
        return "session";
    }

    public long getJavaPid() {
        return javaPid;
    }

    public void setJavaPid(long javaPid) {
        this.javaPid = javaPid;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getTunnelServer() {
        return tunnelServer;
    }

    public void setTunnelServer(String tunnelServer) {
        this.tunnelServer = tunnelServer;
    }

    public String getStatUrl() {
        return statUrl;
    }

    public void setStatUrl(String statUrl) {
        this.statUrl = statUrl;
    }

    public boolean isTunnelConnected() {
        return tunnelConnected;
    }

    public void setTunnelConnected(boolean tunnelConnected) {
        this.tunnelConnected = tunnelConnected;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
