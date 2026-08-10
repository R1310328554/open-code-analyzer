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

package org.keycloak.forms.login.freemarker;

import java.util.List;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker 模板方法：查询指定认证器是否已为当前用户配置完成。
 * <p>模板中通过 {@code authenticatorConfigured(providerId)} 调用。</p>
 */
public class AuthenticatorConfiguredMethod implements TemplateMethodModelEx {
    /** 当前领域。 */
    private final RealmModel realm;
    /** 当前用户。 */
    private final UserModel user;
    /** Keycloak 会话。 */
    private final KeycloakSession session;

    /** @param realm 领域 @param user 用户 @param session 会话 */
    public AuthenticatorConfiguredMethod(RealmModel realm, UserModel user, KeycloakSession session) {
        this.realm = realm;
        this.user = user;
        this.session = session;
    }

    /** 第一个参数为认证器 providerId，返回 {@link Authenticator#configuredFor} 结果。 */
    @Override
    public Object exec(List list) throws TemplateModelException {
        String providerId = list.get(0).toString();
        Authenticator authenticator = session.getProvider(Authenticator.class, providerId);
        return authenticator.configuredFor(session, realm, user);
    }
}
