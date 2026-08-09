package com.alibaba.arthas.nat.agent.registry.impl;

import com.alibaba.arthas.nat.agent.common.constants.NativeAgentConstants;
import com.alibaba.arthas.nat.agent.registry.NativeAgentRegistry;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于 ZooKeeper 的 Native Agent 注册实现，使用临时节点表示 Agent 在线状态。
 *
 * @description: Zookeeper native agent client register implements NativeAgentRegistry
 * @author：flzjkl
 * @date: 2024-07-24 0:01
 */
public class ZookeeperNativeAgentRegistry implements NativeAgentRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperNativeAgentRegistry.class);
    /** 等待 ZK 会话建立完成的闭锁 */
    private static CountDownLatch latch = new CountDownLatch(1);
    /** 会话超时时间（毫秒） */
    private static final int SESSION_TIMEOUT = 15000;
    public void registerNativeAgent(String address, String k, String v) {
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
            // 若父节点不存在则先创建持久父节点
            if (zk.exists(NativeAgentConstants.NATIVE_AGENT_KEY, false) == null) {
                zk.create(NativeAgentConstants.NATIVE_AGENT_KEY, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            // 以 EPHEMERAL 模式创建子节点，会话结束时会自动删除
            String path = zk.create(NativeAgentConstants.NATIVE_AGENT_KEY + "/" + k, v.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("native agent client registered at: " + path);
        } catch (KeeperException | InterruptedException e) {
            logger.error("Register native agent client failed");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
