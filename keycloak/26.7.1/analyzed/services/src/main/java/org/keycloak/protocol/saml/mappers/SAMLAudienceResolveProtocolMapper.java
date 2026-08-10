/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.saml.mappers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.keycloak.dom.saml.v2.assertion.AudienceRestrictionType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;

/**
 * SAML 受众解析协议映射器。
 * <p>将用户拥有至少一个客户端角色的所有 SAML 客户端 {@code client_id} 添加到断言 AudienceRestriction（行为与 OIDC 受众解析映射器一致）。</p>
 *
 * @author rmartinc
 */
public class SAMLAudienceResolveProtocolMapper extends AbstractSAMLProtocolMapper implements SAMLLoginResponseMapper {

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(SAMLAudienceResolveProtocolMapper.class);

    /** 提供方标识 */
    public static final String PROVIDER_ID = "saml-audience-resolve-mapper";

    /** 映射器配置属性列表（空） */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    /** @return 配置属性列表 */
    @Override
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
        return "Audience Resolve";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return SAMLAudienceProtocolMapper.AUDIENCE_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Adds all client_ids of \"allowed\" clients to the audience conditions in the assertion. " +
                "Allowed client means any SAML client for which user has at least one client role";
    }

    /** 解析用户客户端角色并将对应 SAML clientId 加入受众 @return 转换后的 SAML 响应 */
    @Override
    public ResponseType transformLoginResponse(ResponseType response,
            ProtocolMapperModel mappingModel, KeycloakSession session,
            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // 获取断言中的 AudienceRestriction
        AudienceRestrictionType aud = SAMLAudienceProtocolMapper.locateAudienceRestriction(response);
        if (aud != null) {
            // 遍历用户角色，计算需添加的 clientId
            // 将拥有角色的 SAML 客户端 clientId 加入受众（与 OIDC 一致）
            clientSessionCtx.getRolesStream()
                    .peek(r -> logger.tracef("Managing role: %s", r.getName()))
                    .filter(RoleModel::isClientRole)
                    .map(r -> (ClientModel) r.getContainer())
                    // 排除当前客户端（默认已作为受众）
                    .filter(app -> SamlProtocol.LOGIN_PROTOCOL.equals(app.getProtocol()) &&
                            !app.getClientId().equals(clientSessionCtx.getClientSession().getClient().getClientId()))
                    .map(ClientModel::getClientId)
                    .peek(audience -> logger.debugf("Audience to add: %s", audience))
                    .forEach(audience -> {
                        try {
                            aud.addAudience(URI.create(audience));
                        } catch (IllegalArgumentException e) {
                            logger.warnf(e, "Invalid URI syntax for audience: %s", audience);
                        }
                    });
        }
        return response;
    }
}
