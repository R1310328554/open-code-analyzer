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
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.*;

/**
 * Redis 客户端连接配置及 Builder，支持单机/Sentinel/Cluster。
 *
 * @author tiger
 */
public class RedisConnectionConfig {

    /**
     * Sentinel 默认端口。
     */
    public static final int DEFAULT_SENTINEL_PORT = 26379;

    /**
     * Cluster 默认端口。
     */
    public static final int DEFAULT_CLUSTER_PORT = 6379;

    /**
     * Redis 单机默认端口。
     */
    public static final int DEFAULT_REDIS_PORT = 6379;

    /**
     * 默认超时：60 秒
     */
    public static final long DEFAULT_TIMEOUT_MILLISECONDS = 60 * 1000;

    private String host;
    private String redisSentinelMasterId;
    private int port;
    private boolean sslEnable;
    private String trustedCertificatesPath;
    private String trustedCertificatesJksPassword;
    private String keyCertChainFilePath;
    private String keyFilePath;
    private String keyFilePassword;
    private int database;
    private String clientName;
    private char[] password;
    private long timeout = DEFAULT_TIMEOUT_MILLISECONDS;
    private final List<RedisConnectionConfig> redisSentinels = new ArrayList<RedisConnectionConfig>();
    private final List<RedisConnectionConfig> redisClusters = new ArrayList<RedisConnectionConfig>();

    /**
     * 默认无参构造。
     */
    public RedisConnectionConfig() {
    }

    /**
     * 指定主机、端口与超时构造。
     *
     * @param host 主机
     * @param port 端口
     * @param timeout 超时（毫秒）
     */
    public RedisConnectionConfig(String host, int port, long timeout) {

        AssertUtil.notEmpty(host, "Host must not be empty");
        AssertUtil.notNull(timeout, "Timeout duration must not be null");
        AssertUtil.isTrue(timeout >= 0, "Timeout duration must be greater or equal to zero");

        setHost(host);
        setPort(port);
        setTimeout(timeout);
    }

    /**
     * 返回用于构建 {@link RedisConnectionConfig} 的 {@link RedisConnectionConfig.Builder}。
     *
     * @return a new {@link RedisConnectionConfig.Builder} to construct a {@link RedisConnectionConfig}.
     */
    public static RedisConnectionConfig.Builder builder() {
        return new RedisConnectionConfig.Builder();
    }

    /**
     * 返回主机地址。
     *
     * @return the host.
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置 Redis 主机。
     *
     * @param host the host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 返回 Sentinel Master ID。
     *
     * @return the Sentinel Master Id.
     */
    public String getRedisSentinelMasterId() {
        return redisSentinelMasterId;
    }

    /**
     * 设置 Sentinel Master ID。
     *
     * @param redisSentinelMasterId the Sentinel Master Id.
     */
    public void setRedisSentinelMasterId(String redisSentinelMasterId) {
        this.redisSentinelMasterId = redisSentinelMasterId;
    }

    /**
     * 返回 Redis 端口。
     *
     * @return the Redis port
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置 Redis 端口，默认 {@link #DEFAULT_REDIS_PORT}。
     *
     * @param port the Redis port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 返回密码。
     *
     * @return the password
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * 设置密码；空字符串表示跳过认证。
     *
     * @param password 密码，不可为 {@literal null}
     */
    public void setPassword(String password) {

        AssertUtil.notNull(password, "Password must not be null");
        this.password = password.toCharArray();
    }

    /**
     * 设置密码字符数组；空数组表示跳过认证。
     *
     * @param password 密码, must not be {@literal null}.
     */
    public void setPassword(char[] password) {

        AssertUtil.notNull(password, "Password must not be null");
        this.password = Arrays.copyOf(password, password.length);
    }

    /**
     * 返回同步命令执行超时（毫秒）。
     *
     * @return the Timeout
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * 设置同步命令执行超时。
     *
     * @param timeout 同步命令超时
     */
    public void setTimeout(Long timeout) {

        AssertUtil.notNull(timeout, "Timeout must not be null");
        AssertUtil.isTrue(timeout >= 0, "Timeout must be greater or equal 0");

        this.timeout = timeout;
    }

    /**
     * 返回 Redis 库编号；仅单机/主从模式可用。
     *
     * @return database
     */
    public int getDatabase() {
        return database;
    }

