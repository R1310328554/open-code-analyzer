/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.address;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.remote.client.ServerListFactory;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Server list Manager.
 * <p>Nacos 服务端地址列表管理抽象基类：实现 {@link ServerListFactory}，通过 SPI 选择 {@link ServerListProvider}、初始化并暴露 server 列表及命名空间等元数据。</p>
 *
 * @author totalo
 */
public abstract class AbstractServerListManager implements ServerListFactory, Closeable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServerListManager.class);
    
    /** 当前匹配到的地址列表提供者 */
    protected ServerListProvider serverListProvider;
    
    /** 派生后的客户端属性（含模块类型与可选 namespace） */
    protected NacosClientProperties properties;
    
    /** 使用默认 namespace 构造管理器 */
    public AbstractServerListManager(NacosClientProperties properties) {
        this(properties, null);
    }
    
    public AbstractServerListManager(NacosClientProperties properties, String namespace) {
        // 派生副本，避免 setProperty 污染调用方传入的 properties
        NacosClientProperties tmpProperties = properties.derive();
        if (StringUtils.isNotBlank(namespace)) {
            tmpProperties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
        }
        tmpProperties.setProperty(Constants.CLIENT_MODULE_TYPE, getModuleName());
        this.properties = tmpProperties;
    }
    
    @Override
    public List<String> getServerList() {
        return serverListProvider.getServerList();
    }
    
    @Override
    public void shutdown() throws NacosException {
        String className = this.getClass().getName();
        LOGGER.info("{} do shutdown begin", className);
        if (null != serverListProvider) {
            serverListProvider.shutdown();
        }
        serverListProvider = null;
        LOGGER.info("{} do shutdown stop", className);
    }
    
    /**
     * Start server list manager.
     * <p>SPI 加载全部 {@link ServerListProvider}，按 {@link ServerListProvider#getOrder()} 降序匹配并初始化首个成功者。</p>
     *
     * @throws NacosException during start and initialize.
     */
    public void start() throws NacosException {
        Collection<ServerListProvider> serverListProviders =
            NacosServiceLoader.load(ServerListProvider.class);
        Collection<ServerListProvider> sorted = serverListProviders.stream()
            .sorted((a, b) -> b.getOrder() - a.getOrder()).collect(Collectors.toList());
        for (ServerListProvider each : sorted) {
            boolean matchResult = each.match(properties);
            LOGGER.info("Load and match ServerListProvider {}, match result: {}",
                each.getClass().getCanonicalName(),
                matchResult);
            if (matchResult) {
                this.serverListProvider = each;
                LOGGER.info("Will use {} as ServerListProvider",
                    this.serverListProvider.getClass().getCanonicalName());
                break;
            }
        }
        if (null == serverListProvider) {
            LOGGER.error("No server list provider found, SPI load size: {}", sorted.size());
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM,
                "No server list provider found.");
        }
        this.serverListProvider.init(properties, getNacosRestTemplate());
    }
    
    /** 模块名与 provider 服务名组合的唯一标识 */
    public String getServerName() {
        return getModuleName() + "-" + serverListProvider.getServerName();
    }
    
    /** 委托 provider 返回 context path */
    public String getContextPath() {
        return serverListProvider.getContextPath();
    }
    
    /** 委托 provider 返回 namespace */
    public String getNamespace() {
        return serverListProvider.getNamespace();
    }
    
    /** 委托 provider 返回地址来源描述（如 endpoint URL） */
    public String getAddressSource() {
        return serverListProvider.getAddressSource();
    }
    
    /** 服务端列表是否为固定配置（非动态 endpoint 刷新） */
    public boolean isFixed() {
        return serverListProvider.isFixed();
    }
    
    /**
     * get module name.
     * <p>子类返回 config/naming 等模块标识，写入 {@link Constants#CLIENT_MODULE_TYPE}。</p>
     *
     * @return module name
     */
    protected abstract String getModuleName();
    
    /**
     * get nacos rest template.
     * <p>子类提供 HTTP 客户端，供 endpoint 型 provider 拉取地址列表。</p>
     *
     * @return nacos rest template
     */
    protected abstract NacosRestTemplate getNacosRestTemplate();
    
    /** 测试用：返回内部 properties 副本 */
    @JustForTest
    NacosClientProperties getProperties() {
        return properties;
    }
}
