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

import java.nio.channels.SocketChannel;

/**
 * HA 连接接口：主节点侧与单个从节点的同步通道抽象。
 */
public interface HAConnection {
    /** 启动 HA 连接读写线程。 */
    void start();

    /** 关闭 HA 连接及相关资源。 */
    void shutdown();

    /** 关闭底层 Socket 通道。 */
    void close();

    /** 返回底层 SocketChannel。 */
    SocketChannel getSocketChannel();

    /**
     * 获取连接当前状态。
     *
     * @return HAConnectionState
     */
    HAConnectionState getCurrentState();

    /**
     * 获取从节点客户端 IP 地址。
     *
     * @return 客户端 IP
     */
    String getClientAddress();

    /**
     * 获取每秒向该从节点传输的字节数。
     *
     * @return 每秒传输字节
     */
    long getTransferredByteInSecond();

    /**
     * 获取向从节点传输的起始偏移（transferFromWhere）。
     *
     * @return 当前传输起始偏移
     */
    long getTransferFromWhere();

    /**
     * 获取从节点已 ACK 的 CommitLog 偏移。
     *
     * @return 从节点 ACK 偏移
     */
    long getSlaveAckOffset();
}
