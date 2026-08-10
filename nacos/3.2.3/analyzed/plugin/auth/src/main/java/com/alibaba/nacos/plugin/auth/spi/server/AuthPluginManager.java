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

package com.alibaba.nacos.plugin.auth.spi.server;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.nacos.api.plugin.PluginStateCheckerHolder;
import com.alibaba.nacos.api.plugin.PluginType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 服务端认证插件管理器，负责通过 SPI 加载并按名称索引 {@link AuthPluginService} 实例。
 *
 * <p>采用单例模式，在类加载时自动扫描并注册所有认证插件实现。
 * 查找插件时会检查 {@link PluginStateCheckerHolder} 中的启用状态。</p>
 *
 * @author Wuyfee
 * @author xiweng.yy
 */
public class AuthPluginManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthPluginManager.class);
    
    private static final AuthPluginManager INSTANCE = new AuthPluginManager();
    
    /**
     * 认证服务名称与 {@link AuthPluginService} 实例的映射。
     */
    private final Map<String, AuthPluginService> authServiceMap = new HashMap<>();
    
    private AuthPluginManager() {
        initAuthServices();
    }
    
    /**
     * 通过 SPI 扫描并注册所有 {@link AuthPluginService} 实现。
     */
    private void initAuthServices() {
        Collection<AuthPluginService> authPluginServices =
            NacosServiceLoader.load(AuthPluginService.class);
        for (AuthPluginService each : authPluginServices) {
            if (StringUtils.isEmpty(each.getAuthServiceName())) {
                LOGGER.warn(
                    "[AuthPluginManager] Load AuthPluginService({}) AuthServiceName(null/empty) fail. Please Add AuthServiceName to resolve.",
                    each.getClass());
                continue;
            }
            authServiceMap.put(each.getAuthServiceName(), each);
            LOGGER.info(
                "[AuthPluginManager] Load AuthPluginService({}) AuthServiceName({}) successfully.",
                each.getClass(), each.getAuthServiceName());
        }
    }
    
    public static AuthPluginManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 按认证服务名称查找对应的 {@link AuthPluginService} 实例。
     *
     * @param authServiceName 认证服务名称，标识一个 {@link AuthPluginService} 实例
     * @return 匹配的认证插件实例；未找到或插件已禁用时返回空
     */
    public Optional<AuthPluginService> findAuthServiceSpiImpl(String authServiceName) {
        // 检查插件是否已启用
        if (!PluginStateCheckerHolder.isPluginEnabled(PluginType.AUTH.getType(), authServiceName)) {
            LOGGER.debug("[AuthPluginManager] Plugin AUTH:{} is disabled", authServiceName);
            return Optional.empty();
        }
        return Optional.ofNullable(authServiceMap.get(authServiceName));
    }
    
    /**
     * 获取所有已注册的认证插件。
     *
     * @return 不可修改的认证服务名称到 {@link AuthPluginService} 的映射
     */
    public Map<String, AuthPluginService> getAllPlugins() {
        return Collections.unmodifiableMap(authServiceMap);
    }
    
}
