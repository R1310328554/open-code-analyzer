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

package org.keycloak.protocol.oid4vc.issuance.signing;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oid4vc.OID4VCEnvironmentProviderFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link CredentialSigner} 的 {@link ProviderFactory}。
 * <p>按凭证格式（JWT VC、LDP VC、SD-JWT VC）注册签名器实现。</p>
 */
public interface CredentialSignerFactory
        extends ProviderFactory<CredentialSigner<?>>, OID4VCEnvironmentProviderFactory {

    /**
     * @return 本签名器支持的凭证格式标识（如 {@link org.keycloak.VCFormat} 常量）
     */
    String getSupportedFormat();

    /** 工厂 ID 与支持的格式相同。 */
    @Override
    default String getId() {
        return getSupportedFormat();
    }

    /** SPI 初始化；默认无操作。 */
    @Override
    default void init(Config.Scope config) {
    }

    /** 后置初始化；默认无操作。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    /** 关闭工厂；默认无操作。 */
    @Override
    default void close() {
    }
}
