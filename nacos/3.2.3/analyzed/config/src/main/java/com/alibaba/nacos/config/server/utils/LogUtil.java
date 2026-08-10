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

package com.alibaba.nacos.config.server.utils;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置模块日志门面：集中暴露启动、致命、拉取、Dump、追踪、通知等专用 Logger，并提供运行时动态调整 Logback 级别的方法。
 * Log util.
 *
 * @author Nacos
 */
public class LogUtil {
    
    /**
     * 配置服务启动与常规日志。
     * Default log.
     */
    public static final Logger DEFAULT_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.config.startLog");
    
    /**
     * 致命错误日志，需触发告警。
     * Fatal error log, require alarm.
     */
    public static final Logger FATAL_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.config.fatal");
    
    /**
     * 客户端 HTTP 拉取日志。
     * Http client log.
     */
    public static final Logger PULL_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.config.pullLog");
    
    /** 拉取校验专用日志 */
    public static final Logger PULL_CHECK_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.config.pullCheckLog");
    
    /**
     * 本地缓存 Dump 日志。
     * Dump log.
     */
    public static final Logger DUMP_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.config.dumpLog");
    
    /** 内存与监控指标日志 */
    public static final Logger MEMORY_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.config.monitorLog");
    
    /** 客户端请求日志 */
    public static final Logger CLIENT_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.config.clientLog");
    
    /** 全链路 trace 日志，供 {@link com.alibaba.nacos.config.server.service.trace.ConfigTraceService} 写入 */
    public static final Logger TRACE_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.config.traceLog");
    
    /** 长轮询/推送通知日志 */
    public static final Logger NOTIFY_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.config.notifyLog");
    
    /** 按 logName 别名动态设置对应 Logger 的 Logback 级别 */
    public static void setLogLevel(String logName, String level) {
        
        switch (logName) {
            case "config-server":
                ((ch.qos.logback.classic.Logger) DEFAULT_LOG).setLevel(Level.valueOf(level));
                break;
            case "config-fatal":
                ((ch.qos.logback.classic.Logger) FATAL_LOG).setLevel(Level.valueOf(level));
                break;
            case "config-pull":
                ((ch.qos.logback.classic.Logger) PULL_LOG).setLevel(Level.valueOf(level));
                break;
            case "config-pull-check":
                ((ch.qos.logback.classic.Logger) PULL_CHECK_LOG).setLevel(Level.valueOf(level));
                break;
            case "config-dump":
                ((ch.qos.logback.classic.Logger) DUMP_LOG).setLevel(Level.valueOf(level));
                break;
            case "config-memory":
                ((ch.qos.logback.classic.Logger) MEMORY_LOG).setLevel(Level.valueOf(level));
                break;
            case "config-client-request":
                ((ch.qos.logback.classic.Logger) CLIENT_LOG).setLevel(Level.valueOf(level));
                break;
            case "config-trace":
                ((ch.qos.logback.classic.Logger) TRACE_LOG).setLevel(Level.valueOf(level));
                break;
            case "config-notify":
                ((ch.qos.logback.classic.Logger) NOTIFY_LOG).setLevel(Level.valueOf(level));
                break;
            default:
                break;
        }
        
    }
    
}
