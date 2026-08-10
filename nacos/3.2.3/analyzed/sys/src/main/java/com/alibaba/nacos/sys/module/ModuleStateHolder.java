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

package com.alibaba.nacos.sys.module;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 全局模块状态持有者（单例）。
 *
 * <p>启动时通过 SPI 加载全部 {@link ModuleStateBuilder}，按部署类型过滤后构建并缓存 {@link ModuleState}；对非缓存构建器在每次查询时重建。</p>
 *
 * @author xiweng.yy
 */
public class ModuleStateHolder {
    
    /** 模块状态构建失败时的告警日志。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleStateHolder.class);
    
    /** 懒加载单例实例。 */
    private static final ModuleStateHolder INSTANCE = new ModuleStateHolder();
    
    /** 模块名到状态快照的映射表。 */
    private final Map<String, ModuleState> moduleStates;
    
    /** 已注册且通过部署过滤的构建器列表。 */
    private final List<ModuleStateBuilder> moduleStateBuilders = new ArrayList<>();
    
    /** 私有构造：SPI 加载并初始化全部模块状态。 */
    private ModuleStateHolder() {
        this.moduleStates = new HashMap<>();
        for (ModuleStateBuilder each : NacosServiceLoader.load(ModuleStateBuilder.class)) {
            if (each.isIgnore()) {
                continue;
            }
            if (!each.isMatchDeployment(EnvUtil.getDeploymentType())) {
                continue;
            }
            try {
                moduleStateBuilders.add(each);
                ModuleState moduleState = each.build();
                moduleStates.put(moduleState.getModuleName(), moduleState);
            } catch (Exception e) {
                LOGGER.warn("Build ModuleState failed in builder:{}",
                    each.getClass().getCanonicalName(), e);
            }
        }
    }
    
    /** 获取全局单例。 */
    public static ModuleStateHolder getInstance() {
        return INSTANCE;
    }
    
    /** 对标记为非缓存的构建器重新构建状态。 */
    private void reBuildModuleState() {
        for (ModuleStateBuilder each : moduleStateBuilders) {
            if (each.isCacheable()) {
                continue;
            }
            try {
                ModuleState moduleState = each.build();
                moduleStates.put(moduleState.getModuleName(), moduleState);
            } catch (Exception e) {
                LOGGER.warn("reBuild ModuleState failed in builder:{}",
                    each.getClass().getCanonicalName(), e);
            }
        }
        
    }
    
    /** 按模块名查询状态，不存在时返回空 Optional。 */
    public Optional<ModuleState> getModuleState(String moduleName) {
        reBuildModuleState();
        return Optional.ofNullable(moduleStates.get(moduleName));
    }
    
    /** 返回全部模块状态的副本集合。 */
    public Set<ModuleState> getAllModuleStates() {
        reBuildModuleState();
        return new HashSet<>(moduleStates.values());
    }
    
    /** 按模块名与状态名取值，缺失时返回空字符串。 */
    public String getStateValueByName(String moduleName, String stateName) {
        return getStateValueByName(moduleName, stateName, StringUtils.EMPTY);
    }
    
    /**
     * 按模块名与状态名取值，支持泛型默认值。
     *
     * @param moduleName 模块名
     * @param stateName 状态项名
     * @param defaultValue 模块或状态不存在时的默认值
     * @return 状态值
     */
    public <T> T getStateValueByName(String moduleName, String stateName, T defaultValue) {
        Optional<ModuleState> moduleState = getModuleState(moduleName);
        if (!moduleState.isPresent()) {
            return defaultValue;
        }
        return moduleState.get().getState(stateName, defaultValue);
    }
    
    /**
     * 跨模块按状态名线性搜索首个匹配值。
     *
     * @param stateName 状态项名
     * @param defaultValue 未找到时的默认值
     * @return 状态值
     */
    @SuppressWarnings("all")
    public <T> T searchStateValue(String stateName, T defaultValue) {
        T result = null;
        for (ModuleState each : getAllModuleStates()) {
            if (each.getStates().containsKey(stateName)) {
                result = (T) each.getStates().get(stateName);
                break;
            }
        }
        return null == result ? defaultValue : result;
    }
}
