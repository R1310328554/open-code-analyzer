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

package com.alibaba.nacos.auth.config;

import com.alibaba.nacos.common.spi.NacosServiceLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link NacosAuthConfig} 的 SPI 持有者（单例）。
 *
 * <p>通过 {@link NacosServiceLoader} 加载各作用域的鉴权配置实现。</p>
 *
 * @author xiweng.yy
 */
public class NacosAuthConfigHolder {
    
    /** 单例实例。 */
    private static final NacosAuthConfigHolder INSTANCE = new NacosAuthConfigHolder();
    
    /** 按鉴权作用域索引的配置映射。 */
    private final Map<String, NacosAuthConfig> nacosAuthConfigMap;
    
    /** 通过 SPI 加载并注册所有 {@link NacosAuthConfig} 实现。 */
    NacosAuthConfigHolder() {
        this.nacosAuthConfigMap = new HashMap<>();
        for (NacosAuthConfig each : NacosServiceLoader.load(NacosAuthConfig.class)) {
            nacosAuthConfigMap.put(each.getAuthScope(), each);
        }
    }
    
    /** 返回持有者单例。 */
    public static NacosAuthConfigHolder getInstance() {
        return INSTANCE;
    }
    
    /** 按作用域获取鉴权配置。 */
    public NacosAuthConfig getNacosAuthConfigByScope(String scope) {
        return nacosAuthConfigMap.get(scope);
    }
    
    /** 返回所有已注册的鉴权配置。 */
    public Collection<NacosAuthConfig> getAllNacosAuthConfig() {
        return nacosAuthConfigMap.values();
    }
    
    /** 判断是否存在任一作用域已启用鉴权。 */
    public boolean isAnyAuthEnabled() {
        return nacosAuthConfigMap.values().stream().anyMatch(NacosAuthConfig::isAuthEnabled);
    }
    
    /**
     * 判断给定作用域列表中是否有任一启用鉴权。
     *
     * @param scope 待检查的作用域
     * @return 任一启用返回 {@code true}，全部禁用返回 {@code false}
     */
    public boolean isAnyAuthEnabled(String... scope) {
        for (String each : scope) {
            NacosAuthConfig config = nacosAuthConfigMap.get(each);
            if (null != config && config.isAuthEnabled()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 从首个 {@link NacosAuthConfig} 读取鉴权插件类型。
     *
     * <p>同一 Nacos 节点内各作用域的插件类型应保持一致。</p>
     *
     * @return 鉴权插件类型
     */
    public String getNacosAuthSystemType() {
        return nacosAuthConfigMap.values().stream().findFirst()
            .map(NacosAuthConfig::getNacosAuthSystemType)
            .orElse(null);
    }
}
