/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.authenticators.conditional;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

/**
 * 条件认证器工厂接口：注册条件类认证器，引用分类为 {@value #REFERENCE_CATEGORY}。
 * <p>使用单例 {@link #getSingleton()} 而非每次 create 新实例。</p>
 */
public interface ConditionalAuthenticatorFactory extends AuthenticatorFactory {

    /** 管理控制台引用分类标识。 */
    String REFERENCE_CATEGORY = "condition";

    /** @return 单例条件认证器实例 */
    @Override
    default Authenticator create(KeycloakSession session) {
        return getSingleton();
    }

    /** @return 引用分类 {@value #REFERENCE_CATEGORY} */
    @Override
    default String getReferenceCategory() {
        return REFERENCE_CATEGORY;
    }

    /** @return 条件认证器单例 */
    ConditionalAuthenticator getSingleton();

}
