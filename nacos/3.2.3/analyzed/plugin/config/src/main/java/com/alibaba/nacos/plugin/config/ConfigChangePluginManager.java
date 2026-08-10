/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.config;

import com.alibaba.nacos.api.plugin.PluginStateChecker;
import com.alibaba.nacos.api.plugin.PluginStateCheckerHolder;
import com.alibaba.nacos.api.plugin.PluginType;
import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.config.constants.ConfigChangePointCutTypes;
import com.alibaba.nacos.plugin.config.spi.ConfigChangePluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Comparator;

/**
 * 配置变更插件统一管理器。
 *
 * <p>通过 SPI 加载所有 {@link ConfigChangePluginService} 实现，按服务类型与切点类型建立索引，
 * 并在同一切点下按 {@link ConfigChangePluginService#getOrder()} 排序，供配置变更流程按序调用。</p>
 *
 * @author liyunfei
 */
public class ConfigChangePluginManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigChangePluginManager.class);
    
    /** 插件服务 Map 初始容量，与预期插件数量一致。 */
    private static final Integer PLUGIN_SERVICE_COUNT = 4;
    
    /** 切点类型数量，用于初始化切点索引 Map 容量。 */
    private static final Integer POINT_CUT_TYPE_COUNT = ConfigChangePointCutTypes.values().length;
    
    /**
     * 服务类型与 {@link ConfigChangePluginService} 的映射，默认容量为插件服务数量。
     */
    private static final Map<String, ConfigChangePluginService> CONFIG_CHANGE_PLUGIN_SERVICE_MAP =
        new ConcurrentHashMap<>(
            PLUGIN_SERVICE_COUNT);
    
    /**
     * 配置变更切点类型与对应 {@link ConfigChangePluginService} 列表的映射，默认容量为切点类型数量。
     */
    private static final Map<ConfigChangePointCutTypes, List<ConfigChangePluginService>> CONFIG_CHANGE_PLUGIN_SERVICES_MAP =
        new ConcurrentHashMap<>(
            POINT_CUT_TYPE_COUNT);
    
    /** 单例实例。 */
    private static final ConfigChangePluginManager INSTANCE = new ConfigChangePluginManager();
    
    private ConfigChangePluginManager() {
        loadConfigChangeServices();
    }
    
    /**
     * 通过 SPI 加载所有配置变更插件服务。
     */
    private static void loadConfigChangeServices() {
        Collection<ConfigChangePluginService> configChangePluginServices = NacosServiceLoader
            .load(ConfigChangePluginService.class);
        // 通过 SPI 加载全部配置变更插件
        for (ConfigChangePluginService each : configChangePluginServices) {
            if (StringUtils.isEmpty(each.getServiceType())) {
                LOGGER.warn(
                    "[ConfigChangePluginManager] Load {}({}) ConfigChangeServiceName(null/empty) fail. "
                        + "Please Add the Plugin Service ConfigChangeServiceName to resolve.",
                    each.getClass().getName(), each.getClass());
                continue;
            }
            CONFIG_CHANGE_PLUGIN_SERVICE_MAP.put(each.getServiceType(), each);
            LOGGER.info(
                "[ConfigChangePluginManager] Load {}({}) ConfigChangeServiceName({}) successfully.",
                each.getClass().getName(), each.getClass(), each.getServiceType());
            // 建立切点与插件服务的映射关系
            addPluginServiceByPointCut(each);
        }
        // 按 order 对各切点下的插件服务排序
        sortPluginServiceByPointCut();
    }
    
    /**
     * 获取管理器单例。
     *
     * @return 配置变更插件管理器实例
     */
    public static ConfigChangePluginManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 按服务类型动态查找插件实现。
     *
     * <p>若插件状态检查器存在且该插件已被禁用，则返回空。</p>
     *
     * @param serviceType 插件服务类型
     * @return 对应的插件服务，未找到或已禁用时为空
     */
    public Optional<ConfigChangePluginService> findPluginServiceImpl(String serviceType) {
        Optional<PluginStateChecker> checker = PluginStateCheckerHolder.getInstance();
        if (checker.isPresent()
            && !checker.get().isPluginEnabled(PluginType.CONFIG_CHANGE.getType(), serviceType)) {
            LOGGER.debug("[ConfigChangePluginManager] Plugin CONFIG_CHANGE:{} is disabled",
                serviceType);
            return Optional.empty();
        }
        return Optional.ofNullable(CONFIG_CHANGE_PLUGIN_SERVICE_MAP.get(serviceType));
    }
    
    /**
     * 动态注册新的配置变更插件服务。
     *
     * @param configChangePluginService 待加入的插件服务
     * @return 始终为 {@code true}
     */
    public static synchronized boolean join(ConfigChangePluginService configChangePluginService) {
        CONFIG_CHANGE_PLUGIN_SERVICE_MAP
            .putIfAbsent(configChangePluginService.getServiceType(), configChangePluginService);
        addPluginServiceByPointCut(configChangePluginService);
        return true;
    }
    
    /**
     * 获取指定切点下的插件服务执行队列。
     *
     * @param pointcutName 切点方法名，详见 {@link ConfigChangePointCutTypes}
     * @return 该切点对应的插件服务列表，无则返回空列表
     */
    public static List<ConfigChangePluginService> findPluginServicesByPointcut(
        ConfigChangePointCutTypes pointcutName) {
        return CONFIG_CHANGE_PLUGIN_SERVICES_MAP.getOrDefault(pointcutName, new ArrayList<>());
    }
    
    /** 将插件服务注册到其声明的各个切点索引中。 */
    private static void addPluginServiceByPointCut(
        ConfigChangePluginService configChangePluginService) {
        ConfigChangePointCutTypes[] pointcutNames = configChangePluginService.pointcutMethodNames();
        for (ConfigChangePointCutTypes name : pointcutNames) {
            List<ConfigChangePluginService> configChangePluginServiceList =
                CONFIG_CHANGE_PLUGIN_SERVICES_MAP
                    .get(name);
            if (configChangePluginServiceList == null) {
                configChangePluginServiceList = new ArrayList<>(PLUGIN_SERVICE_COUNT);
            }
            configChangePluginServiceList.add(configChangePluginService);
            CONFIG_CHANGE_PLUGIN_SERVICES_MAP.put(name, configChangePluginServiceList);
        }
    }
    
    /** 按 order 值对各切点下的插件服务列表升序排序。 */
    private static void sortPluginServiceByPointCut() {
        CONFIG_CHANGE_PLUGIN_SERVICES_MAP.forEach((type, pluginServices) -> {
            List<ConfigChangePluginService> sortedList = new ArrayList<>(pluginServices);
            sortedList.sort(Comparator.comparingInt(ConfigChangePluginService::getOrder));
            CONFIG_CHANGE_PLUGIN_SERVICES_MAP.put(type, sortedList);
        });
    }
    
    /** 清空所有插件索引，仅用于测试。 */
    @JustForTest
    public static synchronized void reset() {
        CONFIG_CHANGE_PLUGIN_SERVICE_MAP.clear();
        CONFIG_CHANGE_PLUGIN_SERVICES_MAP.clear();
    }
    
    /**
     * 获取全部已加载的配置变更插件服务。
     *
     * @return 不可修改的服务类型到插件服务的映射
     */
    public Map<String, ConfigChangePluginService> getAllPlugins() {
        return Collections.unmodifiableMap(CONFIG_CHANGE_PLUGIN_SERVICE_MAP);
    }
}
