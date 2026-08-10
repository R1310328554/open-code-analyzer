/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.keycloak.models.GroupModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

/**
 * 用户组成员身份映射器。
 * <p>将用户所属组列表映射到令牌声明，可选择完整组路径或仅组名。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class GroupMembershipMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        ProviderConfigProperty property1 = new ProviderConfigProperty();
        property1.setName("full.path");
        property1.setLabel("Full group path");
        property1.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property1.setDefaultValue("true");
        property1.setHelpText("Include full path to group i.e. /top/level1/level2, false will just specify the group name");
        configProperties.add(property1);

        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, GroupMembershipMapper.class);
    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-group-membership-mapper";


    /** @return 配置属性列表 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** @return 映射器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Group Membership";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Map user group membership";
    }

    /** 是否使用完整组路径 @param mappingModel 映射配置 @return 为 true 时使用完整路径 */
    public static boolean useFullPath(ProtocolMapperModel mappingModel) {
        return "true".equals(mappingModel.getConfig().get("full.path"));
    }


    /**
     * 将组成员信息写入 {@link IDToken#otherClaims}。
     * @param token 目标令牌
     * @param mappingModel 映射配置
     * @param userSession 用户会话
     */
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        Function<GroupModel, String> toGroupRepresentation = useFullPath(mappingModel) ?
                ModelToRepresentation::buildGroupPath : GroupModel::getName;
        List<String> membership = userSession.getUser().getGroupsStream().map(toGroupRepresentation).collect(Collectors.toList());

        // 强制多值：该映射器未定义单值属性
        mappingModel.getConfig().put(ProtocolMapperUtils.MULTIVALUED, "true");
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, membership);
    }

    /**
     * 创建组成员映射器。
     * @param name 映射器名称
     * @param tokenClaimName 令牌声明名
     * @param consentRequired 是否需要同意
     * @param consentText 同意文本
     * @param accessToken 是否写入访问令牌
     * @param idToken 是否写入 ID Token
     * @param introspectionEndpoint 是否写入自省端点
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel create(String name,
                                             String tokenClaimName,
                                             boolean consentRequired, String consentText,
                                             boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<String, String>();
        config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, tokenClaimName);
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        mapper.setConfig(config);

        return mapper;
    }


}
