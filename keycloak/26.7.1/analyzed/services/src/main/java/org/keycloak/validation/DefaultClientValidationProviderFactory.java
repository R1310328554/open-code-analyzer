/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.validation;

import org.keycloak.models.KeycloakSession;

/**
 * 默认客户端校验提供者工厂。
 * <p>返回单例 {@link DefaultClientValidationProvider} 实例。</p>
 */
public class DefaultClientValidationProviderFactory implements ClientValidationProviderFactory {

    /** 共享的单例校验提供者实例。 */
    private final DefaultClientValidationProvider provider = new DefaultClientValidationProvider();

    /** 创建（返回）客户端校验提供者。 */
    @Override
    public ClientValidationProvider create(KeycloakSession session) {
        return provider;
    }

    /** @return 工厂标识 {@code "default"} */
    @Override
    public String getId() {
        return "default";
    }

}
