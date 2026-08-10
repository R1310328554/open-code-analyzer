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

package com.alibaba.nacos.plugin.environment;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.environment.spi.CustomEnvironmentPluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义环境变量插件管理器。
 *
 * <p>通过 SPI 加载 {@link CustomEnvironmentPluginService} 实现，按 {@code order} 排序后
 * 依次合并各插件对配置项的自定义值。</p>
 *
 * @author : huangtianhui
 */
public class CustomEnvironmentPluginManager {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(CustomEnvironmentPluginManager.class);
    
    /** 已加载的环境插件服务列表（按优先级排序）。 */
    private static final List<CustomEnvironmentPluginService> SERVICE_LIST = new LinkedList<>();
    
    /** 单例实例。 */
    private static final CustomEnvironmentPluginManager INSTANCE =
        new CustomEnvironmentPluginManager();
    
    public CustomEnvironmentPluginManager() {
        loadInitial();
    }
    
    /**
     * 初始化加载所有 SPI 注册的环境插件。
     */
    private void loadInitial() {
        Collection<CustomEnvironmentPluginService> customEnvironmentPluginServices =
            NacosServiceLoader.load(
                CustomEnvironmentPluginService.class);
        for (CustomEnvironmentPluginService customEnvironmentPluginService : customEnvironmentPluginServices) {
            if (StringUtils.isBlank(customEnvironmentPluginService.pluginName())) {
                LOGGER.warn(
                    "[customEnvironmentPluginService] Load customEnvironmentPluginService({}) customEnvironmentPluginName(null/empty) fail."
                        + " Please Add customEnvironmentPluginName to resolve.",
                    customEnvironmentPluginService.getClass());
                continue;
            }
            LOGGER.info(
                "[CustomEnvironmentPluginManager] Load customEnvironmentPluginService({}) customEnvironmentPluginName({}) successfully.",
                customEnvironmentPluginService.getClass(),
                customEnvironmentPluginService.pluginName());
        }
        SERVICE_LIST.addAll(customEnvironmentPluginServices.stream()
            .sorted(Comparator.comparingInt(CustomEnvironmentPluginService::order))
            .collect(Collectors.toList()));
    }
    
    /**
     * 获取管理器单例。
     *
     * @return 单例实例
     */
    public static CustomEnvironmentPluginManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 汇总所有插件声明的配置键名。
     *
     * @return 配置键名集合
     */
    public Set<String> getPropertyKeys() {
        Set<String> keys = new HashSet<>();
        for (CustomEnvironmentPluginService customEnvironmentPluginService : SERVICE_LIST) {
            keys.addAll(customEnvironmentPluginService.propertyKey());
        }
        return keys;
    }
    
    /**
     * 按插件顺序计算自定义配置值。
     *
     * <p>每个插件仅允许修改自身声明的配置键，值为 {@code null} 的项会被过滤。</p>
     *
     * @param sourceProperty 原始配置键值
     * @return 合并后的自定义配置
     */
    public Map<String, Object> getCustomValues(Map<String, Object> sourceProperty) {
        Map<String, Object> customValuesMap = new HashMap<>(1);
        for (CustomEnvironmentPluginService customEnvironmentPluginService : SERVICE_LIST) {
            Set<String> keys = customEnvironmentPluginService.propertyKey();
            Map<String, Object> propertyMap = new HashMap<>(keys.size());
            for (String key : keys) {
                propertyMap.put(key, sourceProperty.get(key));
            }
            Map<String, Object> targetPropertyMap =
                customEnvironmentPluginService.customValue(propertyMap);
            // 仅允许修改当前插件声明的配置键
            Set<String> targetKeys = new HashSet<>(targetPropertyMap.keySet());
            targetKeys.removeAll(keys);
            for (String key : targetKeys) {
                targetPropertyMap.remove(key);
            }
            customValuesMap.putAll(targetPropertyMap);
        }
        // [issue 13367] 修复 ConcurrentModificationException
        customValuesMap.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));
        return customValuesMap;
    }
    
    /**
     * 运行时注入环境插件实现。
     *
     * @param customEnvironmentPluginService customEnvironmentPluginService implementation
     */
    public static synchronized void join(
        CustomEnvironmentPluginService customEnvironmentPluginService) {
        if (Objects.isNull(customEnvironmentPluginService)) {
            return;
        }
        SERVICE_LIST.add(customEnvironmentPluginService);
        LOGGER.info("[CustomEnvironmentPluginService] join successfully.");
    }
}
