package com.alibaba.arthas.nat.agent.registry;

/**
 * Native Agent 客户端注册中心接口，便于扩展 etcd、ZooKeeper 等不同实现。
 *
 * @description: Native agent client registry interface, easy to extend to other registry implementations
 * @author：flzjkl
 * @date: 2024-09-15 16:21
 */
public interface NativeAgentRegistry {

    /**
     * 将 Native Agent 的地址信息注册到注册中心。
     *
     * @param address registry address
     * @param k       native agent ip
     * @param v       http port + ws port
     */
    void registerNativeAgent(String address, String k, String v);

}
