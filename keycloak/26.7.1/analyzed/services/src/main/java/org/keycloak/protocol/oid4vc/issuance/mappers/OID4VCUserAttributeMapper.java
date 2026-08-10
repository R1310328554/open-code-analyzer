/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oid4vc.OID4VCLoginProtocolFactory;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.utils.JsonUtils;

import org.apache.commons.collections4.ListUtils;

/**
 * 将用户属性或配置项映射到凭证主体 claims 的协议映射器。
 * <p>支持点分路径声明名、多值聚合（{@link #AGGREGATE_ATTRIBUTES_KEY}）及 User Profile 属性选择。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public class OID4VCUserAttributeMapper extends OID4VCMapper {

    /** 协议映射器 Provider ID。 */
    public static final String MAPPER_ID = "oid4vc-user-attribute-mapper";
    /** 配置键：是否聚合用户属性的多值。 */
    public static final String AGGREGATE_ATTRIBUTES_KEY = "aggregateAttributes";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        ProviderConfigProperty subjectPropertyNameConfig = new ProviderConfigProperty();
        subjectPropertyNameConfig.setName(CLAIM_NAME);
        subjectPropertyNameConfig.setLabel(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME_LABEL);
        subjectPropertyNameConfig.setHelpText(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME_TOOLTIP);
        subjectPropertyNameConfig.setType(ProviderConfigProperty.STRING_TYPE);
        CONFIG_PROPERTIES.add(subjectPropertyNameConfig);

        ProviderConfigProperty userAttributeConfig = new ProviderConfigProperty();
        userAttributeConfig.setName(USER_ATTRIBUTE_KEY);
        userAttributeConfig.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
        userAttributeConfig.setHelpText(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_HELP_TEXT);
        userAttributeConfig.setType(ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE);
        CONFIG_PROPERTIES.add(userAttributeConfig);

        ProviderConfigProperty aggregateAttributesConfig = new ProviderConfigProperty();
        aggregateAttributesConfig.setName(AGGREGATE_ATTRIBUTES_KEY);
        aggregateAttributesConfig.setLabel("Aggregate attributes");
        aggregateAttributesConfig.setHelpText("Should the mapper aggregate user attributes.");
        aggregateAttributesConfig.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        CONFIG_PROPERTIES.add(aggregateAttributesConfig);
    }

    @Override
    protected List<ProviderConfigProperty> getIndividualConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    public void setClaim(VerifiableCredential verifiableCredential,
                         UserSessionModel userSessionModel) {
        // 用户属性写入 Map claims
    }

    @Override
    public void setClaim(Map<String, Object> claims, UserSessionModel userSessionModel) {
        String claimName = mapperModel.getConfig().get(CLAIM_NAME);
        String userAttribute = mapperModel.getConfig().get(USER_ATTRIBUTE_KEY);
        if (claimName == null && userAttribute == null) {
            return;
        }
        boolean aggregateAttributes = Optional.ofNullable(mapperModel.getConfig().get(AGGREGATE_ATTRIBUTES_KEY))
                .map(Boolean::parseBoolean).orElse(false);
        Collection<String> attributes =
                KeycloakModelUtils.resolveAttribute(userSessionModel.getUser(), userAttribute,
                        aggregateAttributes);
        attributes.removeAll(Collections.singleton(null));
        if (!attributes.isEmpty()) {
            JsonUtils.mapClaim(
                    JsonUtils.splitClaimPath(Optional.ofNullable(claimName).orElse(userAttribute)),
                    String.join(",", attributes),
                    claims,
                    false
            );
        }
    }

    /**
     * 工厂方法：创建用户属性映射器模型。
     * @param mapperName 映射器名称
     * @param claimName 目标声明路径
     * @param userAttribute 源用户属性
     * @param aggregateAttributes 是否聚合多值
     */
    public static ProtocolMapperModel create(String mapperName, String claimName, String userAttribute,
                                             boolean aggregateAttributes) {
        ProtocolMapperModel mapperModel = new ProtocolMapperModel();
        mapperModel.setName(mapperName);
        Map<String, String> configMap = new HashMap<>();
        configMap.put(CLAIM_NAME, claimName);
        configMap.put(USER_ATTRIBUTE_KEY, userAttribute);
        configMap.put(AGGREGATE_ATTRIBUTES_KEY, Boolean.toString(aggregateAttributes));
        mapperModel.setConfig(configMap);
        mapperModel.setProtocol(OID4VCLoginProtocolFactory.PROTOCOL_ID);
        mapperModel.setProtocolMapper(MAPPER_ID);
        return mapperModel;
    }

    @Override
    public String getDisplayType() {
        return "User Attribute Mapper";
    }

    @Override
    public String getHelpText() {
        return "Maps user attributes or properties to credential claims.";
    }

    @Override
    public ProtocolMapper create(KeycloakSession session) {
        return new OID4VCUserAttributeMapper();
    }

    @Override
    public String getId() {
        return MAPPER_ID;
    }

    @Override
    public List<String> getMetadataAttributePath() {
        String claimName = mapperModel.getConfig().get(CLAIM_NAME);
        final String userAttributeName = mapperModel.getConfig().get(USER_ATTRIBUTE_KEY);
        // 将声明名拆分为路径段，供元数据端点描述嵌套位置
        final List<String> claimPath = Optional.ofNullable(claimName)
                .map(JsonUtils::splitClaimPath)
                .orElse(Optional.ofNullable(userAttributeName)
                        .map(List::of)
                        .orElse(Collections.emptyList()));
        if (claimPath.isEmpty()) {
            return Collections.emptyList();
        }
        return ListUtils.union(getAttributePrefix(), claimPath);
    }
}
