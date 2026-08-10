/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.remote;

/**
 * Nacos 远程连接元数据标签常量。
 *
 * <p>连接建立时通过 {@link com.alibaba.nacos.api.remote.request.ConnectionSetupRequest} 的 labels 传递，标识客户端来源、模块类型等，供服务端路由与监控。</p>
 *
 * @author liuzunfei
 * @version $Id: ConnectionMetaConstants.java, v 0.1 2020年08月13日 1:05 PM liuzunfei Exp $
 */
public class RemoteConstants {
    
    /** 标签键：连接来源（source）。 */
    /** 标签键名 {@code source}。 */
    public static final String LABEL_SOURCE = "source";
    
    /** 来源值：SDK 客户端。 */
    public static final String LABEL_SOURCE_SDK = "sdk";
    
    /** 来源值：集群间通信。 */
    public static final String LABEL_SOURCE_CLUSTER = "cluster";
    
    /** 标签键：业务模块（module）。 */
    public static final String LABEL_MODULE = "module";
    
    /** 模块值：配置中心。 */
    public static final String LABEL_MODULE_CONFIG = "config";
    
    /** 模块值：命名服务。 */
    public static final String LABEL_MODULE_NAMING = "naming";
    
    /** 监控标签占位：无。 */
    public static final String MONITOR_LABEL_NONE = "none";
    
    /** 模块值：分布式锁。 */
    public static final String LABEL_MODULE_LOCK = "lock";
    
    /** 模块值：AI 能力。 */
    public static final String LABEL_MODULE_AI = "ai";
}
