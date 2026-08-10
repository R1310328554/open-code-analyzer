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

package com.alibaba.nacos.sys.utils;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Properties;

/**
 * Spring Environment 属性绑定工具。
 *
 * <p>基于 {@link Binder} 按前缀将配置项绑定为 {@link Properties} 或 {@code Map}，供 Nacos 各模块读取带前缀的配置块。</p>
 *
 * @author xiweng.yy
 */
public class PropertiesUtil {
    
    /** 按前缀绑定为 {@link Properties} 对象。 */
    public static Properties getPropertiesWithPrefix(Environment environment, String prefix) {
        return handleSpringBinder(environment, prefix, Properties.class);
    }
    
    /** 按前缀绑定为 Map 结构。 */
    public static Map<String, Object> getPropertiesWithPrefixForMap(Environment environment,
        String prefix) {
        return handleSpringBinder(environment, prefix, Map.class);
    }
    
    /**
     * 通用 Spring Binder 绑定入口。
     *
     * @param environment Spring 环境
     * @param prefix 配置前缀（可带或不带点后缀）
     * @param targetClass 目标绑定类型
     * @param <T> 目标类型参数
     * @return 绑定结果，无匹配时返回 null
     */
    public static <T> T handleSpringBinder(Environment environment, String prefix,
        Class<T> targetClass) {
        String prefixParam =
            prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
        return Binder.get(environment).bind(prefixParam, Bindable.of(targetClass)).orElse(null);
    }
}
