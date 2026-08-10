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
import com.alibaba.nacos.auth.serveridentity.ServerIdentity;
import com.alibaba.nacos.auth.serveridentity.ServerIdentityChecker;
import com.alibaba.nacos.auth.serveridentity.ServerIdentityCheckerHolder;
import com.alibaba.nacos.auth.serveridentity.ServerIdentityResult;
import com.alibaba.nacos.auth.util.Loggers;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.AuthResult;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.api.Resource;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.spi.server.AuthPluginManager;
import com.alibaba.nacos.plugin.auth.spi.server.AuthPluginService;

import java.util.Optional;
import java.util.Properties;

/**
 * 协议鉴权服务的抽象基类。
 *
 * <p>封装身份校验、权限校验与服务端身份检查等通用流程，子类仅需实现协议相关的资源/身份解析。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractProtocolAuthService<R> implements ProtocolAuthService<R> {
    
    /** Nacos 鉴权配置。 */
    protected final NacosAuthConfig authConfig;
    
    /** 服务端身份校验器。 */
    protected final ServerIdentityChecker checker;
    
    /** 注入鉴权配置并初始化服务端身份校验器。 */
    protected AbstractProtocolAuthService(NacosAuthConfig authConfig) {
        this.authConfig = authConfig;
        this.checker = ServerIdentityCheckerHolder.getInstance().newChecker();
    }
    
    /** 初始化服务端身份校验器。 */
    @Override
    public void initialize() {
        this.checker.init(authConfig);
    }
    
    /** 根据 {@link Secured} 注解判断当前插件是否对该请求启用鉴权。 */
    @Override
    public boolean enableAuth(Secured secured) {
        Optional<AuthPluginService> authPluginService = AuthPluginManager.getInstance()
            .findAuthServiceSpiImpl(authConfig.getNacosAuthSystemType());
        if (authPluginService.isPresent()) {
            return authPluginService.get().enableAuth(secured.action(), secured.signType());
        }
        Loggers.AUTH.warn(
            "Can't find auth plugin for type {}, please add plugin to classpath or set {} as false",
            authConfig.getNacosAuthSystemType(), Constants.Auth.NACOS_CORE_AUTH_ENABLED);
        return false;
    }
    
    /** 委托鉴权插件校验请求身份是否合法。 */
    @Override
    public AuthResult validateIdentity(IdentityContext identityContext, Resource resource)
        throws AccessException {
        Optional<AuthPluginService> authPluginService = AuthPluginManager.getInstance()
            .findAuthServiceSpiImpl(authConfig.getNacosAuthSystemType());
        if (authPluginService.isPresent()) {
            return authPluginService.get().validateIdentity(identityContext, resource);
        }
        return AuthResult.successResult();
    }
    
    /** 委托鉴权插件校验身份是否具备指定权限。 */
    @Override
    public AuthResult validateAuthority(IdentityContext identityContext, Permission permission)
        throws AccessException {
        Optional<AuthPluginService> authPluginService = AuthPluginManager.getInstance()
            .findAuthServiceSpiImpl(authConfig.getNacosAuthSystemType());
        if (authPluginService.isPresent()) {
            return authPluginService.get().validateAuthority(identityContext, permission);
        }
        return AuthResult.successResult();
    }
    
    /** 校验请求是否携带合法的服务端身份标识（集群间调用）。 */
    @Override
    public ServerIdentityResult checkServerIdentity(R request, Secured secured) {
        if (isInvalidServerIdentity()) {
            return ServerIdentityResult.fail(
                "Invalid server identity key or value, Please make sure set `nacos.core.auth.server.identity.key`"
                    + " and `nacos.core.auth.server.identity.value`, or open `nacos.core.auth.enable.userAgentAuthWhite`");
        }
        ServerIdentity serverIdentity = parseServerIdentity(request);
        return checker.check(serverIdentity, secured);
    }
    
    /** 判断服务端身份 key/value 配置是否缺失。 */
    private boolean isInvalidServerIdentity() {
        return StringUtils.isBlank(authConfig.getServerIdentityKey()) || StringUtils.isBlank(
            authConfig.getServerIdentityValue());
    }
    
    /**
     * 从协议请求中解析服务端身份标识。
     *
     * @param request 协议请求对象
     * @return Nacos 服务端身份
     */
    protected abstract ServerIdentity parseServerIdentity(R request);
    
    /**
     * 根据 {@link Secured#resource()} 直接构造资源对象。
     *
     * @param secured 鉴权注解
     * @return 资源实例
     */
    protected Resource parseSpecifiedResource(Secured secured) {
        Properties properties = new Properties();
        for (String each : secured.tags()) {
            properties.put(each, each);
        }
        return new Resource(null, null, secured.resource(), SignType.SPECIFIED, properties);
    }
    
    /**
     * 使用注解指定的 {@link Secured#parser()} 解析资源。
     *
     * @param secured 鉴权注解
     * @param request 协议请求
     * @return 解析结果，失败时返回 {@link Resource#EMPTY_RESOURCE}
     */
    protected Resource useSpecifiedParserToParse(Secured secured, R request) {
        try {
            return secured.parser().newInstance().parse(request, secured);
        } catch (Exception e) {
            Loggers.AUTH.error("Use specified resource parser {} parse resource failed.",
                secured.parser().getCanonicalName(), e);
            return Resource.EMPTY_RESOURCE;
        }
    }
}
