/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.protocol.oidc.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.AmrUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import org.jboss.logging.Logger;

/**
 * 认证方法引用（AMR）协议映射器。
 * <p>根据用户会话中已完成认证器的配置，将 {@code amr} 声明写入 OIDC 令牌。</p>
 *
 * @author Ben Cresitello-Dittmar
 */
public class AmrProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(AmrProtocolMapper.class);

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-amr-mapper";

    /** @return 配置属性列表 */
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AmrProtocolMapper.class);
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
        return "Authentication Method Reference (AMR)";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Add authentication method reference (AMR) to the token.";
    }

    /** 将会话中的 AMR 值写入令牌声明 */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {
        AuthenticatedClientSessionModel clientSession = clientSessionCtx.getClientSession();
        List<String> amr = getAmr(clientSession, userSession.getRealm());
        token.setOtherClaims(OAuth2Constants.AUTHENTICATOR_METHOD_REFERENCE, amr);
    }

    /** 创建 AMR 映射器 @param name 名称 @param accessToken 是否写入访问令牌 @param idToken 是否写入 ID Token @return 协议映射器模型 */
    public static ProtocolMapperModel create(String name, boolean accessToken, boolean idToken) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        mapper.setConfig(config);
        return mapper;
    }

    /**
     * 从现有会话提取 AMR 值。
     *
     * @param clientSession 已认证的客户端会话
     * @param realmModel 映射器所在领域，用于读取认证执行配置
     * @return 已完成认证执行对应的认证器引用值列表
     */
    protected List<String> getAmr(AuthenticatedClientSessionModel clientSession, RealmModel realmModel) {
        Map<String, Integer> executions = AuthenticatorUtils.parseCompletedExecutions(clientSession.getUserSession().getNote(Constants.AUTHENTICATORS_COMPLETED));
        logger.debugf("found the following completed authentication executions: %s", executions.toString());
        List<String> refs = AmrUtils.getAuthenticationExecutionReferences(executions, realmModel);
        logger.debugf("amr %s set in token", refs);
        return refs;
    }
}
