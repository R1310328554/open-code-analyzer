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

package com.alibaba.nacos.auth;

import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.auth.serveridentity.ServerIdentityResult;
import com.alibaba.nacos.plugin.auth.api.AuthResult;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.api.Resource;
import com.alibaba.nacos.plugin.auth.exception.AccessException;

/**
 * 协议层鉴权服务 SPI 接口。
 *
 * <p>定义 HTTP/gRPC 等协议在鉴权流程中的统一契约：资源解析、身份提取与权限校验。</p>
 *
 * @author xiweng.yy
 */
public interface ProtocolAuthService<R> {
    
    /** 初始化协议鉴权服务（注册解析器等）。 */
    void initialize();
    
    /**
     * 根据 {@link Secured} 信息判断是否对该请求启用鉴权。
     * <p>
     *     configuration authEnabled in {@link NacosAuthConfig} is the main switch.
     *     If authEnabled is {@code false}, this method and other follow methods should not be called.
     *
     *     This method is only for plugin to judge whether auth this {@link Secured}.
     *     For example, plugins can only auth for write action or only for naming type request.
     * </p>
     *
     * @param secured 鉴权注解信息
     * @return 启用鉴权返回 {@code true}，否则 {@code false}
     */
    boolean enableAuth(Secured secured);
    
    /**
     * 从协议请求与 {@link Secured} 注解解析鉴权资源。
     *
     * @param request 协议请求
     * @param secured API 鉴权注解
     * @return 资源对象
     */
    Resource parseResource(R request, Secured secured);
    
    /**
     * 从协议请求提取身份上下文。
     *
     * @param request 协议请求
     * @return 身份上下文
     */
    IdentityContext parseIdentity(R request);
    
    /**
     * 校验身份是否合法。
     *
     * @param identityContext 身份上下文
     * @param resource        目标资源
     * @return 校验结果 {@link AuthResult}
     * @throws AccessException 校验过程异常
     */
    AuthResult validateIdentity(IdentityContext identityContext, Resource resource)
        throws AccessException;
    
    /**
     * 校验身份是否具备对资源的操作权限。
     *
     * @param identityContext 身份上下文
     * @param permission      含资源与动作的权限对象
     * @return 校验结果 {@link AuthResult}
     * @throws AccessException 校验过程异常
     */
    AuthResult validateAuthority(IdentityContext identityContext, Permission permission)
        throws AccessException;
    
    /**
     * 校验集群间请求的服务端身份标识。
     *
     * @param request 协议请求
     * @param secured API 鉴权注解
     * @return 服务端身份校验结果
     */
    ServerIdentityResult checkServerIdentity(R request, Secured secured);
}
