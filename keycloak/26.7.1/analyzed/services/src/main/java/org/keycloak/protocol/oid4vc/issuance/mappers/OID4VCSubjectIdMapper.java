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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oid4vc.OID4VCLoginProtocolFactory;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_SUBJECT_ID;

/**
 * 设置凭证主体标识（subject id）的协议映射器，可从用户 ID 或用户属性映射。
 * <p>VC 常与 Holder 的 DID 绑定；启用 {@code oid4vc-vci} 特性时默认 UserProfile 含 {@code did} 属性。未来可能由 Holder 在授权阶段提供 DID，Issuer 校验密钥持有后签发，无需持久化 Holder DID。</p>
 * <p>EBSI 等互操作场景要求类似授权流程；当前 EBSI 兼容性测试以 Holder DID 作为 OIDC {@code client_id}。</p>
 * <p>默认注入的用户属性定义示例：{@code did}，带 {@code ^did:.+:.+$} 格式校验。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public class OID4VCSubjectIdMapper extends OID4VCMapper {

    /** 协议映射器 Provider ID。 */
    public static final String MAPPER_ID = "oid4vc-subject-id-mapper";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        ProviderConfigProperty idPropertyNameConfig = new ProviderConfigProperty();
        idPropertyNameConfig.setName(CLAIM_NAME);
        idPropertyNameConfig.setLabel(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME_LABEL);
        idPropertyNameConfig.setHelpText("Name of the claim to insert into the token. This can be a fully qualified name such as 'address.street'. In case that 'id' is used as a value of this configuration property, it would be mapped into sd-jwt credential as claim 'sub'.");
        idPropertyNameConfig.setType(ProviderConfigProperty.STRING_TYPE);
        idPropertyNameConfig.setDefaultValue(CLAIM_NAME_SUBJECT_ID);
        CONFIG_PROPERTIES.add(idPropertyNameConfig);

        ProviderConfigProperty userAttributeConfig = new ProviderConfigProperty();
        userAttributeConfig.setName(USER_ATTRIBUTE_KEY);
        userAttributeConfig.setLabel("User attribute");
        userAttributeConfig.setHelpText("The name of the user attribute that maps to the subject id.");
        userAttributeConfig.setType(ProviderConfigProperty.LIST_TYPE);
        userAttributeConfig.setOptions(List.of(UserModel.DID, UserModel.USERNAME, UserModel.EMAIL, UserModel.ID));
        userAttributeConfig.setDefaultValue(UserModel.DID);
        CONFIG_PROPERTIES.add(userAttributeConfig);
    }

    /**
     * 工厂方法：创建预配置的 Subject ID 映射器模型。
     * @param name 映射器显示名
     * @param claimName 目标声明名
     * @param userAttribute 源用户属性
     */
    public static ProtocolMapperModel create(String name, String claimName, String userAttribute) {
        var mapperModel = new ProtocolMapperModel();
        mapperModel.setName(name);
        Map<String, String> configMap = new HashMap<>();
        configMap.put(CLAIM_NAME, claimName);
        configMap.put(USER_ATTRIBUTE_KEY, userAttribute);
        mapperModel.setConfig(configMap);
        mapperModel.setProtocol(OID4VCLoginProtocolFactory.PROTOCOL_ID);
        mapperModel.setProtocolMapper(MAPPER_ID);
        return mapperModel;
    }

    @Override
    protected List<ProviderConfigProperty> getIndividualConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    public void setClaim(VerifiableCredential verifiableCredential, UserSessionModel userSessionModel) {
        // subject id 写入 Map claims，不修改 VC 顶层
    }

    @Override
    public void setClaim(Map<String, Object> claims, UserSessionModel userSessionModel) {
        UserModel userModel = userSessionModel.getUser();
        List<String> attributePath = getMetadataAttributePath();
        if (attributePath.isEmpty()) {
            return;
        }
        String propertyName = attributePath.get(attributePath.size() - 1);
        String userAttributeName = mapperModel.getConfig().get(OID4VCMapper.USER_ATTRIBUTE_KEY);
        Consumer<String> userIdConsumer = (val) -> claims.put(propertyName, val);
        if (UserModel.ID.equals(userAttributeName)) {
            userIdConsumer.accept(userModel.getId());
        } else {
            KeycloakModelUtils.resolveAttribute(userModel, userAttributeName, false).stream()
                    .filter(Objects::nonNull)
                    .findFirst()
                    .ifPresent(userIdConsumer);
        }
    }

    @Override
    public String getDisplayType() {
        return "CredentialSubject ID Mapper";
    }

    @Override
    public String getHelpText() {
        return "Sets an ID for the credential subject, either from User ID or by attribute mapping.";
    }

    @Override
    public ProtocolMapper create(KeycloakSession session) {
        return new OID4VCSubjectIdMapper();
    }

    @Override
    public String getId() {
        return MAPPER_ID;
    }
}
