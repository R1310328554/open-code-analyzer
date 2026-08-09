package com.alibaba.arthas.nat.agent.management.web.discovery.impl;

import com.alibaba.arthas.nat.agent.common.constants.NativeAgentConstants;
import com.alibaba.arthas.nat.agent.management.web.discovery.NativeAgentProxyDiscovery;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 基于 ZooKeeper 的 {@link NativeAgentProxyDiscovery} 实现，读取代理注册节点的子节点列表。
 *
 * @description: ZookeeperNativeAgentProxyDiscovery implements NativeAgentProxyDiscovery
 * @author：flzjkl
 * @date: 2024-07-24 20:33
 */
public class ZookeeperNativeAgentProxyDiscovery implements NativeAgentProxyDiscovery {

    /** ZooKeeper 会话超时时间（毫秒） */
    private static final int SESSION_TIMEOUT = 20000;
    /** 连接建立前的同步等待信号量 */
    private static final CountDownLatch connectedSemaphore = new CountDownLatch(1);

    @Override
    public List<String> listNativeAgentProxy(String address) {
        if (address == null || "".equals(address)) {
            return null;
        }

        // 建立连接并等待 SyncConnected 事件
        try {
            ZooKeeper zooKeeper = new ZooKeeper(address, SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectedSemaphore.countDown();
                }
            });
            connectedSemaphore.await();

            // 获取 NATIVE_AGENT_PROXY_KEY 节点下的所有子节点（即代理地址）
            List<String> children = zooKeeper.getChildren(NativeAgentConstants.NATIVE_AGENT_PROXY_KEY, false);
            if (children == null || children.size() == 0) {
                return children;
            }

            zooKeeper.close();
            return children;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
