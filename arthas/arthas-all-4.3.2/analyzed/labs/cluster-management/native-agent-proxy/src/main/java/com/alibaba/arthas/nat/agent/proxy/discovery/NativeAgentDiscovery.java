package com.alibaba.arthas.nat.agent.proxy.discovery;

import java.util.Map;

/**
 * Native Agent 服务发现接口，从注册中心查询已注册的 Agent 客户端及其端口信息。
 *
 * @description: NativeAgentDiscovery
 * @author：flzjkl
 * @date: 2024-09-19 7:22
 */
public interface NativeAgentDiscovery {

    /**
     * 根据注册中心地址查找所有 Native Agent 客户端。
     *
     * @param address register address
     * @return Map<String, String> k: native agent client id ,v: http port + ws port
     */
    Map<String, String> findNativeAgent(String address);

}
