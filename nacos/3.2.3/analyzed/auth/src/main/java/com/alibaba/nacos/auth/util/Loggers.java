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

package com.alibaba.nacos.auth.util;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nacos 鉴权模块日志记录器集合。
 *
 * <p>提供 {@link #AUTH} 静态 Logger，并支持运行时动态调整鉴权日志级别。</p>
 *
 * @author nkorange
 * @since 1.2.0
 */
public class Loggers {
    
    /** 鉴权日志类别标识，对应 Logback 日志名 {@code auth}。 */
    private static final String AUTH_LOG_NAME = "auth";
    
    /** 鉴权模块 SLF4J 日志记录器，名称为 {@code com.alibaba.nacos.auth}。 */
    public static final Logger AUTH = LoggerFactory.getLogger("com.alibaba.nacos.auth");
    
    /**
     * 按日志名动态设置 Logback 日志级别。
     *
     * @param logName 日志类别，当前仅支持 {@code auth}
     * @param level Logback 级别字符串，如 {@code DEBUG}、{@code INFO}
     */
    public static void setLogLevel(String logName, String level) {
        
        if (AUTH_LOG_NAME.equals(logName)) {
            ((ch.qos.logback.classic.Logger) AUTH).setLevel(Level.valueOf(level));
        }
    }
}
