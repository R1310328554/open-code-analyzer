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

package com.alibaba.nacos.common.trace;

/**
 * 实例注销原因枚举：标识 Nacos Naming 模块将实例从注册表移除的触发来源，
 * 供 {@link com.alibaba.nacos.common.trace.event.naming.DeregisterInstanceTraceEvent} 追踪审计。
 * The reasons of deregister instance.
 *
 * @author yanda
 */
public enum DeregisterInstanceReason {
    /** 客户端主动发起注销请求 */
    /**
     * client initiates request.
      * <p>实例注销原因；详见类级说明。</p>
     */
    REQUEST,
    /** 实例在本节点检测到连接断开（本地感知） */
    /**
     * Instance native disconnected.
      * <p>实例注销原因；详见类级说明。</p>
     */
    NATIVE_DISCONNECTED,
    /** 实例经集群同步得知已断开（远端同步） */
    /**
     * Instance synced disconnected.
      * <p>实例注销原因；详见类级说明。</p>
     */
    SYNCED_DISCONNECTED,
    /** 实例心跳超时，被服务端判定为不健康并注销 */
    /**
     * Instance heart beat timeout expire.
      * <p>实例注销原因；详见类级说明。</p>
     */
    HEARTBEAT_EXPIRE,
    
}
