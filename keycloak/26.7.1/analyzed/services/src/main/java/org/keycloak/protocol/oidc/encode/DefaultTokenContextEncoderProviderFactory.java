/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.protocol.oidc.encode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oidc.grants.OAuth2GrantType;
import org.keycloak.protocol.oidc.grants.OAuth2GrantTypeFactory;

/**
 * 默认令牌上下文编码器工厂：维护会话/令牌/授权类型的快捷码映射。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultTokenContextEncoderProviderFactory implements TokenContextEncoderProviderFactory {

    private KeycloakSessionFactory sessionFactory;
    private Map<String, AccessTokenContext.SessionType> sessionTypesByShortcut;
    private Map<String, AccessTokenContext.TokenType> tokenTypesByShortcut;
    Map<String, String> grantsByShortcuts;
    Map<String, String> grantsToShortcuts;

    /** @param session Keycloak 会话
     * @return 编码器实例 */
    @Override
    public TokenContextEncoderProvider create(KeycloakSession session) {
        return new DefaultTokenContextEncoderProvider(session, this);
    }

    /** 初始化会话类型与令牌类型快捷码索引。 */
    @Override
    public void init(Config.Scope config) {
        sessionTypesByShortcut = new HashMap<>();
        for (AccessTokenContext.SessionType st : AccessTokenContext.SessionType.values()) {
            sessionTypesByShortcut.put(st.getShortcut(), st);
        }
        sessionTypesByShortcut = Collections.unmodifiableMap(sessionTypesByShortcut);

        tokenTypesByShortcut = new HashMap<>();
        for (AccessTokenContext.TokenType tt : AccessTokenContext.TokenType.values()) {
            tokenTypesByShortcut.put(tt.getShortcut(), tt);
        }
        tokenTypesByShortcut = Collections.unmodifiableMap(tokenTypesByShortcut);
    }

    /** 收集所有 {@link OAuth2GrantType} 的授权类型快捷码并校验无重复。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        this.sessionFactory = factory;
        grantsByShortcuts = new ConcurrentHashMap<>();
        grantsToShortcuts = new ConcurrentHashMap<>();

        factory.getProviderFactoriesStream(OAuth2GrantType.class)
                .forEach((factory1) -> {
                    OAuth2GrantTypeFactory gtf = (OAuth2GrantTypeFactory) factory1;
                    String grantName = gtf.getId();
                    String grantShortcut = gtf.getShortcut();
                    grantsByShortcuts.put(grantShortcut, grantName);
                    grantsToShortcuts.put(grantName, grantShortcut);
                });
        grantsByShortcuts.put(DefaultTokenContextEncoderProvider.UNKNOWN, DefaultTokenContextEncoderProvider.UNKNOWN);
        grantsToShortcuts.put(DefaultTokenContextEncoderProvider.UNKNOWN, DefaultTokenContextEncoderProvider.UNKNOWN);

        // 校验授权类型快捷码无重复
        if (grantsByShortcuts.size() != grantsToShortcuts.size()) {
            throw new IllegalStateException("Different lengths of maps. grantsByShortcuts.size=" + grantsByShortcuts.size() + ", grantsToShortcuts.size=" + grantsToShortcuts.size() +
                    ". Make sure that there is no OAuth2GrantType implementation with same ID or shortcut like other grants");
        }
    }

    @Override
    public void close() {

    }

    /** @return 工厂 ID {@code default} */
    @Override
    public String getId() {
        return "default";
    }

    /** @param sessionTypeShortcut 2 字符会话类型码
     * @return 会话类型或 null */
    protected AccessTokenContext.SessionType getSessionTypeByShortcut(String sessionTypeShortcut) {
        return sessionTypesByShortcut.get(sessionTypeShortcut);
    }

    /** @param tokenTypeShortcut 2 字符令牌类型码
     * @return 令牌类型或 null */
    protected AccessTokenContext.TokenType getTokenTypeByShortcut(String tokenTypeShortcut) {
        return tokenTypesByShortcut.get(tokenTypeShortcut);
    }

    /** 按授权类型名查快捷码，必要时动态刷新映射。 */
    protected String getShortcutByGrantType(String grantType) {
        String grantShortcut = grantsToShortcuts.get(grantType);
        if (grantShortcut == null) {
            // 新 grant 部署后刷新映射
            OAuth2GrantTypeFactory factory = (OAuth2GrantTypeFactory) sessionFactory.getProviderFactory(OAuth2GrantType.class, grantType);
            if (factory != null) {
                String shortcut = factory.getShortcut();
                grantsByShortcuts.put(shortcut, grantType);
                grantsToShortcuts.put(grantType, shortcut);
            }
            grantShortcut = grantsToShortcuts.get(grantType);
        }
        return grantShortcut;
    }

    /** 按快捷码反查授权类型名。 */
    protected String getGrantTypeByShortcut(String shortcut) {
        String grantType = grantsByShortcuts.get(shortcut);
        if (grantType == null) {
            // Refresh maps in case new grant type was deployed
            sessionFactory.getProviderFactoriesStream(OAuth2GrantType.class)
                    .map(fct -> (OAuth2GrantTypeFactory) fct)
                    .filter(fct -> shortcut.equals(fct.getShortcut()))
                    .map(OAuth2GrantTypeFactory::getId)
                    .findFirst()
                    .ifPresent(newGrantType -> {
                        grantsByShortcuts.put(shortcut, newGrantType);
                        grantsToShortcuts.put(newGrantType, shortcut);
                    });
            grantType = grantsByShortcuts.get(shortcut);
        }
        return grantType;
    }
}
