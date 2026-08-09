package com.alibaba.arthas.nat.agent.common.dto;

/**
 * Native Agent 节点信息传输对象，用于注册发现与代理路由。
 *
 * <p>包含 Agent 所在主机 IP、HTTP 管理端口与 WebSocket 端口。</p>
 *
 * @description: NativeAgentInfoDTO
 * @author：flzjkl
 * @date: 2024-09-05 8:04
 */
public class NativeAgentInfoDTO {
    /** Agent 进程所在机器 IP */
    private String ip;
    /** HTTP 服务端口 */
    private Integer httpPort;
    /** WebSocket 隧道端口 */
    private Integer wsPort;

    public NativeAgentInfoDTO() {

    }

    public NativeAgentInfoDTO(String ip, Integer httpPort, Integer wsPort) {
        this.ip = ip;
        this.httpPort = httpPort;
        this.wsPort = wsPort;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getWsPort() {
        return wsPort;
    }

    public void setWsPort(Integer wsPort) {
        this.wsPort = wsPort;
    }
}
