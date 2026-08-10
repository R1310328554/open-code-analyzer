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
package org.keycloak.protocol.saml.mappers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.Config;
import org.keycloak.authentication.authenticators.util.LoAUtil;
import org.keycloak.common.Profile;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AuthnContextClassRefType;
import org.keycloak.dom.saml.v2.assertion.AuthnContextType;
import org.keycloak.dom.saml.v2.assertion.AuthnStatementType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.utils.AcrUtils;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.sessions.AuthenticationSessionModel;

import org.jboss.logging.Logger;

/**
 * SAML AuthnContextClassRef 映射器。
 * <p>根据认证 LoA（保证级别）或 SAML 请求中的 ACR 值，向 AuthnStatement 写入 {@link AuthnContextClassRefType}；需启用 STEP_UP_AUTHENTICATION_SAML 特性。</p>
 *
 * @author rmartinc
 */
public class AuthnContextClassRefMapper extends AbstractSAMLProtocolMapper implements SAMLLoginResponseMapper, EnvironmentDependentProviderFactory {

    /** 提供方标识 */
    public static final String PROVIDER_ID = "saml-authn-context-class-ref-mapper";
    /** 映射器分类标签 */
    public static final String AUTHN_CONTEXT_CLASS_REF_CATEGORY = "AuthnContextClassRef mapper";
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(AuthnContextClassRefMapper.class);

    /** @return 配置属性列表（空） */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    /** @return 映射器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return AUTHN_CONTEXT_CLASS_REF_CATEGORY;
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return AUTHN_CONTEXT_CLASS_REF_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Add the AuthnContextClassRef to the AuthContext with the Level of Assurance if present.";
    }

    /**
     * 向 SAML 登录响应的 AuthnStatement 设置 AuthnContextClassRef。
     * @param response SAML 响应
     * @param clientSessionCtx 客户端会话上下文
     * @return 转换后的响应
     */
    @Override
    public ResponseType transformLoginResponse(ResponseType response, ProtocolMapperModel mappingModel,
            KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        int loa = LoAUtil.getCurrentLevelOfAuthentication(clientSessionCtx.getClientSession());
        AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();
        String acrValue = authSession != null? authSession.getClientNote(SamlProtocol.SAML_AUTHN_CONTEXT_CLASS_REF) : null;
        logger.tracef("Current level of authentication %d, requested level %s", loa, acrValue);
        if (loa < Constants.MINIMUM_LOA) {
            // 未使用阶梯认证时保持原响应不变
            return response;
        }

        Map<String, Integer> acrLoaMap = AcrUtils.getUriLoaMap(clientSessionCtx.getClientSession().getClient());
        if (acrValue == null) {
            // SAML 未显式请求 ACR 时，查找该 LoA 对应的 URI
            acrValue = acrLoaMap.entrySet().stream().filter(e -> loa == e.getValue()).map(Map.Entry::getKey).sorted().findFirst().orElse(null);
        } else {
            // 校验请求的 LoA 是否已由认证流达成，否则置空
            Integer requestedLevel = acrLoaMap.get(acrValue);
            if (requestedLevel == null || requestedLevel > loa) {
                logger.warnf("Requested level '%s' (%s) was not reached after authentication flow, current level %d",
                        acrValue, requestedLevel, loa);
                acrValue = null;
            }
        }

        URI authnContextClassRef = createUri(acrValue);

        if (authnContextClassRef == null) {
            return response;
        }

        Optional<AuthnStatementType> authStatementOptional = response.getAssertions().stream()
                .map(ResponseType.RTChoiceType::getAssertion)
                .map(AssertionType::getStatements)
                .flatMap(s -> s.stream())
                .filter(AuthnStatementType.class::isInstance)
                .map(AuthnStatementType.class::cast)
                .findAny();

        if (authStatementOptional.isPresent()) {
            logger.tracef("Setting the authentication context to '%s'", acrValue);
            AuthnStatementType authStatement = authStatementOptional.get();
            AuthnContextType authContext = new AuthnContextType();
            AuthnContextType.AuthnContextTypeSequence sequence = new AuthnContextType.AuthnContextTypeSequence();
            sequence.setClassRef(new AuthnContextClassRefType(authnContextClassRef));
            authContext.setSequence(sequence);
            authStatement.setAuthnContext(authContext);
        }

        return response;
    }

    /** 将 ACR 字符串解析为 URI @param acrValue ACR/AuthnContextClassRef 值 @return URI 或 null */
    private URI createUri(String acrValue) {
        if (acrValue == null) {
            return null;
        }

        try {
            return new URI(acrValue);
        } catch (URISyntaxException e) {
            logger.warnf("Invalid URI syntax for AuthnContextClassRef in the Level of Authentication '%s'", acrValue);
            return null;
        }
    }

    /** 是否支持：需启用 STEP_UP_AUTHENTICATION_SAML 特性 @return 特性开启时 true */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.STEP_UP_AUTHENTICATION_SAML);
    }

    /** 创建 AuthnContextClassRef 映射器 @param name 映射器名称 @return 协议映射器模型 */
    public static ProtocolMapperModel create(String name) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(SamlProtocol.LOGIN_PROTOCOL);
        return mapper;
    }
}
