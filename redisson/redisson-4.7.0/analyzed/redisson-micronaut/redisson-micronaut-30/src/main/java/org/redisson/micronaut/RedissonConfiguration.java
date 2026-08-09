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
package org.redisson.micronaut;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;
import org.redisson.client.NettyHook;
import org.redisson.client.codec.Codec;
import org.redisson.config.*;
import org.redisson.connection.AddressResolverGroupFactory;
import org.redisson.connection.ConnectionListener;

import java.lang.reflect.InvocationTargetException;

/**
 * Micronaut {@link ConfigurationProperties} 绑定的 Redisson {@link Config} 扩展（Micronaut 3.x）。
 * <p>首次访问各服务器模式配置时按需懒初始化对应子配置块。
 *
 * @author Nikita Koksharov
 */
@ConfigurationProperties("redisson")
@Requires(missingBeans = Config.class)
@Requires(property = "redisson")
public class RedissonConfiguration extends Config {

    /** 默认构造；子配置块在首次 getter 调用时创建。 */
    public RedissonConfiguration() {
    }

    /** 获取单机模式配置；未指定任何模式时自动调用 {@link #useSingleServer()}。 */
    @Override
    public SingleServerConfig getSingleServerConfig() {
        if (isNotDefined()) {
            return useSingleServer();
        }
        return super.getSingleServerConfig();
    }

    /** Micronaut 配置绑定入口：{@code redisson.single-server-config.*}。 */
    @Override
    @ConfigurationBuilder("singleServerConfig")
    protected void setSingleServerConfig(SingleServerConfig singleConnectionConfig) {
        super.setSingleServerConfig(singleConnectionConfig);
    }

    /** 获取集群模式配置；未指定任何模式时自动调用 {@link #useClusterServers()}。 */
    @Override
    public ClusterServersConfig getClusterServersConfig() {
        if (isNotDefined()) {
            return useClusterServers();
        }
        return super.getClusterServersConfig();
    }

    /** Micronaut 配置绑定入口：{@code redisson.cluster-servers-config.*}。 */
    @Override
    @ConfigurationBuilder(value = "clusterServersConfig", includes = {"nodeAddresses"})
    protected void setClusterServersConfig(ClusterServersConfig clusterServersConfig) {
        super.setClusterServersConfig(clusterServersConfig);
    }

    /** 判断是否尚未选择任何 Redis 部署模式（五种子配置均为 null）。 */
    private boolean isNotDefined() {
        return super.getSingleServerConfig() == null
                && super.getClusterServersConfig() == null
                && super.getReplicatedServersConfig() == null
                && super.getSentinelServersConfig() == null
                && super.getMasterSlaveServersConfig() == null;
    }

    /** 获取复制模式配置；未指定任何模式时自动调用 {@link #useReplicatedServers()}。 */
    @Override
    public ReplicatedServersConfig getReplicatedServersConfig() {
        if (isNotDefined()) {
            return useReplicatedServers();
        }
        return super.getReplicatedServersConfig();
    }

    @Override
    @ConfigurationBuilder(value = "replicatedServersConfig", includes = {"nodeAddresses"})
    protected void setReplicatedServersConfig(ReplicatedServersConfig replicatedServersConfig) {
        super.setReplicatedServersConfig(replicatedServersConfig);
    }

    /** 获取哨兵模式配置；未指定任何模式时自动调用 {@link #useSentinelServers()}。 */
    @Override
    public SentinelServersConfig getSentinelServersConfig() {
        if (isNotDefined()) {
            return useSentinelServers();
        }
        return super.getSentinelServersConfig();
    }

    @Override
    @ConfigurationBuilder(value = "sentinelServersConfig", includes = {"sentinelAddresses"})
    protected void setSentinelServersConfig(SentinelServersConfig sentinelConnectionConfig) {
        super.setSentinelServersConfig(sentinelConnectionConfig);
    }

    /** 获取主从模式配置；未指定任何模式时自动调用 {@link #useMasterSlaveServers()}。 */
    @Override
    public MasterSlaveServersConfig getMasterSlaveServersConfig() {
        if (isNotDefined()) {
            return useMasterSlaveServers();
        }
        return super.getMasterSlaveServersConfig();
    }

    @Override
    @ConfigurationBuilder(value = "masterSlaveServersConfig", includes = {"slaveAddresses"})
    protected void setMasterSlaveServersConfig(MasterSlaveServersConfig masterSlaveConnectionConfig) {
        super.setMasterSlaveServersConfig(masterSlaveConnectionConfig);
    }

    @Override
    @ConfigurationBuilder(value = "codec1")
    public Config setCodec(Codec codec) {
        return super.setCodec(codec);
    }

    /** 通过反射按类名实例化 {@link Codec} 并设置到配置。 */
    public Config setCodec(String className) {
        try {
            Codec codec = (Codec) Class.forName(className).getDeclaredConstructor().newInstance();
            return super.setCodec(codec);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    @ConfigurationBuilder(value = "nettyHook1")
    public Config setNettyHook(NettyHook nettyHook) {
        return super.setNettyHook(nettyHook);
    }

    /** 通过反射按类名实例化 {@link NettyHook} 并设置到配置。 */
    public Config setNettyHook(String className) {
        try {
            NettyHook nettyHook = (NettyHook) Class.forName(className).getDeclaredConstructor().newInstance();
            return super.setNettyHook(nettyHook);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    @ConfigurationBuilder(value = "addressResolverGroupFactory1")
    public Config setAddressResolverGroupFactory(AddressResolverGroupFactory addressResolverGroupFactory) {
        return super.setAddressResolverGroupFactory(addressResolverGroupFactory);
    }

    /** 通过反射按类名实例化 {@link AddressResolverGroupFactory} 并设置到配置。 */
    public Config setAddressResolverGroupFactory(String className) {
        try {
            AddressResolverGroupFactory value = (AddressResolverGroupFactory) Class.forName(className).getDeclaredConstructor().newInstance();
            return super.setAddressResolverGroupFactory(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    @ConfigurationBuilder(value = "connectionListener1")
    public Config setConnectionListener(ConnectionListener connectionListener) {
        return super.setConnectionListener(connectionListener);
    }

    /** 通过反射按类名实例化 {@link ConnectionListener} 并设置到配置。 */
    public Config setConnectionListener(String className) {
        try {
            ConnectionListener connectionListener = (ConnectionListener) Class.forName(className).getDeclaredConstructor().newInstance();
            return super.setConnectionListener(connectionListener);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
