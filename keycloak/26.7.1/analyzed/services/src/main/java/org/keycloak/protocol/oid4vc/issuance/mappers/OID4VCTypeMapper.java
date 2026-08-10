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

package org.keycloak.protocol.oid4vc.issuance.mappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 为可验证凭证添加 {@code type} 的协议映射器。
 * <p>将配置的 VC 类型合并进 {@link VerifiableCredential#getType()}，默认追加 {@link #DEFAULT_VC_TYPE}。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public class OID4VCTypeMapper extends OID4VCMapper {

    /** 协议映射器 Provider ID。 */
    public static final String MAPPER_ID = "oid4vc-vc-type-mapper";
    /** 配置键：VC 类型字符串。 */
    public static final String TYPE_KEY = "vcTypeProperty";
    /** 未配置时的默认类型。 */
    public static final String DEFAULT_VC_TYPE = "VerifiableCredential";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        ProviderConfigProperty vcTypePropertyNameConfig = new ProviderConfigProperty();
        vcTypePropertyNameConfig.setName(TYPE_KEY);
        vcTypePropertyNameConfig.setLabel("Verifiable Credential Type");
        vcTypePropertyNameConfig.setHelpText("Type of the credential.");
        vcTypePropertyNameConfig.setType(ProviderConfigProperty.STRING_TYPE);
        CONFIG_PROPERTIES.add(vcTypePropertyNameConfig);
    }

    @Override
    protected List<ProviderConfigProperty> getIndividualConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    /** 默认不将 type 纳入凭证元数据。 */

    @Override
    public boolean includeInMetadata() {
        return Optional.ofNullable(mapperModel.getConfig().get(CredentialScopeModel.VC_INCLUDE_IN_METADATA))
                       .map(Boolean::parseBoolean)
                       .orElse(false);
    }

    @Override
    public List<String> getMetadataAttributePath() {
        return List.of("type");
    }

    public void setClaim(VerifiableCredential verifiableCredential,
                         UserSessionModel userSessionModel) {
        // 去重后合并 type 列表
        Set<String> types = new HashSet<>();
        if (verifiableCredential.getType() != null) {
            types = new HashSet<>(verifiableCredential.getType());
        }
        types.add(Optional.ofNullable(mapperModel.getConfig().get(TYPE_KEY)).orElse(DEFAULT_VC_TYPE));
        verifiableCredential.setType(new ArrayList<>(types));
    }

    @Override
    public void setClaim(Map<String, Object> claims, UserSessionModel userSessionModel) {
        // type 写入 VC 顶层，不操作 subject claims
    }

    @Override
    public String getDisplayType() {
        return "Credential Type Mapper";
    }

    @Override
    public String getHelpText() {
        return "Assigns a type to the credential.";
    }

    @Override
    public ProtocolMapper create(KeycloakSession session) {
        return new OID4VCTypeMapper();
    }

    @Override
    public String getId() {
        return MAPPER_ID;
    }
}
