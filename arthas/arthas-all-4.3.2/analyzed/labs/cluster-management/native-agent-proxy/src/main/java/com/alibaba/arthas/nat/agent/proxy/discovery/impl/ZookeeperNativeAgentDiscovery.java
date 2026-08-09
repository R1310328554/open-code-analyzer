package com.alibaba.arthas.nat.agent.proxy.discovery.impl;

import com.alibaba.arthas.nat.agent.common.constants.NativeAgentConstants;
import com.alibaba.arthas.nat.agent.proxy.discovery.NativeAgentDiscovery;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 基于 ZooKeeper 的 {@link NativeAgentDiscovery} 实现，枚举 Agent 注册节点并读取各子节点数据。
 *
 * @description: ZookeeperNativeAgentDiscovery implements NativeAgentDiscovery
 * @author：flzjkl
 * @date: 2024-07-24 20:33
 */
public class ZookeeperNativeAgentDiscovery implements NativeAgentDiscovery {

    /** ZooKeeper 会话超时时间（毫秒） */
    private static final int SESSION_TIMEOUT = 20000;
    /** 连接建立前的同步等待信号量 */
    private static final CountDownLatch connectedSemaphore = new CountDownLatch(1);

    @Override
    public Map<String, String> findNativeAgent(String address) {
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

            // 获取 NATIVE_AGENT_KEY 节点下的所有子节点（clientId）
            List<String> children = zooKeeper.getChildren(NativeAgentConstants.NATIVE_AGENT_KEY, false);

            // 逐个读取子节点数据（HTTP/WS 端口信息）
            Map<String, String> res = new ConcurrentHashMap<>(children.size());
            for (String child : children) {
                String childPath = NativeAgentConstants.NATIVE_AGENT_KEY + "/" + child;
                byte[] data = zooKeeper.getData(childPath, false, new Stat());
                String dataStr = new String(data);

                res.put(child, dataStr);
            }

            zooKeeper.close();
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
