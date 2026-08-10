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

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.utils.RoleResolveUtil;

/**
 * 硬编码角色映射器。
 * <p>向访问令牌、UserInfo 或自省响应强制添加配置的角色，供后续角色映射器决定是否最终写入。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HardcodedRole extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /** 配置键：要添加的角色 */
    public static final String ROLE_CONFIG = "role";

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ROLE_CONFIG);
        property.setLabel("Role");
        property.setHelpText("Role you want added to the token.  Click 'Select Role' button to browse roles, or just type it in the textbox.  To reference a client role the syntax is clientname.clientrole, i.e. myclient.myrole");
        property.setType(ProviderConfigProperty.ROLE_TYPE);
        configProperties.add(property);
    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-hardcoded-role-mapper";


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
        return "Hardcoded Role";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Hardcode a role into the access token.";
    }

    /** @return 映射器执行优先级（硬编码角色优先） */
    @Override
    public int getPriority() {
        return ProtocolMapperUtils.PRIORITY_HARDCODED_ROLE_MAPPER;
    }

    /** 转换 UserInfo 令牌并预置硬编码角色 @return 转换后的令牌 */
    @Override
    public AccessToken transformUserInfoToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                              UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // 映射器始终执行；最终是否写入由后续角色映射器决定
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    /** 转换访问令牌并预置硬编码角色 @return 转换后的访问令牌 */
    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // the mapper is always executed and then other role mappers decide if the claims are really set to the token
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    /** 转换自省令牌并预置硬编码角色 @return 转换后的令牌 */
    @Override
    public AccessToken transformIntrospectionToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // the mapper is always executed and then other role mappers decide if the claims are really set to the token
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    /** 将配置的角色添加到已解析的领域或客户端角色集合 @param role 格式为 realmRole 或 clientId.clientRole */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession session,
                            ClientSessionContext clientSessionCtx) {

        String role = mappingModel.getConfig().get(ROLE_CONFIG);
        String[] scopedRole = KeycloakModelUtils.parseRole(role);
        String appName = scopedRole[0];
        String roleName = scopedRole[1];
        if (appName != null) {
            AccessToken.Access access = RoleResolveUtil.getResolvedClientRoles(session, clientSessionCtx, appName, true);
            access.addRole(roleName);
        } else {
            AccessToken.Access access = RoleResolveUtil.getResolvedRealmRoles(session, clientSessionCtx, true);
            access.addRole(role);
        }
    }

    /** 创建硬编码角色映射器 @param name 名称 @param role 角色（领域或 client.role 格式） @return 协议映射器模型 */
    public static ProtocolMapperModel create(String name,
                                             String role) {
        String mapperId = PROVIDER_ID;
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(mapperId);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        config.put(ROLE_CONFIG, role);
        mapper.setConfig(config);
        return mapper;

    }

}
