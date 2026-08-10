/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.constant;

/**
 * 认证签名类型常量，标识请求所属的业务模块。
 *
 * <p>用于 {@link com.alibaba.nacos.plugin.auth.api.RequestResource} 与
 * {@link com.alibaba.nacos.plugin.auth.spi.server.AuthPluginService#enableAuth} 中的类型匹配。</p>
 *
 * @author xiweng.yy
 */
public class SignType {
    
    /** 服务发现（Naming）模块。 */
    public static final String NAMING = "naming";
    
    /** 配置管理（Config）模块。 */
    public static final String CONFIG = "config";
    
    /** 分布式锁（Lock）模块。 */
    public static final String LOCK = "lock";
    
    /** AI 能力模块。 */
    public static final String AI = "ai";
    
    /** 控制台管理模块。 */
    public static final String CONSOLE = "console";
    
    /** 显式指定资源类型。 */
    public static final String SPECIFIED = "specified";
}
