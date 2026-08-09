/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.connection;

import org.redisson.api.NodeType;

import java.net.InetSocketAddress;

/**
 * Redis 连接生命周期监听器。
 * <p>
 * 实现 {@link #onConnect(InetSocketAddress, NodeType)} 与
 * {@link #onDisconnect(InetSocketAddress, NodeType)} 以接收连接/断开通知。
 *
 * @author Nikita Koksharov
 *
 */
public interface ConnectionListener {

    /*
     * 请实现 {@link #onConnect(InetSocketAddress, NodeType)} 替代此方法。
     * 实现可为空。
     */
    @Deprecated
    void onConnect(InetSocketAddress addr);

    /**
     * Redisson 成功连接到 Redis 服务器时触发。
     *
     * @param addr Redis 服务器网络地址
     * @param nodeType 节点类型（主/从/哨兵等）
     */
    default void onConnect(InetSocketAddress addr, NodeType nodeType) {
        onConnect(addr);
    }

    /*
     * 请实现 {@link #onDisconnect(InetSocketAddress, NodeType)} 替代此方法。
     * 实现可为空。
     */
    @Deprecated
    void onDisconnect(InetSocketAddress addr);

    /**
     * Redisson 检测到 Redis 服务器断开连接时触发。
     *
     * @param addr Redis 服务器网络地址
     * @param nodeType 节点类型
     */
    default void onDisconnect(InetSocketAddress addr, NodeType nodeType) {
        onDisconnect(addr);
    }

}
