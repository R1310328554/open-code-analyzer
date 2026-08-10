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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

/**
 * 用户全名映射器。
 * <p>将用户名字与姓氏拼接为 OpenID Connect {@code name} 声明，格式为「名 + 空格 + 姓」。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class FullNameMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, FullNameMapper.class);

    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-full-name-mapper";


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
        return "User's full name";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Maps the user's first and last name to the OpenID Connect 'name' claim. Format is <first> + ' ' + <last>";
    }

    /** 将用户全名写入 {@code name} 声明 @param token 目标令牌 @param userSession 用户会话 */
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        UserModel user = userSession.getUser();
        List<String> parts = new LinkedList<>();
        Optional.ofNullable(user.getFirstName()).filter(s -> !s.isEmpty()).ifPresent(parts::add);
        Optional.ofNullable(user.getLastName()).filter(s -> !s.isEmpty()).ifPresent(parts::add);
        if (!parts.isEmpty()) {
            token.getOtherClaims().put("name", String.join(" ", parts));
        }
    }

    /**
     * 创建全名映射器。
     * @param name 映射器名称
     * @param accessToken 是否写入访问令牌
     * @param idToken 是否写入 ID Token
     * @param userInfo 是否写入 UserInfo
     * @param introspectionEndpoint 是否写入自省端点
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel create(String name, boolean accessToken, boolean idToken, boolean userInfo, boolean introspectionEndpoint) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (userInfo) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        mapper.setConfig(config);
        return mapper;
    }

}
