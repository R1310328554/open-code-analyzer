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

package com.alibaba.nacos.core.utils;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core 模块 SLF4J 日志记录器常量，按子域划分 logger 名称。
 *
 * @author nkorange
 * @since 1.2.0
 */
public class Loggers {
    
    /** 认证相关日志。 */
    public static final Logger AUTH = LoggerFactory.getLogger("com.alibaba.nacos.core.auth");
    
    /** Core 通用日志。 */
    public static final Logger CORE = LoggerFactory.getLogger("com.alibaba.nacos.core");
    
    /** Raft 一致性协议日志。 */
    public static final Logger RAFT =
        LoggerFactory.getLogger("com.alibaba.nacos.core.protocol.raft");
    
    /** Distro 协议日志。 */
    public static final Logger DISTRO =
        LoggerFactory.getLogger("com.alibaba.nacos.core.protocol.distro");
    
    /** 集群成员与寻址日志。 */
    public static final Logger CLUSTER = LoggerFactory.getLogger("com.alibaba.nacos.core.cluster");
    
    /** 远程 RPC 通用日志。 */
    public static final Logger REMOTE = LoggerFactory.getLogger("com.alibaba.nacos.core.remote");
    
    /** 远程推送日志。 */
    public static final Logger REMOTE_PUSH =
        LoggerFactory.getLogger("com.alibaba.nacos.core.remote.push");
    
    /** 远程 RPC 摘要/审计日志。 */
    public static final Logger REMOTE_DIGEST =
        LoggerFactory.getLogger("com.alibaba.nacos.core.remote.digest");
    
    /**
     * 运行时调整指定 Core 子域日志级别（Logback）。
     *
     * @param logName 日志别名，如 {@code core-auth}、{@code core-raft}
     * @param level   Logback {@link Level} 名称
     */
        
        switch (logName) {
            case "core-auth":
                ((ch.qos.logback.classic.Logger) AUTH).setLevel(Level.valueOf(level));
                break;
            case "core":
                ((ch.qos.logback.classic.Logger) CORE).setLevel(Level.valueOf(level));
                break;
            case "core-raft":
                ((ch.qos.logback.classic.Logger) RAFT).setLevel(Level.valueOf(level));
                break;
            case "core-distro":
                ((ch.qos.logback.classic.Logger) DISTRO).setLevel(Level.valueOf(level));
                break;
            case "core-cluster":
                ((ch.qos.logback.classic.Logger) CLUSTER).setLevel(Level.valueOf(level));
                break;
            default:
                break;
        }
        
    }
}