    /**
     * 设置 Redis 库编号。
     *
     * @param database Redis 库编号
     */
    public void setDatabase(int database) {

        AssertUtil.isTrue(database >= 0, "Invalid database number: " + database);

        this.database = database;
    }

    /**
     * 返回客户端名称。
     *
     * @return
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * 设置 Redis 连接上的客户端名称。
     *
     * @param clientName 客户端名称
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * @return {@link RedisConnectionConfig Sentinel 节点}列表。
     */
    public List<RedisConnectionConfig> getRedisSentinels() {
        return redisSentinels;
    }

    /**
     * @return {@link RedisConnectionConfig Cluster 节点}列表。
     */
    public List<RedisConnectionConfig> getRedisClusters() {
        return redisClusters;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());

        sb.append(" [");

        if (host != null) {
            sb.append("host='").append(host).append('\'');
            sb.append(", port=").append(port);
        }
        if (redisSentinelMasterId != null) {
            sb.append("redisSentinels=").append(getRedisSentinels());
            sb.append(", redisSentinelMasterId=").append(redisSentinelMasterId);
        }

        if (redisClusters.size() > 0) {
            sb.append("redisClusters=").append(getRedisClusters());
        }

        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RedisConnectionConfig)) {
            return false;
        }
        RedisConnectionConfig redisURI = (RedisConnectionConfig)o;

        if (port != redisURI.port) {
            return false;
        }
        if (database != redisURI.database) {
            return false;
        }
        if (host != null ? !host.equals(redisURI.host) : redisURI.host != null) {
            return false;
        }
        if (redisSentinelMasterId != null ? !redisSentinelMasterId.equals(redisURI.redisSentinelMasterId)
            : redisURI.redisSentinelMasterId != null) {
            return false;
        }
        if (redisClusters != null ? !redisClusters.equals(redisURI.redisClusters)
            : redisURI.redisClusters != null) {
            return false;
        }
        return !(redisSentinels != null ? !redisSentinels.equals(redisURI.redisSentinels)
            : redisURI.redisSentinels != null);

    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (redisSentinelMasterId != null ? redisSentinelMasterId.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + database;
        result = 31 * result + (redisSentinels != null ? redisSentinels.hashCode() : 0);
        result = 31 * result + (redisClusters != null ? redisClusters.hashCode() : 0);
        return result;
    }

    /**
     * {@link RedisConnectionConfig} 构建器。
     */
    public static class Builder {

        private String host;
        private String redisSentinelMasterId;
        private int port;
        private int database;
        private String clientName;
        private char[] password;
        private boolean sslEnable;
        private String trustedCertificatesPath;
        private String trustedCertificatesJksPassword;
        private String keyCertChainFilePath;
        private String keyFilePath;
        private String keyFilePassword;
        private long timeout = DEFAULT_TIMEOUT_MILLISECONDS;
        private final List<RedisHostAndPort> redisSentinels = new ArrayList<RedisHostAndPort>();
        private final List<RedisHostAndPort> redisClusters = new ArrayList<RedisHostAndPort>();

        private Builder() {
        }

        /**
         * 设置 Redis 主机并创建新 Builder。
         *
         * @param host 主机名
         * @return 含 Redis 主机/端口的 Builder。
         */
        public static RedisConnectionConfig.Builder redis(String host) {
            return redis(host, DEFAULT_REDIS_PORT);
        }

        /**
         * 设置 Redis 主机与端口并创建新 Builder。
         *
         * @param host the host name
         * @param port 端口
         * @return New builder with Redis host/port.
         */
        public static RedisConnectionConfig.Builder redis(String host, int port) {

            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            Builder builder = RedisConnectionConfig.builder();
            return builder.withHost(host).withPort(port);
        }

        /**
         * 设置 Sentinel 主机并创建新 Builder。
         *
         * @param host the host name
         * @return 含 Sentinel 主机/端口的 Builder。
         */
        public static RedisConnectionConfig.Builder redisSentinel(String host) {

            AssertUtil.notEmpty(host, "Host must not be empty");

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisSentinel(host);
        }

        /**
         * 设置 Sentinel 主机与端口并创建新 Builder。
         *
         * @param host the host name
         * @param port the port
         * @return New builder with Sentinel host/port.
         */
        public static RedisConnectionConfig.Builder redisSentinel(String host, int port) {

            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisSentinel(host, port);
        }

        /**
         * 设置 Sentinel 主机与 Master ID 并创建新 Builder。
         *
         * @param host     the host name
         * @param masterId Sentinel Master ID
         * @return New builder with Sentinel host/port.
         */
        public static RedisConnectionConfig.Builder redisSentinel(String host, String masterId) {
            return redisSentinel(host, DEFAULT_SENTINEL_PORT, masterId);
        }

        /**
         * 设置 Sentinel 主机、端口与 Master ID 并创建新 Builder。
         *
         * @param host     the host name
         * @param port     the port
         * @param masterId redisSentinel master id
         * @return New builder with Sentinel host/port.
         */
        public static RedisConnectionConfig.Builder redisSentinel(String host, int port, String masterId) {

            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withSentinelMasterId(masterId).withRedisSentinel(host, port);
        }

        /**
         * 向 Builder 追加 Sentinel 节点。
         *
         * @param host the host name
         * @return Builder 自身
         */
        public RedisConnectionConfig.Builder withRedisSentinel(String host) {
            return withRedisSentinel(host, DEFAULT_SENTINEL_PORT);
        }

        /**
         * 向 Builder 追加 Sentinel 主机与端口。
         *
         * @param host the host name
         * @param port the port
         * @return the builder
         */
        public RedisConnectionConfig.Builder withRedisSentinel(String host, int port) {

            AssertUtil.assertState(this.host == null, "Cannot use with Redis mode.");
            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            redisSentinels.add(RedisHostAndPort.of(host, port));
            return this;
        }

        /**
         * 设置 Cluster 主机并创建新 Builder。
         *
         * @param host the host name
         * @return 含 Cluster 主机/端口的 Builder。
         */
        public static RedisConnectionConfig.Builder redisCluster(String host) {

            AssertUtil.notEmpty(host, "Host must not be empty");

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisCluster(host);
        }

        /**
         * 设置 Cluster 主机与端口并创建新 Builder。
         *
         * @param host the host name
         * @param port the port
         * @return New builder with Cluster host/port.
         */
        public static RedisConnectionConfig.Builder redisCluster(String host, int port) {

            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisCluster(host, port);
        }

        /**
         * 向 Builder 追加 Cluster 节点。
         *
         * @param host the host name
         * @return the builder
         */
        public RedisConnectionConfig.Builder withRedisCluster(String host) {
            return withRedisCluster(host, DEFAULT_CLUSTER_PORT);
        }

        /**
         * 向 Builder 追加 Cluster 主机与端口。
         *
         * @param host the host name
         * @param port the port
         * @return the builder
         */
        public RedisConnectionConfig.Builder withRedisCluster(String host, int port) {

            AssertUtil.assertState(this.host == null, "Cannot use with Redis mode.");
            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            redisClusters.add(RedisHostAndPort.of(host, port));
            return this;
        }

        /**
         * 设置主机（仅单机模式，不可与 Sentinel 混用）。
         *
         * @param host 主机名
         * @return the builder
         */
        public RedisConnectionConfig.Builder withHost(String host) {

            AssertUtil.assertState(this.redisSentinels.isEmpty(),
                "Sentinels are non-empty. Cannot use in Sentinel mode.");
            AssertUtil.notEmpty(host, "Host must not be empty");

            this.host = host;
            return this;
        }

        /**
         * 设置端口（需先设置主机）。
         *
         * @param port the port
         * @return the builder
         */
        public RedisConnectionConfig.Builder withPort(int port) {

            AssertUtil.assertState(this.host != null, "Host is null. Cannot use in Sentinel mode.");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            this.port = port;
            return this;
        }

        /**
         * 配置库编号。
         *
         * @param database the database number
         * @return the builder
         */
        public RedisConnectionConfig.Builder withDatabase(int database) {

            AssertUtil.isTrue(database >= 0, "Invalid database number: " + database);

            this.database = database;
            return this;
        }

        /**
         * 配置客户端名称。
         *
         * @param clientName the client name
         * @return the builder
         */
        public RedisConnectionConfig.Builder withClientName(String clientName) {

            AssertUtil.notNull(clientName, "Client name must not be null");

            this.clientName = clientName;
            return this;
        }


        /**
         * 配置认证密码。
         *
         * @param password the password
         * @return the builder
         */
        public RedisConnectionConfig.Builder withPassword(String password) {

            AssertUtil.notNull(password, "Password must not be null");

            return withPassword(password.toCharArray());
        }

        /**
         * Configures authentication.
         *
         * @param password the password
         * @return the builder
         */
        public RedisConnectionConfig.Builder withPassword(char[] password) {

            AssertUtil.notNull(password, "Password must not be null");

            this.password = Arrays.copyOf(password, password.length);
            return this;
        }

        /**
         * 配置命令超时。
         *
         * @param timeout must not be {@literal null} or negative.
         * @return the builder
         */
        public RedisConnectionConfig.Builder withTimeout(long timeout) {

            AssertUtil.notNull(timeout, "Timeout must not be null");
            AssertUtil.notNull(timeout >= 0, "Timeout must be greater or equal 0");

            this.timeout = timeout;
            return this;
        }

        /**
         * 配置 Sentinel Master ID。
         *
         * @param sentinelMasterId Sentinel Master ID，不可为空
         * @return the builder
         */
        public RedisConnectionConfig.Builder withSentinelMasterId(String sentinelMasterId) {

            AssertUtil.notEmpty(sentinelMasterId, "Sentinel master id must not empty");

            this.redisSentinelMasterId = sentinelMasterId;
            return this;
        }

        /**
         * 设置是否启用 SSL。
         *
         * @param sslEnable sslEnable
         * @return Builder 自身
         */
        public RedisConnectionConfig.Builder withSslEnable(boolean sslEnable) {
            this.sslEnable = sslEnable;
            return this;
        }

        /**
         * 设置受信任证书路径。
         *
         * @param trustedCertificatesPath trustedCertificatesPath
         * @return the value of Builder
         */
        public RedisConnectionConfig.Builder withTrustedCertificatesPath(String trustedCertificatesPath) {

            AssertUtil.notEmpty(trustedCertificatesPath, "trusted certificates path must not empty");

            this.trustedCertificatesPath = trustedCertificatesPath;
            return this;
        }

        /**
         * 设置受信任证书 JKS 密码。
         *
         * @param trustedCertificatesJksPassword trustedCertificatesJksPassword
         * @return the value of Builder
         */
        public RedisConnectionConfig.Builder withTrustedCertificatesJksPassword(String trustedCertificatesJksPassword) {
            this.trustedCertificatesJksPassword = trustedCertificatesJksPassword;
            return this;
        }

        /**
         * 设置密钥证书链文件路径。
         *
         * @param keyCertChainFilePath keyCertChainFilePath
         * @return the value of Builder
         */
        public RedisConnectionConfig.Builder withKeyCertChainFilePath(String keyCertChainFilePath) {
            this.keyCertChainFilePath = keyCertChainFilePath;
            return this;
        }

        /**
         * 设置私钥文件路径。
         *
         * @param keyFilePath keyFilePath
         * @return the value of Builder
         */
        public RedisConnectionConfig.Builder withKeyFilePath(String keyFilePath) {
            this.keyFilePath = keyFilePath;
            return this;
        }

        /**
         * 设置私钥文件密码。
         *
         * @param keyFilePassword keyFilePassword
         * @return the value of Builder
         */
        public RedisConnectionConfig.Builder withKeyFilePassword(String keyFilePassword) {
            this.keyFilePassword = keyFilePassword;
            return this;
        }

        /**
         * @return 构建完成的 {@link RedisConnectionConfig}。
         */
        public RedisConnectionConfig build() {

            if (redisSentinels.isEmpty() && redisClusters.isEmpty() && StringUtil.isEmpty(host)) {
                throw new IllegalStateException(
                    "Cannot build a RedisConnectionConfig. One of the following must be provided Host, Socket, Cluster or "
                        + "Sentinel");
            }

            RedisConnectionConfig redisConnectionConfig = new RedisConnectionConfig();
            redisConnectionConfig.setHost(host);
            redisConnectionConfig.setPort(port);

            if (sslEnable){
                redisConnectionConfig.setSslEnable(true);
                redisConnectionConfig.setTrustedCertificatesPath(trustedCertificatesPath);
                redisConnectionConfig.setTrustedCertificatesJksPassword(trustedCertificatesJksPassword);
                redisConnectionConfig.setKeyCertChainFilePath(keyCertChainFilePath);
                redisConnectionConfig.setKeyFilePath(keyFilePath);
                redisConnectionConfig.setKeyFilePassword(keyFilePassword);
            }

            if (password != null) {
                redisConnectionConfig.setPassword(password);
            }

            redisConnectionConfig.setDatabase(database);
            redisConnectionConfig.setClientName(clientName);

            redisConnectionConfig.setRedisSentinelMasterId(redisSentinelMasterId);

            for (RedisHostAndPort sentinel : redisSentinels) {
                redisConnectionConfig.getRedisSentinels().add(
                    new RedisConnectionConfig(sentinel.getHost(), sentinel.getPort(), timeout));
            }

            for (RedisHostAndPort sentinel : redisClusters) {
                redisConnectionConfig.getRedisClusters().add(
                    new RedisConnectionConfig(sentinel.getHost(), sentinel.getPort(), timeout));
            }

            redisConnectionConfig.setTimeout(timeout);

            return redisConnectionConfig;
        }
    }

    /**
     * 判断端口是否在有效范围内。
     */
    private static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    /**
     * 获取受信任证书路径。
     *
     * @return the value of trustedCertificatesPath
     */
    public String getTrustedCertificatesPath() {
        return trustedCertificatesPath;
    }

    /**
     * Sets the trustedCertificatesPath.
     * <p>
     * <p>You can use getTrustedCertificatesPath() to get the value of trustedCertificatesPath</p>
     *
     * @param trustedCertificatesPath trustedCertificatesPath
     */
    public void setTrustedCertificatesPath(String trustedCertificatesPath) {
        this.trustedCertificatesPath = trustedCertificatesPath;
    }

    /**
     * 获取受信任证书 JKS 密码。
     *
     * @return the value of trustedCertificatesJksPassword
     */
    public String getTrustedCertificatesJksPassword() {
        return trustedCertificatesJksPassword;
    }

    /**
     * Sets the trustedCertificatesJksPassword.
     * <p>
     * <p>You can use getTrustedCertificatesJksPassword() to get the value of trustedCertificatesJksPassword</p>
     *
     * @param trustedCertificatesJksPassword trustedCertificatesJksPassword
     */
    public void setTrustedCertificatesJksPassword(String trustedCertificatesJksPassword) {
        this.trustedCertificatesJksPassword = trustedCertificatesJksPassword;
    }

    /**
     * 获取密钥证书链文件路径。
     *
     * @return the value of keyCertChainFilePath
     */
    public String getKeyCertChainFilePath() {
        return keyCertChainFilePath;
    }

    /**
     * Sets the keyCertChainFilePath.
     * <p>
     * <p>You can use getKeyCertChainFilePath() to get the value of keyCertChainFilePath</p>
     *
     * @param keyCertChainFilePath keyCertChainFilePath
     */
    public void setKeyCertChainFilePath(String keyCertChainFilePath) {
        this.keyCertChainFilePath = keyCertChainFilePath;
    }

    /**
     * 获取私钥文件路径。
     *
     * @return the value of keyFilePath
     */
    public String getKeyFilePath() {
        return keyFilePath;
    }

    /**
     * Sets the keyFilePath.
     * <p>
     * <p>You can use getKeyFilePath() to get the value of keyFilePath</p>
     *
     * @param keyFilePath keyFilePath
     */
    public void setKeyFilePath(String keyFilePath) {
        this.keyFilePath = keyFilePath;
    }

    /**
     * 获取私钥文件密码。
     *
     * @return the value of keyFilePassword
     */
    public String getKeyFilePassword() {
        return keyFilePassword;
    }

    /**
     * Sets the keyFilePassword.
     * <p>
     * <p>You can use getKeyFilePassword() to get the value of keyFilePassword</p>
     *
     * @param keyFilePassword keyFilePassword
     */
    public void setKeyFilePassword(String keyFilePassword) {
        this.keyFilePassword = keyFilePassword;
    }

    /**
     * Sets the sslEnable.
     * <p>
     * <p>You can use isSslEnable() to get the value of sslEnable</p>
     *
     * @param sslEnable sslEnable
     */
    public void setSslEnable(boolean sslEnable) {
        this.sslEnable = sslEnable;
    }


    /**
     * 获取是否启用 SSL。
     *
     * @return the value of sslEnable
     */
    public boolean isSslEnable() {
        return sslEnable;
    }
}
