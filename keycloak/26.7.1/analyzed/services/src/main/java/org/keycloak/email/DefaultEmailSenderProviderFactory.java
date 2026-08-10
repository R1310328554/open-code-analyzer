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

package org.keycloak.email;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认 {@link EmailSenderProvider} SPI 工厂。
 * <p>在 {@link #init} 中注册 NONE/BASIC/TOKEN 三种 {@link EmailAuthenticator} 实现。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultEmailSenderProviderFactory implements EmailSenderProviderFactory {

    /** 认证类型 → SMTP 认证策略实现映射。 */
    private final Map<EmailAuthenticator.AuthenticatorType, EmailAuthenticator> emailAuthenticators = new ConcurrentHashMap<>();

    @Override
    /** @param session 当前会话 @return 绑定认证策略表的邮件发送提供者 */
    public EmailSenderProvider create(KeycloakSession session) {
        return new DefaultEmailSenderProvider(session, emailAuthenticators);
    }

    @Override
    /** 注册默认、密码与 OAuth2 令牌三种 SMTP 认证实现。 */
    public void init(Config.Scope config) {
        emailAuthenticators.put(EmailAuthenticator.AuthenticatorType.NONE, new DefaultEmailAuthenticator());
        emailAuthenticators.put(EmailAuthenticator.AuthenticatorType.BASIC, new PasswordAuthEmailAuthenticator());
        emailAuthenticators.put(EmailAuthenticator.AuthenticatorType.TOKEN, new TokenAuthEmailAuthenticator());
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
        emailAuthenticators.clear();
    }

    @Override
    /** @return SPI 工厂标识 {@code default} */
    public String getId() {
        return "default";
    }

    /** @return 已注册的 SMTP 认证策略映射（供测试或扩展使用） */
    public Map<EmailAuthenticator.AuthenticatorType, EmailAuthenticator> getEmailAuthenticators() {
        return emailAuthenticators;
    }
}
