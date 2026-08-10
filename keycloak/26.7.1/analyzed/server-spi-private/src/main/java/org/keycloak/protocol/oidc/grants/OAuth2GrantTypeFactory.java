/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.grants;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link OAuth2GrantType} 的 SPI 工厂接口。
 *
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a>
 */
public interface OAuth2GrantTypeFactory extends ProviderFactory<OAuth2GrantType> {

    /**
     * @return grant 的短缩写（通常 3 字符），用于令牌等字符数受限场景；各 grant 间应唯一
     */
    String getShortcut();
}
