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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 连接事件分发中心，维护各地址连接状态并通知 {@link ConnectionListener}。
 * <p>
 * 通过 CAS 更新状态，避免重复触发 connect/disconnect 回调。
 */
public class ConnectionEventsHub {

    /** 节点连接状态枚举。 */
    public enum Status {CONNECTED, DISCONNECTED};

    /** 地址 → 当前连接状态。 */
    private final ConcurrentMap<InetSocketAddress, Status> maps = new ConcurrentHashMap<>();
    /** 监听器 ID → 监听器实例。 */
    private final Map<Integer, ConnectionListener> listenersMap = new ConcurrentHashMap<>();

    /** 注册连接监听器，返回基于 identityHashCode 的 ID。 */
    public int addListener(ConnectionListener listener) {
        int id = System.identityHashCode(listener);
        listenersMap.put(id, listener);
        return id;
    }

    /** 按 ID 移除监听器。 */
    public void removeListener(int listenerId) {
        listenersMap.remove(listenerId);
    }

    /** 触发连接建立事件（状态从断开变为已连接时通知所有监听器）。 */
    public void fireConnect(InetSocketAddress addr, NodeType nodeType) {
        if (maps.get(addr) == Status.CONNECTED) {
            return;
        }

        if (maps.putIfAbsent(addr, Status.CONNECTED) == null
                || maps.replace(addr, Status.DISCONNECTED, Status.CONNECTED)) {
            for (ConnectionListener listener : listenersMap.values()) {
                listener.onConnect(addr, nodeType);
            }
        }
    }

    /** 触发连接断开事件。 */
    public void fireDisconnect(InetSocketAddress addr, NodeType nodeType) {
        if (addr == null || maps.get(addr) == Status.DISCONNECTED) {
            return;
        }

        if (maps.replace(addr, Status.CONNECTED, Status.DISCONNECTED)) {
            for (ConnectionListener listener : listenersMap.values()) {
                listener.onDisconnect(addr, nodeType);
            }
        }
    }


}
