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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.models.oid4vci.Oid4vcProtocolMapperModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oid4vc.OID4VCEnvironmentProviderFactory;
import org.keycloak.protocol.oid4vc.OID4VCLoginProtocolFactory;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.provider.ProviderConfigProperty;

import org.apache.commons.collections4.ListUtils;

import static org.keycloak.OID4VCConstants.CREDENTIAL_SUBJECT;
import static org.keycloak.VCFormat.SD_JWT_VC;

/**
 * OID4VC 协议映射器抽象基类，统一公共配置与元数据路径逻辑。
 * <p>提供 mandatory/display 等通用配置项，并根据 VC 格式（如 SD-JWT）决定 claims 路径前缀。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public abstract class OID4VCMapper implements ProtocolMapper, OID4VCEnvironmentProviderFactory {

    /** 配置键：目标声明/属性名。 */
    public static final String CLAIM_NAME = "claim.name";
    /** 配置键：源用户属性名。 */
    public static final String USER_ATTRIBUTE_KEY = "userAttribute";
    private static final List<ProviderConfigProperty> OID4VC_CONFIG_PROPERTIES = new ArrayList<>();

    static {
        ProviderConfigProperty property;

        // vc.mandatory：标识该声明在凭证中是否必填（写入元数据供钱包展示）
        property = new ProviderConfigProperty();
        property.setName(Oid4vcProtocolMapperModel.MANDATORY);
        property.setLabel("Mandatory Claim");
        property.setHelpText("Indicates whether this claim must be present in the issued credential. " +
                "This information is included in the credential metadata for wallet applications.");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setDefaultValue(false);
        OID4VC_CONFIG_PROPERTIES.add(property);

        // vc.display：钱包 UI 展示用国际化标签
        property = new ProviderConfigProperty();
        property.setName(Oid4vcProtocolMapperModel.DISPLAY);
        property.setLabel("Claim Display Information");
        property.setHelpText("Display metadata for wallet applications to show user-friendly claim names. " +
                "Provide display entries with name and locale for internationalization support.");
        property.setType(ProviderConfigProperty.CLAIM_DISPLAY_TYPE);
        property.setDefaultValue(null);
        OID4VC_CONFIG_PROPERTIES.add(property);
    }

    /** 当前协议映射器模型（含配置）。 */
    protected ProtocolMapperModel mapperModel;
    /** 当前凭证格式（如 SD-JWT），影响 claims 路径前缀。 */
    protected String format;

    protected abstract List<ProviderConfigProperty> getIndividualConfigProperties();

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Stream.concat(OID4VC_CONFIG_PROPERTIES.stream(), getIndividualConfigProperties().stream()).toList();
    }

    public OID4VCMapper setMapperModel(ProtocolMapperModel mapperModel, String format) {
        this.mapperModel = mapperModel;
        this.format = format;
        return this;
    }

    /**
     * 是否将该映射器产生的声明纳入凭证元数据。
     * <p>部分声明（如 jti、sub、iss）通常不应出现在元数据中，子类可覆盖为 {@code false}。</p>
     */
    public boolean includeInMetadata() {
        return Optional.ofNullable(mapperModel.getConfig().get(CredentialScopeModel.VC_INCLUDE_IN_METADATA))
                       .map(Boolean::parseBoolean)
                       .orElse(true);
    }

    /**
     * 返回写入凭证的有序属性路径，供元数据端点描述声明位置。
     *
     * @return 映射到凭证中的属性路径片段列表
     */
    public List<String> getMetadataAttributePath() {
        final String claimName = mapperModel.getConfig().get(CLAIM_NAME);
        final String userAttributeName = mapperModel.getConfig().get(USER_ATTRIBUTE_KEY);
        String attributeName = Optional.ofNullable(claimName)
                .orElse(userAttributeName);

        if (attributeName == null) {
            return Collections.emptyList();
        }
        
        return ListUtils.union(getAttributePrefix(), List.of(attributeName));
    }

    protected List<String> getAttributePrefix() {
        if (SD_JWT_VC.equals(format)) {
            return Collections.emptyList();
        } else {
            return List.of(CREDENTIAL_SUBJECT);
        }
    }

    @Override
    public String getProtocol() {
        return OID4VCLoginProtocolFactory.PROTOCOL_ID;
    }

    @Override
    public String getDisplayCategory() {
        return "OID4VC Mapper";
    }

    @Override
    public void init(Config.Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // 预留：postInit 阶段可预加载凭证相关资源
    }

    @Override
    public void close() {
    }

    /** 向 {@link VerifiableCredential} 顶层结构写入声明（如 context、type）。 */

    public abstract void setClaim(VerifiableCredential verifiableCredential,
                                  UserSessionModel userSessionModel);

    /** 向凭证主体 claims 映射写入声明。 */

    public abstract void setClaim(Map<String, Object> claims,
                                  UserSessionModel userSessionModel);

    /**
     * 将 {@link #setClaim(Map, UserSessionModel)} 产生的扁平 claims 按元数据路径前缀写入嵌套结构。
     *
     * @param claimsOrig {@link #setClaim(Map, UserSessionModel)} 返回的原始 claims（通常只读）
     * @param claimsWithPrefix 带路径前缀的目标嵌套 Map（本方法可能写入）
     */
    public void setClaimWithMetadataPrefix(Map<String, Object> claimsOrig, Map<String, Object> claimsWithPrefix) {
        List<String> attributePath = getMetadataAttributePath();
        if (attributePath.isEmpty()) {
            return;
        }
        String propertyName = attributePath.get(attributePath.size() - 1);
        if (claimsOrig.get(propertyName) != null) {
            Object claimValue = claimsOrig.get(propertyName);
            Map<String, Object> current = claimsWithPrefix;

            for (int i = 0; i < attributePath.size(); i++) {
                String currentSnippetName = attributePath.get(i);
                if (i < attributePath.size() - 1) {
                    Map<String, Object> obj = (Map<String, Object>) current.get(currentSnippetName);
                    if (obj == null) {
                         obj = new HashMap<>();
                         current.put(currentSnippetName, obj);
                    }
                    current = obj;
                } else {
                    // 路径末段：写入实际声明值
                    current.put(currentSnippetName, claimValue);
                }
            }
        }
    }

}
