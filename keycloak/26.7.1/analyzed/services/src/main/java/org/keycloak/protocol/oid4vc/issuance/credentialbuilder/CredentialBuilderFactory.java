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

package org.keycloak.protocol.oid4vc.issuance.credentialbuilder;

import org.keycloak.Config;
import org.keycloak.component.ComponentFactory;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oid4vc.OID4VCEnvironmentProviderFactory;

/**
 * {@link CredentialBuilder} 的组件工厂，按凭证格式创建对应构建器实例。
 * <p>工厂 ID 即 {@link #getSupportedFormat()} 返回值。</p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public interface CredentialBuilderFactory extends
        ComponentFactory<CredentialBuilder, CredentialBuilder>,
        OID4VCEnvironmentProviderFactory {

    /** @return 工厂所支持的可验证凭证格式标识 */

    String getSupportedFormat();

    /** {@inheritDoc} 以支持格式作为组件 ID。 */
    @Override
    default String getId() {
        return getSupportedFormat();
    }

    /** {@inheritDoc} 默认无初始化逻辑。 */
    @Override
    default void init(Config.Scope config) {
    }

    /** {@inheritDoc} 默认无后置初始化。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    /** {@inheritDoc} 默认无资源需释放。 */
    @Override
    default void close() {
    }
}
