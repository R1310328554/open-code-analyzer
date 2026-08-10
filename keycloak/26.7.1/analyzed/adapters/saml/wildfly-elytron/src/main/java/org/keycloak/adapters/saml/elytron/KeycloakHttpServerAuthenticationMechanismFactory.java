/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.adapters.saml.elytron;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

import org.keycloak.adapters.saml.SamlDeploymentContext;
import org.keycloak.adapters.spi.InMemorySessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapperUpdater;

import org.wildfly.security.http.HttpAuthenticationException;
import org.wildfly.security.http.HttpServerAuthenticationMechanism;
import org.wildfly.security.http.HttpServerAuthenticationMechanismFactory;

/**
 * Keycloak SAML 的 Elytron {@link HttpServerAuthenticationMechanismFactory} 实现。
 *
 * <p>向 Elytron 注册 {@link KeycloakHttpServerAuthenticationMechanism#NAME} 机制，
 * 并创建带内存会话映射器的认证机制实例。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class KeycloakHttpServerAuthenticationMechanismFactory implements HttpServerAuthenticationMechanismFactory {

    /** 默认内存会话 ID 映射器。 */
    private final SessionIdMapper idMapper = new InMemorySessionIdMapper();
    /** 可选的 SAML 部署上下文。 */
    private final SamlDeploymentContext deploymentContext;

    /**
     * 创建工厂实例（无部署上下文）。
     *
     * <p>默认构造函数供 {@link java.util.ServiceLoader} 加载使用。</p>
     */
    public KeycloakHttpServerAuthenticationMechanismFactory() {
        this(null);
    }

    /**
     * 使用指定 SAML 部署上下文创建工厂。
     *
     * @param deploymentContext SAML 部署上下文，可为 null
     */
    public KeycloakHttpServerAuthenticationMechanismFactory(SamlDeploymentContext deploymentContext) {
        this.deploymentContext = deploymentContext;
    }

    /**
     * 返回本工厂支持的机制名称列表。
     *
     * @param properties 机制属性（未使用）
     * @return 仅包含 {@link KeycloakHttpServerAuthenticationMechanism#NAME}
     */
    @Override
    public String[] getMechanismNames(Map<String, ?> properties) {
        return new String[] {KeycloakHttpServerAuthenticationMechanism.NAME};
    }

    /**
     * 按机制名创建 {@link KeycloakHttpServerAuthenticationMechanism} 实例。
     *
     * @param mechanismName   机制名称
     * @param properties      机制属性
     * @param callbackHandler Elytron 回调处理器
     * @return 认证机制实例，名称不匹配时返回 null
     * @throws HttpAuthenticationException 创建失败时抛出
     */
    @Override
    public HttpServerAuthenticationMechanism createAuthenticationMechanism(String mechanismName, Map<String, ?> properties, CallbackHandler callbackHandler) throws HttpAuthenticationException {
        Map<String, Object> mechanismProperties = new HashMap();

        mechanismProperties.putAll(properties);

        if (KeycloakHttpServerAuthenticationMechanism.NAME.equals(mechanismName)) {
            KeycloakHttpServerAuthenticationMechanism mech = new KeycloakHttpServerAuthenticationMechanism(properties, callbackHandler, this.deploymentContext, idMapper, SessionIdMapperUpdater.DIRECT);
            return mech;
        }

        return null;
    }
}
