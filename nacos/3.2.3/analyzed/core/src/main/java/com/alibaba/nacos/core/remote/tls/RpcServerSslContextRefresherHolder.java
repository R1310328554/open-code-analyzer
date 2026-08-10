/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.remote.tls;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.util.Collection;
import java.util.Properties;

/**
 * {@link RpcServerSslContextRefresher} 实例的持有者，负责按通信类型（SDK 或 Cluster）初始化并提供 SSL 上下文刷新器。
 *
 * <p>通过 SPI 加载刷新器实现，并与 {@link RpcServerTlsConfigFactory} 生成的 TLS 配置匹配。</p>
 *
 * @author liuzunfei
 * @version $Id: RpcServerSslContextRefresherHolder.java, v 0.1 2023年03月17日 12:00 PM liuzunfei Exp $
 */
public class RpcServerSslContextRefresherHolder {
    
    /** SDK 通信通道对应的 {@link RpcServerSslContextRefresher} 实例。 */
    private static RpcServerSslContextRefresher sdkInstance;
    
    /** 集群通信通道对应的 {@link RpcServerSslContextRefresher} 实例。 */
    private static RpcServerSslContextRefresher clusterInstance;
    
    /** 类加载时初始化 SDK 与 Cluster 两套 SSL 上下文刷新器。 */
    static {
        init();
    }
    
    /**
     * 获取 SDK 通信通道的 {@link RpcServerSslContextRefresher} 实例。
     *
     * @return SDK 通道 SSL 上下文刷新器，未配置时可能为 {@code null}
     */
    public static RpcServerSslContextRefresher getSdkInstance() {
        return sdkInstance;
    }
    
    /**
     * 获取集群通信通道的 {@link RpcServerSslContextRefresher} 实例。
     *
     * @return 集群通道 SSL 上下文刷新器，未配置时可能为 {@code null}
     */
    public static RpcServerSslContextRefresher getClusterInstance() {
        return clusterInstance;
    }
    
    /**
     * 初始化持有者：加载 SPI 刷新器并按 SDK/Cluster TLS 配置匹配对应实现。
     */
    private static void init() {
        synchronized (RpcServerSslContextRefresherHolder.class) {
            Properties properties = EnvUtil.getProperties();
            RpcServerTlsConfig clusterServerTlsConfig =
                RpcServerTlsConfigFactory.getInstance().createClusterConfig(properties);
            RpcServerTlsConfig sdkServerTlsConfig =
                RpcServerTlsConfigFactory.getInstance().createSdkConfig(properties);
            Collection<RpcServerSslContextRefresher> refreshers = NacosServiceLoader.load(
                RpcServerSslContextRefresher.class);
            sdkInstance = getSslContextRefresher(refreshers, sdkServerTlsConfig);
            clusterInstance = getSslContextRefresher(refreshers, clusterServerTlsConfig);
            Loggers.REMOTE.info("RpcServerSslContextRefresher initialization completed.");
        }
    }
    
    /**
     * 根据 TLS 配置从 SPI 集合中选取并初始化 SSL 上下文刷新器。
     *
     * @param refreshers      SPI 加载的全部刷新器实现
     * @param serverTlsConfig 对应通道的 TLS 配置（含刷新器名称）
     * @return 匹配到的刷新器实例，未配置或未找到时返回 {@code null}
     */
    private static RpcServerSslContextRefresher getSslContextRefresher(
        Collection<RpcServerSslContextRefresher> refreshers, RpcServerTlsConfig serverTlsConfig) {
        String refresherName = serverTlsConfig.getSslContextRefresher();
        RpcServerSslContextRefresher instance = null;
        if (StringUtils.isNotBlank(refresherName)) {
            for (RpcServerSslContextRefresher contextRefresher : refreshers) {
                if (refresherName.equals(contextRefresher.getName())) {
                    instance = contextRefresher;
                    Loggers.REMOTE.info("RpcServerSslContextRefresher initialized using {}.",
                        contextRefresher.getClass().getSimpleName());
                    break;
                }
            }
            if (instance == null) {
                Loggers.REMOTE.warn("Failed to find RpcServerSslContextRefresher with name {}.",
                    refresherName);
            }
        } else {
            Loggers.REMOTE.info("Ssl Context auto refresh is not supported.");
        }
        return instance;
    }
}
