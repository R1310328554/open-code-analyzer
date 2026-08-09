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

/**
 * HA 客户端接口：从节点侧连接主节点并同步 CommitLog。
 */
public interface HAClient {

    /** 启动 HA 客户端线程。 */
    void start();

    /** 关闭 HA 客户端。 */
    void shutdown();

    /** 唤醒阻塞中的 HA 客户端。 */
    void wakeup();

    /**
     * 更新主节点业务地址。
     *
     * @param newAddress 新主节点地址
     */
    void updateMasterAddress(String newAddress);

    /**
     * 更新主节点 HA 专用地址。
     *
     * @param newAddress 新 HA 地址
     */
    void updateHaMasterAddress(String newAddress);

    /**
     * 获取主节点业务地址。
     *
     * @return 主节点地址
     */
    String getMasterAddress();

    /**
     * 获取主节点 HA 地址。
     *
     * @return HA 地址
     */
    String getHaMasterAddress();

    /**
     * 获取最近一次读主节点数据的时间戳。
     *
     * @return 最后读时间戳
     */
    long getLastReadTimestamp();

    /**
     * 获取最近一次向主节点写入的时间戳。
     *
     * @return 最后写时间戳
     */
    long getLastWriteTimestamp();

    /**
     * 获取当前 HA 连接状态。
     *
     * @return HAConnectionState
     */
    HAConnectionState getCurrentState();

    /**
     * 测试用：强制修改连接状态。
     *
     * @param haConnectionState 目标状态
     */
    void changeCurrentState(HAConnectionState haConnectionState);

    /** 测试用：断开与主节点的连接。 */
    void closeMaster();

    /**
     * 获取每秒传输字节数。
     *
     * @return 每秒传输字节
     */
    long getTransferredByteInSecond();
}
