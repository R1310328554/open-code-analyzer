/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.store.ha;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.utils.NetworkUtil;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.protocol.body.HARuntimeInfo;
import org.apache.rocketmq.store.CommitLog;
import org.apache.rocketmq.store.DefaultMessageStore;
import org.apache.rocketmq.store.config.BrokerRole;
import org.apache.rocketmq.store.config.MessageStoreConfig;

/**
 * 默认主从同步（HA）服务实现：管理 Accept 监听、GroupTransfer 与 HAClient。
 */
public class DefaultHAService implements HAService {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    /** 当前 HA 连接数量。 */
    protected final AtomicInteger connectionCount = new AtomicInteger(0);

    /** 活跃 HA 连接列表。 */
    protected final List<HAConnection> connectionList = new LinkedList<>();

    /** 接受从节点连接的 Socket 服务。 */
    protected AcceptSocketService acceptSocketService;

    /** 所属 MessageStore 实例。 */
    protected DefaultMessageStore defaultMessageStore;

    /** 组提交等待/唤醒对象。 */
    protected WaitNotifyObject waitNotifyObject = new WaitNotifyObject();
    /** 已推送到从节点的最大 CommitLog 偏移。 */
    protected AtomicLong push2SlaveMaxOffset = new AtomicLong(0);

    /** 组传输服务，等待从节点 ACK。 */
    protected GroupTransferService groupTransferService;

    /** 从节点侧 HA 客户端（主节点为 null）。 */
    protected HAClient haClient;

    /** HA 连接状态变更通知服务。 */
    protected HAConnectionStateNotificationService haConnectionStateNotificationService;

    /** 默认构造。 */
    public DefaultHAService() {
    }

    /** 初始化 HA 组件，须在其它方法之前调用。 */
    @Override
    public void init(final DefaultMessageStore defaultMessageStore) throws IOException {
        this.defaultMessageStore = defaultMessageStore;
        this.acceptSocketService = new DefaultAcceptSocketService(defaultMessageStore.getMessageStoreConfig());
        this.groupTransferService = new GroupTransferService(this, defaultMessageStore);
        if (this.defaultMessageStore.getMessageStoreConfig().getBrokerRole() == BrokerRole.SLAVE) {
            this.haClient = new DefaultHAClient(this.defaultMessageStore);
        }
        this.haConnectionStateNotificationService = new HAConnectionStateNotificationService(this, defaultMessageStore);
    }

    /** 更新主节点业务地址。 */
    @Override
    public void updateMasterAddress(final String newAddr) {
        if (this.haClient != null) {
            this.haClient.updateMasterAddress(newAddr);
        }
    }

    /** 更新主节点 HA 专用地址。 */
    @Override
    public void updateHaMasterAddress(String newAddr) {
        if (this.haClient != null) {
            this.haClient.updateHaMasterAddress(newAddr);
        }
    }

    /** 提交组提交请求至 GroupTransferService。 */
    @Override
    public void putRequest(final CommitLog.GroupCommitRequest request) {
        this.groupTransferService.putRequest(request);
    }

    /** 判断从节点是否跟得上主节点写入进度。 */
    @Override
    public boolean isSlaveOK(final long masterPutWhere) {
        boolean result = this.connectionCount.get() > 0;
        result =
            result
                && masterPutWhere - this.push2SlaveMaxOffset.get() < this.defaultMessageStore
                .getMessageStoreConfig().getHaMaxGapNotInSync();
        return result;
    }

    /** 更新 push2SlaveMaxOffset 并唤醒传输等待。 */
    public void notifyTransferSome(final long offset) {
        for (long value = this.push2SlaveMaxOffset.get(); offset > value; ) {
            boolean ok = this.push2SlaveMaxOffset.compareAndSet(value, offset);
            if (ok) {
                this.groupTransferService.notifyTransferSome();
                break;
            } else {
                value = this.push2SlaveMaxOffset.get();
            }
        }
    }

    /** 返回当前 HA 连接数。 */
    @Override
    public AtomicInteger getConnectionCount() {
        return connectionCount;
    }

    /** 启动 Accept、GroupTransfer、状态通知及 HAClient。 */
    @Override
    public void start() throws Exception {
        this.acceptSocketService.beginAccept();
        this.acceptSocketService.start();
        this.groupTransferService.start();
        this.haConnectionStateNotificationService.start();
        if (haClient != null) {
            this.haClient.start();
        }
    }

