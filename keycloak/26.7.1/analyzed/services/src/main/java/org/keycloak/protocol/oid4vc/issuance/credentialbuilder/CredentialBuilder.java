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

import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.model.CredentialBuildConfig;
import org.keycloak.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.provider.Provider;

/**
 * 可验证凭证构建器：将内部 {@link VerifiableCredential} 表示转换为特定格式的未签名凭证体。
 * <p>实现类通过 SPI 注册，支持 JWT-VC、LDP-VC、SD-JWT-VC 等格式。</p>
 */
public interface CredentialBuilder extends Provider {

    /** {@inheritDoc} 默认无资源需释放。 */
    @Override
    default void close() {
    }

    /** @return 本构建器支持的凭证格式标识（如 {@code jwt_vc}） */

    String getSupportedFormat();

    /**
     * 基于内部可验证凭证表示，构建特定格式的未完成凭证体。
     * <p>返回体待外部签名流程完成签发。</p>
     *
     * @param verifiableCredential 内部凭证表示
     * @param credentialBuildConfig 构建附加配置
     * @return 待签名的格式特定凭证体
     * @throws CredentialBuilderException 构建失败时
     */
    CredentialBody buildCredentialBody(
            VerifiableCredential verifiableCredential,
            CredentialBuildConfig credentialBuildConfig
    ) throws CredentialBuilderException;

    /**
     * 向 OID4VCI well-known 凭证签发者元数据贡献格式特定字段。
     * <p>例如 {@code dc+sd-jwt} 格式设置 {@code vct}， {@code jwt_vc_json} 设置 {@code credential_definition}。</p>
     * <p>默认空实现以保持向后兼容。</p>
     *
     * @param credentialConfig 待填充的凭证配置
     * @param credentialScope 凭证 Scope 模型（数据源）
     */
    default void contributeToMetadata(SupportedCredentialConfiguration credentialConfig, CredentialScopeModel credentialScope) {
    }
}
