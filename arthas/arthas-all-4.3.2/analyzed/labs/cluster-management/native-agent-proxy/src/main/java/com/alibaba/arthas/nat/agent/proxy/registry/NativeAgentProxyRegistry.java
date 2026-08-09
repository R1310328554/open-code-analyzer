package com.alibaba.arthas.nat.agent.proxy.registry;

/**
 * Native Agent Proxy 注册接口，将 Proxy 实例地址写入注册中心供管理端发现。
 *
 * @description: NativeAgentProxyRegistry
 * @author：flzjkl
 * @date: 2024-10-20 10:31
 */
public interface NativeAgentProxyRegistry {

    /**
     * 向注册中心登记 Proxy 服务地址。
     *
     * @param address registry address
     * @param k native agent proxy ip
     * @param v port
     */
    void register(String address, String k, String v);

}
