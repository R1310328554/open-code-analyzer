/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.server.connection;

import java.net.SocketAddress;

/**
 * 集群令牌服务端连接抽象，封装本地/远端地址与最近读时间等信息。
 *
 * @author xuyue
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface Connection extends AutoCloseable {

    /** 返回连接的本地地址。 */
    SocketAddress getLocalAddress();

    /** 返回远端端口。 */
    int getRemotePort();

    /** 返回远端 IP 地址。 */
    String getRemoteIP();

    /** 刷新最近读时间戳。
     *
     * @param lastReadTime 最近读时间（毫秒）
     */

    /** 返回最近读时间戳（毫秒）。 */
    long getLastReadTime();

    /** 返回连接唯一键，通常为 {@code ip:port} 格式。 */
    String getConnectionKey();
}