    /** 将新 HA 连接加入列表。 */
    public void addConnection(final HAConnection conn) {
        synchronized (this.connectionList) {
            this.connectionList.add(conn);
        }
    }

    /** 移除连接并触发状态通知检查。 */
    public void removeConnection(final HAConnection conn) {
        this.haConnectionStateNotificationService.checkConnectionStateAndNotify(conn);
        synchronized (this.connectionList) {
            this.connectionList.remove(conn);
        }
    }

    /** 关闭 HA 全部子服务与连接。 */
    @Override
    public void shutdown() {
        if (this.haClient != null) {
            this.haClient.shutdown();
        }
        if (this.acceptSocketService != null) {
            this.acceptSocketService.shutdown(true);
        }
        this.destroyConnections();
        if (this.groupTransferService != null) {
            groupTransferService.shutdown();
        }

        if (this.haConnectionStateNotificationService != null) {
            this.haConnectionStateNotificationService.shutdown();
        }
    }

    /** 关闭并清空所有 HA 连接。 */
    public void destroyConnections() {
        synchronized (this.connectionList) {
            for (HAConnection c : this.connectionList) {
                c.shutdown();
            }

            this.connectionList.clear();
        }
    }

    /** 返回关联的 MessageStore。 */
    public DefaultMessageStore getDefaultMessageStore() {
        return defaultMessageStore;
    }

    /** 返回等待/唤醒对象。 */
    @Override
    public WaitNotifyObject getWaitNotifyObject() {
        return waitNotifyObject;
    }

    /** 返回已推送至从节点的最大偏移。 */
    @Override
    public AtomicLong getPush2SlaveMaxOffset() {
        return push2SlaveMaxOffset;
    }

    /** 统计与主节点同步的副本数量（含主）。 */
    @Override
    public int inSyncReplicasNums(final long masterPutWhere) {
        int inSyncNums = 1;
        for (HAConnection conn : this.connectionList) {
            if (this.isInSyncSlave(masterPutWhere, conn)) {
                inSyncNums++;
            }
        }
        return inSyncNums;
    }

    /** 判断单个从连接是否在同步阈值内。 */
    protected boolean isInSyncSlave(final long masterPutWhere, HAConnection conn) {
        if (masterPutWhere - conn.getSlaveAckOffset() < this.defaultMessageStore.getMessageStoreConfig()
            .getHaMaxGapNotInSync()) {
            return true;
        }
        return false;
    }

    /** 注册 HA 连接状态等待请求。 */
    @Override
    public void putGroupConnectionStateRequest(HAConnectionStateNotificationRequest request) {
        this.haConnectionStateNotificationService.setRequest(request);
    }

    /** 返回 HA 连接列表。 */
    @Override
    public List<HAConnection> getConnectionList() {
        return connectionList;
    }

    /** 返回 HA 客户端（从节点）。 */
    @Override
    public HAClient getHAClient() {
        return this.haClient;
    }

    /** 收集主/从 HA 运行时指标。 */
    @Override
    public HARuntimeInfo getRuntimeInfo(long masterPutWhere) {
        HARuntimeInfo info = new HARuntimeInfo();

        if (BrokerRole.SLAVE.equals(this.getDefaultMessageStore().getMessageStoreConfig().getBrokerRole())) {
            info.setMaster(false);

            info.getHaClientRuntimeInfo().setMasterAddr(this.haClient.getHaMasterAddress());
            info.getHaClientRuntimeInfo().setMaxOffset(this.getDefaultMessageStore().getMaxPhyOffset());
            info.getHaClientRuntimeInfo().setLastReadTimestamp(this.haClient.getLastReadTimestamp());
            info.getHaClientRuntimeInfo().setLastWriteTimestamp(this.haClient.getLastWriteTimestamp());
            info.getHaClientRuntimeInfo().setTransferredByteInSecond(this.haClient.getTransferredByteInSecond());
            info.getHaClientRuntimeInfo().setMasterFlushOffset(this.defaultMessageStore.getMasterFlushedOffset());
        } else {
            info.setMaster(true);
            int inSyncNums = 0;

            info.setMasterCommitLogMaxOffset(masterPutWhere);

            for (HAConnection conn : this.connectionList) {
                HARuntimeInfo.HAConnectionRuntimeInfo cInfo = new HARuntimeInfo.HAConnectionRuntimeInfo();

                long slaveAckOffset = conn.getSlaveAckOffset();
                cInfo.setSlaveAckOffset(slaveAckOffset);
                cInfo.setDiff(masterPutWhere - slaveAckOffset);
                cInfo.setAddr(conn.getClientAddress().substring(1));
                cInfo.setTransferredByteInSecond(conn.getTransferredByteInSecond());
                cInfo.setTransferFromWhere(conn.getTransferFromWhere());

                boolean isInSync = this.isInSyncSlave(masterPutWhere, conn);
                if (isInSync) {
                    inSyncNums++;
                }
                cInfo.setInSync(isInSync);

                info.getHaConnectionInfo().add(cInfo);
            }
            info.setInSyncSlaveNums(inSyncNums);
        }
        return info;
    }

