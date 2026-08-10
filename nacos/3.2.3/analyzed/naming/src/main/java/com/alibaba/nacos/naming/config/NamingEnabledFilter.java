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

package com.alibaba.nacos.naming.config;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.naming.NamingApp;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.filter.NacosPackageExcludeFilter;

import java.util.Set;

import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_AI;
import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_NAMING;
import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_MICROSERVICE;

/**
 * 按 Spring 包扫描控制 Naming 模块是否启用的过滤器。
 *
 * <p>当 {@link EnvUtil#getFunctionMode()} 指定为 naming、microservice 或 ai 模式时加载 Naming 包；否则排除 Naming 相关 Bean。</p>
 *
 * @author xiweng.yy
 */
public class NamingEnabledFilter implements NacosPackageExcludeFilter {
    
    /** 返回本过滤器负责的包前缀（{@link NamingApp} 所在包）。 */
    @Override
    public String getResponsiblePackagePrefix() {
        return NamingApp.class.getPackage().getName();
    }
    
    /**
     * 判断给定类是否应从组件扫描中排除。
     *
     * @param className        全限定类名
     * @param annotationNames  类上注解名集合
     * @return 应排除返回 {@code true}
     */
        String functionMode = EnvUtil.getFunctionMode();
        // 未指定功能模式时默认加载 Naming；指定 naming/microservice/ai 模式时也加载。
        if (StringUtils.isEmpty(functionMode)) {
            return false;
        }
        return !FUNCTION_MODE_NAMING.equals(functionMode)
            && !FUNCTION_MODE_MICROSERVICE.equals(functionMode)
            && !FUNCTION_MODE_AI.equals(functionMode);
    }
}
