package com.alibaba.arthas.tunnel.server;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.netty.channel.ChannelHandlerContext;

/**
 * 已注册 Arthas agent 的运行时信息：Netty 通道、主机地址与版本等。
 *
 * @author hengyunabc 2019-08-27
 *
 */
public class AgentInfo {

    /** 与 agent 的长连接上下文，不参与 JSON 序列化 */
    @JsonIgnore
    private ChannelHandlerContext channelHandlerContext;
    /** agent 所在主机 IP 或经反向代理解析后的客户端 IP */
    private String host;
    private int port;
        /** agent 上报的 Arthas 版本 */
    private String arthasVersion;

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
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

}
