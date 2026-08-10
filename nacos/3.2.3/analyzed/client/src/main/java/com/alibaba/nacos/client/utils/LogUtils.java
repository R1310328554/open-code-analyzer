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

package com.alibaba.nacos.client.utils;

import com.alibaba.nacos.client.logging.NacosLogging;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Log utils.
 * <p>客户端日志工具：类加载时初始化 {@link com.alibaba.nacos.client.logging.NacosLogging} 配置，并提供命名模块专用 Logger 与按类获取 SLF4J Logger 的便捷方法。</p>
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @since 0.9.0
 */
public class LogUtils {
    
    /** 命名服务客户端统一 Logger（category: com.alibaba.nacos.client.naming） */
    public static final Logger NAMING_LOGGER;
    
    /** 加载 Nacos 日志配置并初始化命名 Logger */
    static {
        NacosLogging.getInstance().loadConfiguration();
        NAMING_LOGGER = getLogger("com.alibaba.nacos.client.naming");
    }
    
    /** 按类获取 SLF4J Logger，等价于 {@code LoggerFactory.getLogger(clazz)} */
    public static Logger logger(Class<?> clazz) {
        return getLogger(clazz);
    }
    
}
