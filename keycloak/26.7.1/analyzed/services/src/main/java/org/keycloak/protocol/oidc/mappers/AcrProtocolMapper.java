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

import org.keycloak.Config;
import org.keycloak.authentication.authenticators.util.LoAUtil;
import org.keycloak.common.Profile;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.AcrUtils;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.keycloak.services.managers.AuthenticationManager;

import org.jboss.logging.Logger;

/**
 * ACR（Authentication Context Class Reference）协议映射器。
 * <p>将认证达到的 LoA（Authentication Level）映射为令牌中的 {@code acr} 声明。</p>
 * <p>需启用 STEP_UP_AUTHENTICATION 特性。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AcrProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, TokenIntrospectionTokenMapper, EnvironmentDependentProviderFactory {

    private static final Logger logger = Logger.getLogger(AcrProtocolMapper.class);

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AcrProtocolMapper.class);
    }

    /** Provider 标识符 */
    public static final String PROVIDER_ID = "oidc-acr-mapper";


    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 显示名称：Authentication Context Class Reference (ACR) */
    @Override
    public String getDisplayType() {
        return "Authentication Context Class Reference (ACR)";
    }

    /** {@inheritDoc} 令牌映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** {@inheritDoc} 将 LoA 映射为 acr 声明 */
    @Override
    public String getHelpText() {
        return "Maps the achieved LoA (Level of Authentication) to the 'acr' claim of the token";
    }

    /** {@inheritDoc} 写入 acr 声明 */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {
        AuthenticatedClientSessionModel clientSession = clientSessionCtx.getClientSession();
        String acr = getAcr(clientSession);
        token.setAcr(acr);
    }

    /**
     * 工厂方法：创建 ACR 映射器配置模型。
     * @param name 映射器名称
     * @param accessToken 是否包含于 Access Token
     * @param idToken 是否包含于 ID Token
     * @param introspectionEndpoint 是否包含于 Introspection
     */
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        mapper.setConfig(config);
        return mapper;
    }

    /**
     * 根据客户端 LoA 映射与 claims/acr_values 解析最终 acr 字符串。
     * @param clientSession 已认证客户端会话
     */
        int loa = LoAUtil.getCurrentLevelOfAuthentication(clientSession);
        logger.tracef("Loa level when authenticated to client %s: %d", clientSession.getClient().getClientId(), loa);
        if (loa < Constants.MINIMUM_LOA) {
            loa = AuthenticationManager.isSSOAuthentication(clientSession) ? 0 : 1;
        }

        Map<String, Integer> acrLoaMap = AcrUtils.getAcrLoaMap(clientSession.getClient());
        String acr = AcrUtils.mapLoaToAcr(loa, acrLoaMap, AcrUtils.getRequiredAcrValues(
                clientSession.getNote(OIDCLoginProtocol.CLAIMS_PARAM)));
        if (acr == null) {
            acr = AcrUtils.mapLoaToAcr(loa, acrLoaMap, AcrUtils.getAcrValues(
                    clientSession.getNote(OIDCLoginProtocol.CLAIMS_PARAM),
                    clientSession.getNote(OIDCLoginProtocol.ACR_PARAM), clientSession.getClient()));
            if (acr == null) {
                acr = AcrUtils.mapLoaToAcr(loa, acrLoaMap, acrLoaMap.keySet());
                if (acr == null) {
                    acr = String.valueOf(loa);
                }
            }
        }

        logger.tracef("Level sent in the token to client %s: %s. Original loa from the authentication: %d", clientSession.getClient().getClientId(), acr, loa);
        return acr;
    }

    /** {@inheritDoc} 需启用 STEP_UP_AUTHENTICATION 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.STEP_UP_AUTHENTICATION);
    }
}
