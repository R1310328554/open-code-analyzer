package com.alibaba.arthas.nat.agent.management.web.discovery;

import java.util.List;

/**
 * Native Agent Proxy 服务发现接口，从注册中心查询可用的代理节点地址。
 *
 * @description: NativeAgentProyDiscovery
 * @author：flzjkl
 * @date: 2024-09-19 7:22
 */
public interface NativeAgentProxyDiscovery {

    /**
     * 列出注册中心中所有 Native Agent Proxy 的访问地址。
     *
     * @param address register address
     * @return native agent proxy address
     */
    List<String> listNativeAgentProxy(String address);
}
