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

package com.alibaba.nacos.client.auth.impl;

/**
 * Nacos auth login constants.
 * <p>Nacos 客户端登录/鉴权 JSON 与请求体字段名常量：与 Server 登录接口响应及 RAM 插件约定一致。</p>
 *
 * @author Nacos
 */
public class NacosAuthLoginConstant {
    
    /** 登录响应中的访问令牌字段名 */
    public static final String ACCESSTOKEN = "accessToken";
    
    /** 令牌有效时长（秒）字段名 */
    public static final String TOKENTTL = "tokenTtl";
    
    /** 令牌刷新窗口字段名 */
    public static final String TOKENREFRESHWINDOW = "tokenRefreshWindow";
    
    /** 登录用户名参数名 */
    public static final String USERNAME = "username";
    
    /** 登录密码参数名 */
    public static final String PASSWORD = "password";
    
    /** 用户名密码拼接分隔符 */
    public static final String COLON = ":";
    
    /** 目标 Nacos Server 标识字段名 */
    public static final String SERVER = "server";
    
    /** 是否触发重新登录的标志字段名 */
    public static final String RELOGINFLAG = "reLoginFlag";
}
