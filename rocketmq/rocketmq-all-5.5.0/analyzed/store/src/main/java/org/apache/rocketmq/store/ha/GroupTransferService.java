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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.CommitLog;
import org.apache.rocketmq.store.DefaultMessageStore;
import org.apache.rocketmq.store.PutMessageSpinLock;
import org.apache.rocketmq.store.PutMessageStatus;
import org.apache.rocketmq.store.ha.autoswitch.AutoSwitchHAConnection;
import org.apache.rocketmq.store.ha.autoswitch.AutoSwitchHAService;

/**
 * 组传输服务：等待从节点 ACK 指定偏移后唤醒组提交请求。
 */
public class GroupTransferService extends ServiceThread {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    /** 传输进度通知用的等待对象。 */
    private final WaitNotifyObject notifyTransferObject = new WaitNotifyObject();
    /** 保护请求双缓冲交换的自旋锁。 */
    private final PutMessageSpinLock lock = new PutMessageSpinLock();
    /** 所属 MessageStore。 */
    private final DefaultMessageStore defaultMessageStore;
    /** 关联的 HA 服务。 */
    private final HAService haService;
    /** 写入侧待处理组提交请求列表。 */
    private volatile List<CommitLog.GroupCommitRequest> requestsWrite = new LinkedList<>();
    /** 读取侧正在处理的请求列表。 */
    private volatile List<CommitLog.GroupCommitRequest> requestsRead = new LinkedList<>();

    /** 构造并绑定 HA 与 MessageStore。 */
    public GroupTransferService(final HAService haService, final DefaultMessageStore defaultMessageStore) {
        this.haService = haService;
        this.defaultMessageStore = defaultMessageStore;
    }

    /** 追加组提交请求并唤醒服务线程。 */
    public void putRequest(final CommitLog.GroupCommitRequest request) {
        lock.lock();
        try {
            this.requestsWrite.add(request);
        } finally {
            lock.unlock();
        }
        wakeup();
    }

    /** 通知有新的传输进度，唤醒等待。 */
    public void notifyTransferSome() {
        this.notifyTransferObject.wakeup();
    }

    /** 交换读写请求缓冲区。 */
    private void swapRequests() {
        lock.lock();
        try {
            List<CommitLog.GroupCommitRequest> tmp = this.requestsWrite;
            this.requestsWrite = this.requestsRead;
            this.requestsRead = tmp;
        } finally {
            lock.unlock();
        }
    }

    /** 轮询等待从节点 ACK 直至超时或满足 ack 数。 */
    private void doWaitTransfer() {
        if (!this.requestsRead.isEmpty()) {
            for (CommitLog.GroupCommitRequest req : this.requestsRead) {
                boolean transferOK = false;

                long deadLine = req.getDeadLine();
                final boolean allAckInSyncStateSet = req.getAckNums() == MixAll.ALL_ACK_IN_SYNC_STATE_SET;

                for (int i = 0; !transferOK && deadLine - System.nanoTime() > 0; i++) {
                    if (i > 0) {
                        this.notifyTransferObject.waitForRunning(1);
                    }

                    if (!allAckInSyncStateSet && req.getAckNums() <= 1) {
                        transferOK = haService.getPush2SlaveMaxOffset().get() >= req.getNextOffset();
                        continue;
                    }

                    if (allAckInSyncStateSet && this.haService instanceof AutoSwitchHAService) {
                        // 此模式下须等待 SyncStateSet 内全部副本 ACK，
                        final AutoSwitchHAService autoSwitchHAService = (AutoSwitchHAService) this.haService;
                        final Set<Long> syncStateSet = autoSwitchHAService.getSyncStateSet();
                        if (syncStateSet.size() <= 1) {
                            // 仅主节点时直接成功
                            transferOK = true;
                            break;
                        }

                        // 计数含主节点本身
                        int ackNums = 1;
                        for (HAConnection conn : haService.getConnectionList()) {
                            final AutoSwitchHAConnection autoSwitchHAConnection = (AutoSwitchHAConnection) conn;
                            if (syncStateSet.contains(autoSwitchHAConnection.getSlaveId()) && autoSwitchHAConnection.getSlaveAckOffset() >= req.getNextOffset()) {
                                ackNums++;
                            }
                            if (ackNums >= syncStateSet.size()) {
                                transferOK = true;
                                break;
                            }
                        }
                    } else {
                        // Include master
                        int ackNums = 1;
                        for (HAConnection conn : haService.getConnectionList()) {
                            // TODO: 须确保每条 HAConnection 对应不同从节点
                            // 方案：为每个从节点分配唯一固定 IP:PORT
                            if (conn.getSlaveAckOffset() >= req.getNextOffset()) {
                                ackNums++;
                            }
                            if (ackNums >= req.getAckNums()) {
                                transferOK = true;
                                break;
                            }
                        }
                    }
                }

                if (!transferOK) {
                    log.warn("transfer message to slave timeout, offset : {}, request acks: {}",
                        req.getNextOffset(), req.getAckNums());
                }

                req.wakeupCustomer(transferOK ? PutMessageStatus.PUT_OK : PutMessageStatus.FLUSH_SLAVE_TIMEOUT);
            }

            this.requestsRead = new LinkedList<>();
        }
    }

    /** 主循环：周期性处理传输等待。 */
    @Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        while (!this.isStopped()) {
            try {
                this.waitForRunning(10);
                this.doWaitTransfer();
            } catch (Exception e) {
                log.warn(this.getServiceName() + " service has exception. ", e);
            }
        }

        log.info(this.getServiceName() + " service end");
    }

    /** 等待结束时交换请求缓冲区。 */
    @Override
    protected void onWaitEnd() {
        this.swapRequests();
    }

    /** 返回服务名称（容器模式下带 Broker 标识）。 */
    @Override
    public String getServiceName() {
        if (defaultMessageStore != null && defaultMessageStore.getBrokerConfig().isInBrokerContainer()) {
            return defaultMessageStore.getBrokerIdentity().getIdentifier() + GroupTransferService.class.getSimpleName();
        }
        return GroupTransferService.class.getSimpleName();
    }
}
