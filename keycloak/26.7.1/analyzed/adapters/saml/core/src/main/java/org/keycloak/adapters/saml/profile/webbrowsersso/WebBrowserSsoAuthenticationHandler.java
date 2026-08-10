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

package org.keycloak.adapters.saml.profile.webbrowsersso;

import org.keycloak.adapters.saml.OnSessionCreated;
import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlSession;
import org.keycloak.adapters.saml.SamlSessionStore;
import org.keycloak.adapters.saml.SamlUtil;
import org.keycloak.adapters.saml.profile.AbstractSamlAuthenticationHandler;
import org.keycloak.adapters.saml.profile.SamlAuthenticationHandler;
import org.keycloak.adapters.saml.profile.SamlInvocationContext;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.dom.saml.v2.protocol.LogoutRequestType;
import org.keycloak.saml.BaseSAML2BindingBuilder;
import org.keycloak.saml.SAML2LogoutRequestBuilder;
import org.keycloak.saml.SAML2LogoutResponseBuilder;
import org.keycloak.saml.common.constants.GeneralConstants;

/**
 * Web Browser SSO 配置文件下的 SAML 认证处理器实现。
 *
 * <p>继承 {@link AbstractSamlAuthenticationHandler}，处理浏览器单点登录、IdP 发起的
 * 登出请求以及全局登出（GLO）场景。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class WebBrowserSsoAuthenticationHandler extends AbstractSamlAuthenticationHandler {

    /**
     * 工厂方法：创建 Web Browser SSO 认证处理器。
     *
     * @param facade       HTTP 门面
     * @param deployment   SAML 部署配置
     * @param sessionStore 会话存储
     * @return 认证处理器实例
     */
    public static SamlAuthenticationHandler create(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        return new WebBrowserSsoAuthenticationHandler(facade, deployment, sessionStore);
    }

    /**
     * 包内可见构造器，由 {@link #create} 或子类调用。
     */
    WebBrowserSsoAuthenticationHandler(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        super(facade, deployment, sessionStore);
    }

    /**
     * 从 HTTP 请求参数构建 {@link SamlInvocationContext} 并委托 {@link #doHandle}。
     *
     * @param onCreateSession 会话创建回调
     * @return 认证结果
     */
    @Override
    public AuthOutcome handle(OnSessionCreated onCreateSession) {
        return doHandle(new SamlInvocationContext(facade.getRequest().getFirstParam(GeneralConstants.SAML_REQUEST_KEY),
                facade.getRequest().getFirstParam(GeneralConstants.SAML_RESPONSE_KEY),
                facade.getRequest().getFirstParam(GeneralConstants.RELAY_STATE)), onCreateSession);
    }

    /**
     * 已认证请求的处理：支持 {@code GLO=true} 查询参数触发全局登出。
     *
     * @return 认证结果或登出流程结果
     */
    @Override
    protected AuthOutcome handleRequest() {
        boolean globalLogout = "true".equals(facade.getRequest().getQueryParamValue("GLO"));

        if (globalLogout) {
            return globalLogout();
        }

        return AuthOutcome.AUTHENTICATED;
    }

    /**
     * 处理 IdP 发起的 {@link LogoutRequestType}：按主体或 SessionIndex 注销本地会话并回送 LogoutResponse。
     *
     * @param request    IdP 登出请求
     * @param relayState 关联状态
     * @return 处理结果
     */
    @Override
    protected AuthOutcome logoutRequest(LogoutRequestType request, String relayState) {
        if (request.getSessionIndex() == null || request.getSessionIndex().isEmpty()) {
            sessionStore.logoutByPrincipal(request.getNameID().getValue());
        } else {
            sessionStore.logoutBySsoId(request.getSessionIndex());
        }

        String issuerURL = deployment.getEntityID();
        SAML2LogoutResponseBuilder builder = new SAML2LogoutResponseBuilder();
        builder.logoutRequestID(request.getID());
        builder.destination(deployment.getIDP().getSingleLogoutService().getResponseBindingUrl());
        builder.issuer(issuerURL);
        BaseSAML2BindingBuilder binding = new BaseSAML2BindingBuilder().relayState(relayState);
        if (deployment.getIDP().getSingleLogoutService().signResponse()) {
            if (deployment.getSignatureCanonicalizationMethod() != null)
                binding.canonicalizationMethod(deployment.getSignatureCanonicalizationMethod());
            binding.signatureAlgorithm(deployment.getSignatureAlgorithm())
                    .signWith(null, deployment.getSigningKeyPair())
                    .signDocument();
            // TODO: KEYCLOAK-3810 — 在 SAML 文档扩展中添加 KeyID
            //   <related DocumentBuilder>.addExtension(new KeycloakKeySamlExtensionGenerator(<key ID>));
        }


        try {
            SamlUtil.sendSaml(false, facade, deployment.getIDP().getSingleLogoutService().getResponseBindingUrl(), binding, builder.buildDocument(),
                    deployment.getIDP().getSingleLogoutService().getResponseBinding());
        } catch (Exception e) {
            log.error("Could not send logout response SAML request", e);
            return AuthOutcome.FAILED;
        }
        return AuthOutcome.NOT_ATTEMPTED;
    }

    /**
     * 向 IdP 发送全局登出请求（SLO），并将会话状态设为 {@code LOGGING_OUT}。
     *
     * @return 认证结果
     */
    private AuthOutcome globalLogout() {
        SamlSession account = sessionStore.getAccount();
        if (account == null) {
            return AuthOutcome.NOT_ATTEMPTED;
        }
        SAML2LogoutRequestBuilder logoutBuilder = new SAML2LogoutRequestBuilder()
                .assertionExpiration(30)
                .issuer(deployment.getEntityID())
                .sessionIndex(account.getSessionIndex())
                .nameId(account.getPrincipal().getNameID())
                .destination(deployment.getIDP().getSingleLogoutService().getRequestBindingUrl());
        BaseSAML2BindingBuilder binding = new BaseSAML2BindingBuilder();
        if (deployment.getIDP().getSingleLogoutService().signRequest()) {
            if (deployment.getSignatureCanonicalizationMethod() != null)
                binding.canonicalizationMethod(deployment.getSignatureCanonicalizationMethod());
            binding.signatureAlgorithm(deployment.getSignatureAlgorithm());
            binding.signWith(null, deployment.getSigningKeyPair())
                    .signDocument();
            // TODO: KEYCLOAK-3810 — 在 SAML 文档扩展中添加 KeyID
            //   <related DocumentBuilder>.addExtension(new KeycloakKeySamlExtensionGenerator(<key ID>));
        }

        binding.relayState("logout");

        try {
            SamlUtil.sendSaml(true, facade, deployment.getIDP().getSingleLogoutService().getRequestBindingUrl(), binding, logoutBuilder.buildDocument(), deployment.getIDP().getSingleLogoutService().getRequestBinding());
            sessionStore.setCurrentAction(SamlSessionStore.CurrentAction.LOGGING_OUT);
        } catch (Exception e) {
            log.error("Could not send global logout SAML request", e);
            return AuthOutcome.FAILED;
        }
        return AuthOutcome.NOT_ATTEMPTED;
    }
}
