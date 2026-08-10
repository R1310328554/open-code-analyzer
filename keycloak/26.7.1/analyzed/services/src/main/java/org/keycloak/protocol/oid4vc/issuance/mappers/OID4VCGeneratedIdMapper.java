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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.provider.ProviderConfigProperty;

import org.apache.commons.collections4.ListUtils;

/**
 * 为凭证主体生成并写入 UUID 标识的协议映射器。
 * <p>可配置目标属性名，默认写入 {@code id}，值为 {@code urn:uuid:<随机UUID>}。</p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class OID4VCGeneratedIdMapper extends OID4VCMapper {

    /** 协议映射器 Provider ID。 */
    public static final String MAPPER_ID = "oid4vc-generated-id-mapper";
    /** 默认 ID 属性名。 */
    private static final String SUBJECT_PROPERTY_CONFIG_KEY_DEFAULT = "id";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        ProviderConfigProperty idPropertyNameConfig = new ProviderConfigProperty();
        idPropertyNameConfig.setName(CLAIM_NAME);
        idPropertyNameConfig.setLabel("ID Property Name");
        idPropertyNameConfig.setHelpText("Name of the property to contain the generated id.");
        idPropertyNameConfig.setDefaultValue(SUBJECT_PROPERTY_CONFIG_KEY_DEFAULT);
        idPropertyNameConfig.setType(ProviderConfigProperty.STRING_TYPE);
        CONFIG_PROPERTIES.add(idPropertyNameConfig);
    }

    @Override
    protected List<ProviderConfigProperty> getIndividualConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    /** 默认不将此声明纳入凭证元数据。 */

    @Override
    public boolean includeInMetadata() {
        return Optional.ofNullable(mapperModel.getConfig().get(CredentialScopeModel.VC_INCLUDE_IN_METADATA))
                       .map(Boolean::parseBoolean)
                       .orElse(false);
    }

    @Override
    public List<String> getMetadataAttributePath() {
        String property = Optional.ofNullable(mapperModel.getConfig())
                                  .map(config -> config.get(CLAIM_NAME))
                                  .orElse(SUBJECT_PROPERTY_CONFIG_KEY_DEFAULT);
        return ListUtils.union(getAttributePrefix(), List.of(property));
    }

    public void setClaim(VerifiableCredential verifiableCredential,
                         UserSessionModel userSessionModel) {
        // 生成 ID 仅写入 subject claims，不修改 VC 顶层字段
    }

    @Override
    public void setClaim(Map<String, Object> claims, UserSessionModel userSessionModel) {
        // 生成 urn:uuid 格式 ID 并写入 claims
        List<String> attributePath = getMetadataAttributePath();
        if (attributePath.isEmpty()) {
            return;
        }
        String propertyName = attributePath.get(attributePath.size() - 1);
        claims.put(propertyName, String.format("urn:uuid:%s", UUID.randomUUID()));
    }

    @Override
    public String getDisplayType() {
        return "Generated ID Mapper";
    }

    @Override
    public String getHelpText() {
        return "Assigns a generated ID to the credential's subject. The target property can be configured, but `id` is used by default.";
    }

    @Override
    public ProtocolMapper create(KeycloakSession session) {
        return new OID4VCGeneratedIdMapper();
    }

    @Override
    public String getId() {
        return MAPPER_ID;
    }
}
