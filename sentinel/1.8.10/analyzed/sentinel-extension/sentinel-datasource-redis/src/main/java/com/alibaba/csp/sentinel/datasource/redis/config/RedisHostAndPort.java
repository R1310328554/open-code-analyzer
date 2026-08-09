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

package com.alibaba.csp.sentinel.datasource.redis.config;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * 主机与端口的不可变值对象，用于 Redis 连接地址解析。
 *
 * @author tiger
 */
public class RedisHostAndPort {

    private static final int NO_PORT = -1;

    public final String host;
    public final int port;

    /**
     * @param host must not be empty or {@literal null}.
     * @param port 端口号，{@link #NO_PORT} 表示未指定
     */
    private RedisHostAndPort(String host, int port) {
        AssertUtil.notNull(host, "host must not be null");

        this.host = host;
        this.port = port;
    }

    /**
     * 根据 {@code host} 与 {@code port} 创建 {@link RedisHostAndPort} 实例。
     *
     * @param host the hostname
     * @param port a valid port
     * @return the {@link RedisHostAndPort} of {@code host} and {@code port}
     */
    public static RedisHostAndPort of(String host, int port) {
        AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));
        return new RedisHostAndPort(host, port);
    }

    /**
     * @return 是否已指定有效端口号。
     */
    public boolean hasPort() {
        return port != NO_PORT;
    }

    /**
     * @return 主机名文本。
     */
    public String getHost() {
        return host;
    }

    /**
     * @return 端口号；未指定时抛出 {@link IllegalStateException}。
     */
    public int getPort() {
        if (!hasPort()) {
            throw new IllegalStateException("No port present.");
        }
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RedisHostAndPort)) {
            return false;
        }
        RedisHostAndPort that = (RedisHostAndPort)o;
        return port == that.port && (host != null ? host.equals(that.host) : that.host == null);
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    /**
     * @param port the port number
     * @return 端口是否在 0–65535 合法范围内。
     */
    private static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(host);
        if (hasPort()) {
            sb.append(':').append(port);
        }
        return sb.toString();
    }
}
