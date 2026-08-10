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

import java.util.ArrayList;
import java.util.List;

import org.keycloak.VCFormat;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * SD-JWT VC 格式可验证凭证的 {@link CredentialBuilderFactory} 实现。
 * <p>注册 {@link VCFormat#SD_JWT_VC} 格式构建器，供 OID4VCI 凭证端点按组件配置实例化 {@link SdJwtCredentialBuilder}。</p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class SdJwtCredentialBuilderFactory implements CredentialBuilderFactory {

    /** Provider 可配置项列表（当前 SD-JWT 构建器无额外配置）。 */
    protected static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /** {@inheritDoc} 返回 {@link VCFormat#SD_JWT_VC}。 */
    @Override
    public String getSupportedFormat() {
        return VCFormat.SD_JWT_VC;
    }

    /** {@inheritDoc} 说明本工厂构建 SD-JWT 格式可验证凭证。 */
    @Override
    public String getHelpText() {
        return "Builds verifiable credentials on the SD-JWT format (https://drafts.oauth.net/oauth-sd-jwt-vc/draft-ietf-oauth-sd-jwt-vc.html).";
    }

    /** {@inheritDoc} 返回空配置项列表。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /**
     * 创建 SD-JWT 凭证构建器实例。
     * @param session Keycloak 会话
     * @param model 组件模型（当前未使用）
     * @return 新的 {@link SdJwtCredentialBuilder}
     */
    @Override
    public CredentialBuilder create(KeycloakSession session, ComponentModel model) {
        return new SdJwtCredentialBuilder();
    }
}
