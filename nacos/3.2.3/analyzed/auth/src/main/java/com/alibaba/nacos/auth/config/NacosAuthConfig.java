/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.auth.config;

/**
 * Nacos 鉴权配置接口。
 *
 * <p>抽象服务端/控制台等场景的鉴权开关、插件类型与服务端身份配置。</p>
 *
 * @author xiweng.yy
 */
public interface NacosAuthConfig {
    
    /**
     * 获取鉴权作用域（如 server、console 等）。
     *
     * @return 鉴权作用域标识
     */
    String getAuthScope();
    
    /**
     * 当前作用域是否启用鉴权。
     *
     * @return 启用返回 {@code true}，否则 {@code false}
     */
    boolean isAuthEnabled();
    
    /**
     * 获取当前鉴权插件类型。
     *
     * @return 插件类型标识
     */
    String getNacosAuthSystemType();
    
    /**
     * 是否支持服务端身份标识（用于集群节点间互信）。
     *
     * @return 支持返回 {@code true}，否则 {@code false}
     */
    boolean isSupportServerIdentity();
    
    /**
     * 获取服务端身份 HTTP 头键名。
     *
     * @return 启用服务端身份时返回 key，否则空串
     */
    String getServerIdentityKey();
    
    /**
     * 获取服务端身份期望值。
     *
     * @return 启用服务端身份时返回 value，否则空串
     */
    String getServerIdentityValue();
}
