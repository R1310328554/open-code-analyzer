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

package com.alibaba.nacos.api.naming.remote;

/**
 * 命名模块 gRPC 远程请求类型常量集合。
 *
 * <p>各常量值为请求路由标识，供客户端与服务端匹配处理器。</p>
 *
 * @author liuzunfei
 * @author xiweng.yy
 */
public class NamingRemoteConstants {
    
    /** 注册单个实例。 */
    public static final String REGISTER_INSTANCE = "registerInstance";
    
    /** 批量注册实例。 */
    public static final String BATCH_REGISTER_INSTANCE = "batchRegisterInstance";
    
    /** 注销实例。 */
    public static final String DE_REGISTER_INSTANCE = "deregisterInstance";
    
    /** 查询服务实例列表。 */
    public static final String QUERY_SERVICE = "queryService";
    
    /** 订阅服务变更。 */
    public static final String SUBSCRIBE_SERVICE = "subscribeService";
    
    /** 向订阅者推送服务变更通知。 */
    public static final String NOTIFY_SUBSCRIBER = "notifySubscriber";
    
    /** 列出命名空间下服务。 */
    public static final String LIST_SERVICE = "listService";
    
    /** 转发实例注册/变更到对端节点。 */
    public static final String FORWARD_INSTANCE = "forwardInstance";
    
    /** 转发实例心跳到对端节点。 */
    public static final String FORWARD_HEART_BEAT = "forwardHeartBeat";
}
