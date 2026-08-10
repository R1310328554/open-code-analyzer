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

package com.alibaba.nacos.k8s.sync;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.filter.NacosPackageExcludeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_NAMING;
import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_MICROSERVICE;

/**
 * K8s 同步模块 Spring 包扫描排除过滤器：按功能模式与配置开关决定是否加载本模块。
 *
 * <p>依赖 naming 模块；{@code nacos.k8s.sync.enabled=false} 时排除 {@code com.alibaba.nacos.k8s.sync} 包。</p>
 *
 * @author xiweng.yy
 */
public class K8sSyncEnabledFilter implements NacosPackageExcludeFilter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(K8sSyncEnabledFilter.class);
    
    /** 启用开关对应的配置键。 */
    private static final String K8S_SYNC_ENABLED_KEY = "nacos.k8s.sync.enabled";
    
    /** 返回本过滤器负责的包前缀。 */
    @Override
    public String getResponsiblePackagePrefix() {
        return K8sSyncEnabledFilter.class.getPackage().getName();
    }
    
    /** 判断是否排除 K8s 同步包下的类加载。 */
    @Override
    public boolean isExcluded(String className, Set<String> annotationNames) {
        String functionMode = EnvUtil.getFunctionMode();
        // 非 naming/microservice 功能模式下 naming 未启动，K8s 同步一并禁用
        if (isNamingDisabled(functionMode)) {
            LOGGER.warn(
                "K8s Sync module disabled because function mode is {}, and K8s Sync depend naming module",
                functionMode);
            return true;
        }
        boolean k8sSyncDisabled = !EnvUtil.getProperty(K8S_SYNC_ENABLED_KEY, Boolean.class, false);
        if (k8sSyncDisabled) {
            LOGGER.warn("K8s Sync module disabled because set {} as false", K8S_SYNC_ENABLED_KEY);
        }
        return k8sSyncDisabled;
    }
    
    /** 当前功能模式是否未启用 naming 模块。 */
    private boolean isNamingDisabled(String functionMode) {
        if (StringUtils.isEmpty(functionMode)) {
            return false;
        }
        return !FUNCTION_MODE_NAMING.equals(functionMode)
            && !FUNCTION_MODE_MICROSERVICE.equals(functionMode);
    }
}
