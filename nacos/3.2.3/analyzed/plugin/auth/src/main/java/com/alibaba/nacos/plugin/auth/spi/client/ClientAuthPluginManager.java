/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.spi.client;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 客户端认证插件管理器，负责通过 SPI 加载并管理 {@link ClientAuthService} 实例。
 *
 * <p>在客户端 SDK 启动时调用 {@link #init} 完成插件初始化，
 * 服务端列表变更时通过 {@link #refreshServerList} 同步更新。</p>
 *
 * @author wuyfee
 */
public class ClientAuthPluginManager implements Closeable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAuthPluginManager.class);
    
    /**
     * 已加载的 {@link ClientAuthService} 实例集合。
     */
    private final Set<ClientAuthService> clientAuthServiceHashSet = new HashSet<>();
    
    /**
     * 初始化客户端认证插件，注入服务端列表与 HTTP 模板。
     *
     * @param serverList         Nacos 服务端地址列表
     * @param nacosRestTemplate  HTTP 请求模板
     */
    public void init(List<String> serverList, NacosRestTemplate nacosRestTemplate) {
        
        Collection<AbstractClientAuthService> clientAuthServices = NacosServiceLoader
            .load(AbstractClientAuthService.class);
        for (ClientAuthService clientAuthService : clientAuthServices) {
            clientAuthService.setServerList(serverList);
            clientAuthService.setNacosRestTemplate(nacosRestTemplate);
            clientAuthServiceHashSet.add(clientAuthService);
            LOGGER.info("[ClientAuthPluginManager] Load ClientAuthService {} success.",
                clientAuthService.getClass().getCanonicalName());
        }
        if (clientAuthServiceHashSet.isEmpty()) {
            LOGGER.warn(
                "[ClientAuthPluginManager] Load ClientAuthService fail, No ClientAuthService implements");
        }
    }
    
    /**
     * 刷新所有已加载插件的服务端地址列表。
     *
     * @param serverList 新的服务端地址列表
     */
    public void refreshServerList(List<String> serverList) {
        for (ClientAuthService clientAuthService : clientAuthServiceHashSet) {
            clientAuthService.setServerList(serverList);
        }
    }
    
    /**
     * 获取所有已加载的 {@link ClientAuthService} 实例。
     *
     * @return 客户端认证服务实例集合
     */
    public Set<ClientAuthService> getAuthServiceSpiImplSet() {
        return clientAuthServiceHashSet;
    }
    
    @Override
    public void shutdown() throws NacosException {
        for (ClientAuthService each : clientAuthServiceHashSet) {
            each.shutdown();
        }
    }
}