    /** 默认 Accept 实现，创建 DefaultHAConnection。 */
    class DefaultAcceptSocketService extends AcceptSocketService {

        public DefaultAcceptSocketService(final MessageStoreConfig messageStoreConfig) {
            super(messageStoreConfig);
        }

        /** 为新 Socket 创建 DefaultHAConnection。 */
        @Override
        protected HAConnection createConnection(SocketChannel sc) throws IOException {
            return new DefaultHAConnection(DefaultHAService.this, sc);
        }

        @Override
        public String getServiceName() {
            if (defaultMessageStore.getBrokerConfig().isInBrokerContainer()) {
                return defaultMessageStore.getBrokerConfig().getIdentifier() + AcceptSocketService.class.getSimpleName();
            }
            return DefaultAcceptSocketService.class.getSimpleName();
        }
    }

    /**
     * 监听从节点连接并创建 {@link HAConnection}。
     */
    protected abstract class AcceptSocketService extends ServiceThread {
        /** 监听绑定地址。 */
        private final SocketAddress socketAddressListen;
        /** 服务端 Socket 通道。 */
        private ServerSocketChannel serverSocketChannel;
        /** NIO 选择器。 */
        private Selector selector;

        private final MessageStoreConfig messageStoreConfig;

        public AcceptSocketService(final MessageStoreConfig messageStoreConfig) {
            this.messageStoreConfig = messageStoreConfig;
            this.socketAddressListen = new InetSocketAddress(messageStoreConfig.getHaListenPort());
        }

        /**
         * 开始在 HA 端口监听从节点连接。
         *
         * @throws Exception 绑定或注册失败时抛出
         */
        public void beginAccept() throws Exception {
            this.serverSocketChannel = ServerSocketChannel.open();
            this.selector = NetworkUtil.openSelector();
            this.serverSocketChannel.socket().setReuseAddress(true);
            this.serverSocketChannel.socket().bind(this.socketAddressListen);
            if (0 == messageStoreConfig.getHaListenPort()) {
                messageStoreConfig.setHaListenPort(this.serverSocketChannel.socket().getLocalPort());
                log.info("OS picked up {} to listen for HA", messageStoreConfig.getHaListenPort());  // 操作系统自动分配 HA 监听端口
            }
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void shutdown(final boolean interrupt) {
            super.shutdown(interrupt);
            try {
                if (null != this.serverSocketChannel) {
                    this.serverSocketChannel.close();
                }

                if (null != this.selector) {
                    this.selector.close();
                }
            } catch (IOException e) {
                log.error("AcceptSocketService shutdown exception", e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            log.info(this.getServiceName() + " service started");

            while (!this.isStopped()) {
                try {
                    this.selector.select(1000);
                    Set<SelectionKey> selected = this.selector.selectedKeys();

                    if (selected != null) {
                        for (SelectionKey k : selected) {
                            if (k.isAcceptable()) {
                                SocketChannel sc = ((ServerSocketChannel) k.channel()).accept();

                                if (sc != null) {
                                    DefaultHAService.log.info("HAService receive new connection, "
                                        + sc.socket().getRemoteSocketAddress());
                                    try {
                                        HAConnection conn = createConnection(sc);
                                        DefaultHAService.this.addConnection(conn);
                                        conn.start();
                                    } catch (Exception e) {
                                        log.error("new HAConnection exception", e);
                                        sc.close();
                                    }
                                }
                            } else {
                                log.warn("Unexpected ops in select " + k.readyOps());
                            }
                        }

                        selected.clear();
                    }
                } catch (Exception e) {
                    log.error(this.getServiceName() + " service has exception.", e);
                }
            }

            log.info(this.getServiceName() + " service end");
        }

        /**
         * 为接受的 Socket 创建 HA 连接实例
         */
        protected abstract HAConnection createConnection(final SocketChannel sc) throws IOException;
    }
}
