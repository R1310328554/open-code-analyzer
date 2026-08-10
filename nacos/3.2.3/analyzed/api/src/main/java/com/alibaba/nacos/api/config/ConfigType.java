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

package com.alibaba.nacos.api.config;

import com.alibaba.nacos.api.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Nacos 配置数据类型枚举。
 *
 * <p>标识配置内容的格式，发布与解析时用于选择对应处理器。</p>
 *
 * @author liaochuntao
 **/
public enum ConfigType {
    
    /** Properties 格式配置。 */
    PROPERTIES("properties"),
    
    /** XML 格式配置。 */
    XML("xml"),
    
    /** JSON 格式配置。 */
    JSON("json"),
    
    /** 纯文本格式配置。 */
    TEXT("text"),
    
    /** HTML 格式配置。 */
    HTML("html"),
    
    /** YAML 格式配置。 */
    YAML("yaml"),
    
    /** TOML 格式配置。 */
    TOML("toml"),
    
    /** 未指定类型，由服务端或客户端推断。 */
    UNSET("unset");
    
    private final String type;
    
    private static final Map<String, ConfigType> LOCAL_MAP = new HashMap<>();
    
    static {
        for (ConfigType configType : values()) {
            LOCAL_MAP.put(configType.getType(), configType);
        }
    }
    
    ConfigType(String type) {
        this.type = type;
    }
    
    /** 获取类型字符串标识。 */
    public String getType() {
        return type;
    }
    
    /** 获取默认配置类型（{@link #TEXT}）。 */
    public static ConfigType getDefaultType() {
        return TEXT;
    }
    
    /**
     * 校验给定字符串是否为合法配置类型。
     *
     * @param type 待校验的类型字符串
     * @return 合法返回 {@code true}，否则 {@code false}
     */
    public static Boolean isValidType(String type) {
        if (StringUtils.isBlank(type)) {
            return false;
        }
        return null != LOCAL_MAP.get(type);
    }
}
