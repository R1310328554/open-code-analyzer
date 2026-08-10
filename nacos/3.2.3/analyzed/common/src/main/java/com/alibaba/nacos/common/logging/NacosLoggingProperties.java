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

package com.alibaba.nacos.common.logging;

import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Properties;

/**
 * Nacos Logging Properties, save some nacos logging properties.
 * <p>Nacos 日志配置属性封装：解析 {@code nacos.logging.config}、默认配置开关与热重载间隔等客户端日志相关参数。</p>
 *
 * @author xiweng.yy
 */
public class NacosLoggingProperties {
    
    /** 用户自定义 Nacos 日志配置文件路径属性键 */
    private static final String NACOS_LOGGING_CONFIG_PROPERTY = "nacos.logging.config";
    
    private static final String NACOS_LOGGING_DEFAULT_CONFIG_ENABLED_PROPERTY =
        "nacos.logging.default.config.enabled";
    
    private static final String NACOS_LOGGING_RELOAD_INTERVAL_PROPERTY =
        "nacos.logging.reload.interval.seconds";
    
    /** 日志配置热重载默认间隔（秒） */
    private static final long DEFAULT_NACOS_LOGGING_RELOAD_INTERVAL = 10L;
    
    private final String defaultLocation;
    
    private final Properties properties;
    
    public NacosLoggingProperties(String defaultLocation, Properties properties) {
        this.defaultLocation = defaultLocation;
        this.properties = null == properties ? new Properties() : properties;
    }
    
    /**
     * Get the location of nacos logging configuration.
     *
     * @return location of nacos logging configuration
      * <p>Nacos 日志配置属性；详见类级说明。</p>
     */
    public String getLocation() {
        String location = properties.getProperty(NACOS_LOGGING_CONFIG_PROPERTY);
        // 未显式配置路径时，视 default.config.enabled 决定是否回退到内置默认路径
        if (StringUtils.isBlank(location)) {
            if (isDefaultLocationEnabled()) {
                return defaultLocation;
            }
            return null;
        }
        return location;
    }
    
    /**
     * Is default location enabled.
     *
     * <p> It is judged when user don't set the location of nacos logging configuration. </p>
     *
     * @return {@code true} if default location enabled, otherwise {@code false}, default is {@code true}
      * <p>Nacos 日志配置属性；详见类级说明。</p>
     */
    private boolean isDefaultLocationEnabled() {
        String property = properties.getProperty(NACOS_LOGGING_DEFAULT_CONFIG_ENABLED_PROPERTY);
        return property == null || ConvertUtils.toBoolean(property);
    }
    
    /**
     * Get reload internal.
     * <p>获取日志配置热重载间隔（秒），未配置时使用默认 10 秒。</p>
     *
     * @return reload internal
     */
    public long getReloadInternal() {
        String interval = properties.getProperty(NACOS_LOGGING_RELOAD_INTERVAL_PROPERTY);
        return ConvertUtils.toLong(interval, DEFAULT_NACOS_LOGGING_RELOAD_INTERVAL);
    }
    
    /**
     * get property value.
     * <p>按 key 读取属性值，不存在时返回 defaultValue。</p>
     *
     * @param source       source
     * @param defaultValue defaultValue
     * @return value
     */
    public String getValue(String source, String defaultValue) {
        return properties.getProperty(source, defaultValue);
    }
}
