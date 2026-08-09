package com.alibaba.arthas.tunnel.server;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Promise;

/**
 * 浏览器侧 WebSocket 连接信息：等待 agent 通过 openTunnel 打通隧道。
 *
 * @author hengyunabc 2019-08-27
 *
 */
public class ClientConnectionInfo {

    /** 浏览器与 tunnel server 之间的 WebSocket 上下文 */
    @JsonIgnore
    private ChannelHandlerContext channelHandlerContext;
    private String host;
    private int port;

    /**
     * 等待 agent 侧 openTunnel 成功后写入 agent 通道
     */
    @JsonIgnore
    private Promise<Channel> promise;

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    public Promise<Channel> getPromise() {
        return promise;
    }

    public void setPromise(Promise<Channel> promise) {
        this.promise = promise;
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
}
