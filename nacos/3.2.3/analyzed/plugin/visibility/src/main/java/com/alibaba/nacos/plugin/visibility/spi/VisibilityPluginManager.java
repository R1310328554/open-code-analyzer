/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.visibility.spi;

import com.alibaba.nacos.api.plugin.PluginStateCheckerHolder;
import com.alibaba.nacos.api.plugin.PluginType;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可见性服务加载与访问管理器。
 *
 * <p>通过 SPI 加载 {@link VisibilityService} 实现，按服务名索引，
 * 并支持插件级启用状态检查与初始化配置注入。</p>
 *
 * @author xiweng.yy
 */
public class VisibilityPluginManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VisibilityPluginManager.class);
    
    /** 单例实例。 */
    private static final VisibilityPluginManager INSTANCE = new VisibilityPluginManager();
    
    /** 可见性插件配置前缀。 */
    private static final String PROPERTIES_PREFIX = "nacos.plugin.visibility.";
    
    /** 全局启用开关配置键。 */
    private static final String ENABLED_PROPERTY = PROPERTIES_PREFIX + "enabled";
    
    /** 服务名 → 可见性服务实例。 */
    private final Map<String, VisibilityService> visibilityServiceMap = new ConcurrentHashMap<>();
    
    /** 是否已完成初始化。 */
    private volatile boolean initialized;
    
    private VisibilityPluginManager() {
        initVisibilityServices();
    }
    
    /**
     * 初始化加载所有 SPI 注册的可见性服务。
     */
    private synchronized void initVisibilityServices() {
        if (initialized) {
            return;
        }
        Properties allProperties = resolveInitProperties();
        ServiceLoader<VisibilityService> serviceLoader =
            ServiceLoader.load(VisibilityService.class);
        Iterator<VisibilityService> iterator = serviceLoader.iterator();
        while (true) {
            VisibilityService each;
            try {
                if (!iterator.hasNext()) {
                    break;
                }
                each = iterator.next();
            } catch (ServiceConfigurationError | RuntimeException ex) {
                LOGGER.warn(
                    "[VisibilityPluginManager] Failed to load one VisibilityService from SPI, skip it.",
                    ex);
                continue;
            }
            registerVisibilityService(each, allProperties);
        }
        initialized = true;
    }
    
    /**
     * 注册单个可见性服务：解析名称、注入配置并初始化。
     */
    private void registerVisibilityService(VisibilityService service, Properties allProperties) {
        String serviceName;
        try {
            serviceName = service.getVisibilityServiceName();
        } catch (Throwable ex) {
            LOGGER.warn(
                "[VisibilityPluginManager] VisibilityService({}) resolve name failed, skip.",
                service.getClass(), ex);
            return;
        }
        if (StringUtils.isEmpty(serviceName)) {
            LOGGER.warn(
                "[VisibilityPluginManager] VisibilityService({}) has empty serviceName, skip.",
                service.getClass());
            return;
        }
        Properties serviceProperties = resolveServiceProperties(allProperties, serviceName);
        try {
            service.init(serviceProperties);
        } catch (Throwable ex) {
            LOGGER.warn(
                "[VisibilityPluginManager] Initialize VisibilityService({}:{}) failed, skip.",
                service.getClass(), serviceName, ex);
            return;
        }
        visibilityServiceMap.put(serviceName, service);
        LOGGER.info("[VisibilityPluginManager] Loaded VisibilityService({}:{}) successfully.",
            service.getClass(), serviceName);
    }
    
    /**
     * 从全局配置中提取指定服务的专属配置项。
     */
    private Properties resolveServiceProperties(Properties allProperties, String serviceName) {
        Properties result = new Properties();
        if (allProperties.isEmpty()) {
            return result;
        }
        String legacyPrefix = PROPERTIES_PREFIX + serviceName + ".";
        for (String key : allProperties.stringPropertyNames()) {
            if (key.startsWith(legacyPrefix)) {
                result.setProperty(key.substring(legacyPrefix.length()),
                    allProperties.getProperty(key));
            }
        }
        return result;
    }
    
    /**
     * 获取管理器单例。
     *
     * @return 单例实例
     */
    public static VisibilityPluginManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 按名称查找可见性服务。
     *
     * <p>若全局或指定插件被禁用，则返回空。</p>
     *
     * @param serviceName service name
     * @return optional visibility service
     */
    public Optional<VisibilityService> findVisibilityService(String serviceName) {
        if (!isVisibilityPluginEnabled()) {
            LOGGER.debug("[VisibilityPluginManager] Plugin VISIBILITY is disabled by {}",
                ENABLED_PROPERTY);
            return Optional.empty();
        }
        if (!PluginStateCheckerHolder.isPluginEnabled(PluginType.VISIBILITY.getType(),
            serviceName)) {
            LOGGER.debug("[VisibilityPluginManager] Plugin VISIBILITY:{} is disabled", serviceName);
            return Optional.empty();
        }
        return Optional.ofNullable(visibilityServiceMap.get(serviceName));
    }
    
    /**
     * 判断可见性插件全局开关是否启用。
     */
    private boolean isVisibilityPluginEnabled() {
        Properties allProperties = resolveInitProperties();
        String enabledValue = allProperties.getProperty(ENABLED_PROPERTY);
        if (StringUtils.isBlank(enabledValue)) {
            return true;
        }
        return Boolean.parseBoolean(enabledValue);
    }
    
    /**
     * 返回所有已加载的可见性服务（不可修改视图）。
     *
     * @return 服务名 → 实例映射
     */
    public Map<String, VisibilityService> getAllPlugins() {
        return Collections.unmodifiableMap(visibilityServiceMap);
    }
    
    /**
     * 解析初始化所需的全部配置属性。
     *
     * <p>优先通过反射调用 {@code EnvUtil.getProperties()}，
     * 失败时回退到 {@link System#getProperties()}。</p>
     */
    private Properties resolveInitProperties() {
        // TODO: plugin/visibility 可依赖 nacos-sys 后，改为直接调用 EnvUtil.getProperties()
        try {
            Class<?> envUtilClass = Class.forName("com.alibaba.nacos.sys.env.EnvUtil");
            Method method = envUtilClass.getMethod("getProperties");
            Object result = method.invoke(null);
            if (result instanceof Properties) {
                return (Properties) result;
            }
        } catch (Throwable ex) {
            LOGGER.debug(
                "[VisibilityPluginManager] Cannot load EnvUtil properties, fallback to system properties.",
                ex);
        }
        Properties fallback = new Properties();
        fallback.putAll(System.getProperties());
        return fallback;
    }
}
