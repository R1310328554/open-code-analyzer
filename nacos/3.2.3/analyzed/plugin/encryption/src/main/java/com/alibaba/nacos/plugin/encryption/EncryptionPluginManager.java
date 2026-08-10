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

package com.alibaba.nacos.plugin.encryption;

import com.alibaba.nacos.api.plugin.PluginStateChecker;
import com.alibaba.nacos.api.plugin.PluginStateCheckerHolder;
import com.alibaba.nacos.api.plugin.PluginType;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.encryption.spi.EncryptionPluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加密插件管理器。
 *
 * <p>通过 SPI 加载 {@link EncryptionPluginService} 实现，
 * 按算法名索引，并支持插件启用状态检查与运行时动态注册。</p>
 *
 * @author lixiaoshuang
 */
public class EncryptionPluginManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionPluginManager.class);
    
    /** 算法名 → 加密插件服务实例。 */
    private static final Map<String, EncryptionPluginService> ENCRYPTION_SPI_MAP =
        new ConcurrentHashMap<>();
    
    /** 单例实例。 */
    private static final EncryptionPluginManager INSTANCE = new EncryptionPluginManager();
    
    private EncryptionPluginManager() {
        loadInitial();
    }
    
    /**
     * 初始化加载所有 SPI 注册的加密插件。
     */
    private void loadInitial() {
        Collection<EncryptionPluginService> encryptionPluginServices = NacosServiceLoader.load(
            EncryptionPluginService.class);
        for (EncryptionPluginService encryptionPluginService : encryptionPluginServices) {
            if (StringUtils.isBlank(encryptionPluginService.algorithmName())) {
                LOGGER.warn(
                    "[EncryptionPluginManager] Load EncryptionPluginService({}) algorithmName(null/empty) fail."
                        + " Please Add algorithmName to resolve.",
                    encryptionPluginService.getClass());
                continue;
            }
            ENCRYPTION_SPI_MAP.put(encryptionPluginService.algorithmName(),
                encryptionPluginService);
            LOGGER.info(
                "[EncryptionPluginManager] Load EncryptionPluginService({}) algorithmName({}) successfully.",
                encryptionPluginService.getClass(), encryptionPluginService.algorithmName());
        }
    }
    
    /**
     * 获取 EncryptionPluginManager 单例。
     *
     * @return EncryptionPluginManager 实例
     */
    public static EncryptionPluginManager instance() {
        return INSTANCE;
    }
    
    /**
     * 按算法名查找加密插件服务；若插件被禁用则返回空。
     *
     * @param algorithmName 算法名，标识一个 EncryptionPluginService 实例
     * @return 加密插件服务 Optional
     */
    public Optional<EncryptionPluginService> findEncryptionService(String algorithmName) {
        Optional<PluginStateChecker> checker = PluginStateCheckerHolder.getInstance();
        if (checker.isPresent()
            && !checker.get().isPluginEnabled(PluginType.ENCRYPTION.getType(), algorithmName)) {
            LOGGER.debug("[EncryptionPluginManager] Plugin ENCRYPTION:{} is disabled",
                algorithmName);
            return Optional.empty();
        }
        return Optional.ofNullable(ENCRYPTION_SPI_MAP.get(algorithmName));
    }
    
    /**
     * 运行时注入加密插件实现。
     *
     * @param encryptionPluginService 加密插件实现
     */
    public static synchronized void join(EncryptionPluginService encryptionPluginService) {
        if (Objects.isNull(encryptionPluginService)) {
            return;
        }
        ENCRYPTION_SPI_MAP.put(encryptionPluginService.algorithmName(), encryptionPluginService);
        LOGGER.info("[EncryptionPluginManager] join successfully.");
    }
    
    /**
     * 获取所有已注册的加密插件服务。
     *
     * @return 不可修改的算法名 → 插件服务映射
     */
    public Map<String, EncryptionPluginService> getAllPlugins() {
        return Collections.unmodifiableMap(ENCRYPTION_SPI_MAP);
    }
    
}
