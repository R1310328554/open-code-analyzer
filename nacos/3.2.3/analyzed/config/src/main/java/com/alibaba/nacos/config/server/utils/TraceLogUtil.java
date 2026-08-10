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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置服务端链路追踪日志工具：按独立 Logger 名称区分接口请求与长轮询日志。
 * Trace util.
 *
 * @author Nacos
 */
public class TraceLogUtil {
    
    /**
     * 记录 Config Server 各接口的请求日志。
     * Record requests for each interface of the Server.
     */
    public static Logger requestLog = LoggerFactory.getLogger("com.alibaba.nacos.config.request");
    
    /**
     * 记录各客户端长轮询（polling）请求日志。
     * Record polling request records for each client.
     */
    public static Logger pollingLog = LoggerFactory.getLogger("com.alibaba.nacos.config.polling");
    
}
