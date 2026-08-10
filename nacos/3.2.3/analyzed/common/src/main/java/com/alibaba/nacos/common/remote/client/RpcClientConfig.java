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

package com.alibaba.nacos.common.remote.client;

import java.util.Map;

/**
 * RPC 客户端运行时配置接口：定义客户端名称、重试次数、超时、保活、
 * 健康检查及标签等参数，由 gRPC 等具体实现提供。
 * RpcClientConfig.
 *
 * @author karsonto
 */
public interface RpcClientConfig {
    
    /** 获取客户端唯一名称，用于日志与工厂索引 */

    String name();
    
    /** 请求失败时的最大重试次数 */

    int retryTimes();
    
    /** 单次 RPC 请求超时时间（毫秒） */

    long timeOutMills();
    
    /** 连接保活检测间隔（毫秒），超时未活动则触发健康检查 */

    long connectionKeepAlive();
    
    /** 健康检查失败时的重试次数 */

    int healthCheckRetryTimes();
    
    /** 单次健康检查请求超时（毫秒） */

    long healthCheckTimeOut();
    
    /** 客户端标签键值对，随连接上报供服务端识别来源模块 */

    Map<String, String> labels();
    
}
