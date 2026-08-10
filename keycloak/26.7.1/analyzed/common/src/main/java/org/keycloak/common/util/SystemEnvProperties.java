/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common.util;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * <p>基于运行时环境变量与系统属性解析占位符键值的 {@link Properties} 实现。
 * 通常不应解析任意系统变量，而应显式限定允许解析的键集合。</p>
 *
 * <p>环境变量键格式为 {@code env.<key>}，其中 {@code key} 为环境变量名；
 * 系统属性则直接使用键名匹配 {@link System#getProperty(String)}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class SystemEnvProperties extends Properties {

    /**
     * <p>{@link SystemEnvProperties} 的无过滤变体，允许解析任意运行时系统/环境变量。
     * 多数场景应使用带白名单的 {@link SystemEnvProperties} 构造器而非本常量。</p>
     */
    public static final SystemEnvProperties UNFILTERED = new SystemEnvProperties(Collections.emptySet()) {
        @Override
        protected boolean isAllowed(String key) {
            return true;
        }
    };

    /** 允许解析的系统/环境变量键白名单。 */
    private final Set<String> allowedSystemVariables;

    /**
     * 创建仅允许解析指定键的系统/环境变量实例。
     *
     * @param allowedSystemVariables the keys of system variables that should be available at runtime
     */
    public SystemEnvProperties(Set<String> allowedSystemVariables) {
        this.allowedSystemVariables = Optional.ofNullable(allowedSystemVariables).orElse(Collections.emptySet());
    }

    @Override
    public String getProperty(String key) {
        if (key.startsWith("env.")) {
            String envKey = key.substring(4);
            return isAllowed(envKey) ? System.getenv().get(envKey) : null;
        } else {
            return isAllowed(key) ? System.getProperty(key) : null;
        }
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /** 判断给定键是否在白名单内。 */
    protected boolean isAllowed(String key) {
        return allowedSystemVariables.contains(key);
    }
}
