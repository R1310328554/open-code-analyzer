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

package com.alibaba.nacos.naming.misc;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Naming 模块日志 Logger 集中定义。
 *
 * <p>按功能域划分推送、健康检查 RT、主流程、事件、Raft、Distro、性能等独立 Logger，支持运行时动态调整级别。</p>
 *
 * @author nacos
 */
public class Loggers {
    
    /** 实例推送相关日志。 */
    public static final Logger PUSH = LoggerFactory.getLogger("com.alibaba.nacos.naming.push");
    
    /** 健康检查 RT 耗时日志。 */
    public static final Logger CHECK_RT = LoggerFactory.getLogger("com.alibaba.nacos.naming.rt");
    
    /** Naming 主流程日志。 */
    public static final Logger SRV_LOG = LoggerFactory.getLogger("com.alibaba.nacos.naming.main");
    
    /** 服务变更事件日志。 */
    public static final Logger EVT_LOG = LoggerFactory.getLogger("com.alibaba.nacos.naming.event");
    
    /** Raft 一致性协议日志。 */
    public static final Logger RAFT = LoggerFactory.getLogger("com.alibaba.nacos.naming.raft");
    
    /** Distro 协议同步日志。 */
    public static final Logger DISTRO = LoggerFactory.getLogger("com.alibaba.nacos.naming.distro");
    
    /** 服务端性能指标日志。 */
    public static final Logger PERFORMANCE_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.naming.performance");
    
    /** 按别名动态设置指定 Logger 的日志级别。 */
    public static void setLogLevel(String logName, String level) {
        
        switch (logName) {
            case "naming-push":
                ((ch.qos.logback.classic.Logger) PUSH).setLevel(Level.valueOf(level));
                break;
            case "naming-rt":
                ((ch.qos.logback.classic.Logger) CHECK_RT).setLevel(Level.valueOf(level));
                break;
            case "naming-server":
                ((ch.qos.logback.classic.Logger) SRV_LOG).setLevel(Level.valueOf(level));
                break;
            case "naming-event":
                ((ch.qos.logback.classic.Logger) EVT_LOG).setLevel(Level.valueOf(level));
                break;
            case "naming-raft":
                ((ch.qos.logback.classic.Logger) RAFT).setLevel(Level.valueOf(level));
                break;
            case "naming-distro":
                ((ch.qos.logback.classic.Logger) DISTRO).setLevel(Level.valueOf(level));
                break;
            case "naming-performance":
                ((ch.qos.logback.classic.Logger) PERFORMANCE_LOG).setLevel(Level.valueOf(level));
                break;
            default:
                break;
        }
        
    }
}
