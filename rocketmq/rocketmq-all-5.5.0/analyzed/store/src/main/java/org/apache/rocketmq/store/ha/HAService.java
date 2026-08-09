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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.remoting.protocol.body.HARuntimeInfo;
import org.apache.rocketmq.store.CommitLog;
import org.apache.rocketmq.store.DefaultMessageStore;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.rocksdb.RocksDBException;

/**
 * 主从同步（HA）服务接口：管理 CommitLog 复制、连接与角色切换。
 */
public interface HAService {

    /**
     * 初始化 HA 服务，须在其它方法之前调用。
     *
     * @param defaultMessageStore MessageStore 实例
     * @throws IOException 初始化 IO 异常
     */
    void init(DefaultMessageStore defaultMessageStore) throws IOException;

    /**
     * 启动 HA 服务。
     *
     * @throws Exception 启动失败
     */
    void start() throws Exception;

    /** 关闭 HA 服务。 */
    void shutdown();

    /**
     * 切换为主节点。
     *
     * @param masterEpoch 新主 epoch
     */
    default boolean changeToMaster(int masterEpoch) throws RocksDBException {
        return false;
    }

    /**
     * Change to master state
     *
     * @param masterEpoch the new masterEpoch
     */
    default boolean changeToMasterWhenLastRoleIsMaster(int masterEpoch) {
        return false;
    }

    /**
     * 切换为从节点。
     *
     * @param newMasterAddr 新主地址
     * @param newMasterEpoch 新主 epoch
     */
    default boolean changeToSlave(String newMasterAddr, int newMasterEpoch, Long slaveId) {
        return false;
    }

    /**
     * Change to slave state
     *
     * @param newMasterAddr new master addr
     * @param newMasterEpoch new masterEpoch
     */
    default boolean changeToSlaveWhenMasterNotChange(String newMasterAddr, int newMasterEpoch) {
        return false;
    }

    /**
     * 更新主节点业务地址。
     *
     * @param newAddr 新地址
     */
    void updateMasterAddress(String newAddr);

    /**
     * 更新 HA 专用主地址。
     *
     * @param newAddr 新 HA 地址
     */
    void updateHaMasterAddress(String newAddr);

    /**
     * 返回 CommitLog 未明显落后的副本数（含主）。AutoSwitch 模式下等价于 syncStateSet 大小。
     *
     * @return 同步副本数量
     * @see MessageStoreConfig#getHaMaxGapNotInSync()
     */
    int inSyncReplicasNums(long masterPutWhere);

    /**
     * 获取 HA 连接数。
     *
     * @return 连接数量
     */
    AtomicInteger getConnectionCount();

    /**
     * 提交组提交请求由 HA 处理。
     *
     * @param request 组提交请求
     */
    void putRequest(final CommitLog.GroupCommitRequest request);

    /**
     * 注册 preOnline 用的连接状态等待请求。
     *
     * @param request 状态通知请求
     */
    void putGroupConnectionStateRequest(HAConnectionStateNotificationRequest request);

    /**
     * 获取 HA 连接列表。
     *
     * @return HAConnection 列表
     */
    List<HAConnection> getConnectionList();

    /**
     * 获取 HA 客户端（从节点）。
     *
     * @return HAClient
     */
    HAClient getHAClient();

    /** 获取所有从节点中的最大已推送偏移。 */
    AtomicLong getPush2SlaveMaxOffset();

    /** 收集 HA 运行时信息。 */
    HARuntimeInfo getRuntimeInfo(final long masterPutWhere);

    /** 返回等待/唤醒对象。 */
    WaitNotifyObject getWaitNotifyObject();

    /**
     * 根据 masterPutWhere 判断从节点是否跟上；偏移差超过阈值则返回 false。
     */
    boolean isSlaveOK(long masterPutWhere);
}
