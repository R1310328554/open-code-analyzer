/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.maintainer.client.config;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.exception.NacosException;

/**
 * 配置运维维护服务：触发本地缓存刷新与动态调整模块日志级别。
 *
 * @author xiweng.yy
 */
public interface ConfigOpsMaintainerService {
    
    /**
     * 手动触发从存储层刷新本地配置缓存。
     *
     * @return A success message or error details.
     * @throws NacosException if the operation fails.
     */
    @Since("3.0.0")
    String updateLocalCacheFromStore() throws NacosException;
    
    /**
     * 设置指定模块的日志级别。
     *
     * @param logName  Name of the log module (required).
     * @param logLevel Desired log level (required).
     * @return A success message or error details.
     * @throws NacosException if the operation fails.
     */
    @Since("3.0.0")
    String setLogLevel(String logName, String logLevel) throws NacosException;
}
