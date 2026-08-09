package com.alibaba.arthas.nat.agent.proxy.registry.impl;

import com.alibaba.arthas.nat.agent.proxy.registry.NativeAgentProxyRegistry;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于 ZooKeeper 的 {@link NativeAgentProxyRegistry} 实现，以临时节点注册 Proxy 地址。
 *
 * @description: Zookeeper native agent proxy register implements NativeAgentProxyRegistry
 * @author：flzjkl
 * @date: 2024-10-20 18:21
 */
public class ZookeeperNativeAgentProxyRegistry implements NativeAgentProxyRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperNativeAgentProxyRegistry.class);
    private static CountDownLatch latch = new CountDownLatch(1);
    private static final int SESSION_TIMEOUT = 15000;
    private static final String NATIVE_AGENT_PROXY_KEY = "/native-agent-proxy";


    @Override
    public void register(String address, String k, String v) {
        // 创建 ZooKeeper 客户端并等待 SyncConnected
        ZooKeeper zk = null;
        AtomicBoolean createResult = new AtomicBoolean(false);
        try {
            zk = new ZooKeeper(address, SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                    createResult.compareAndSet(false, true);
                }
            });
            latch.await();
        } catch (Exception e) {
            logger.error("Create zookeeper client failed");
            throw new RuntimeException(e);
        } finally {
            latch.countDown();
        }

        if (!createResult.get()) {
            throw new RuntimeException("Create zookeeper client failed");
        }

        try {
            // 父节点不存在时先创建持久节点
            if (zk.exists(NATIVE_AGENT_PROXY_KEY, false) == null) {
                zk.create(NATIVE_AGENT_PROXY_KEY, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            // 以 EPHEMERAL 模式创建子节点，会话结束自动删除
            String path = zk.create(NATIVE_AGENT_PROXY_KEY + "/" + k, v.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("native agent proxy registered at: " + path);
        } catch (KeeperException | InterruptedException e) {
            logger.error("Register native agent proxy failed");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
