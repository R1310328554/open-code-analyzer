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

package org.keycloak.adapters.saml;

import java.io.IOException;
import java.security.KeyPair;

import org.keycloak.adapters.saml.SamlDeployment.IDP.SingleSignOnService;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.saml.BaseSAML2BindingBuilder;
import org.keycloak.saml.SAML2AuthnRequestBuilder;
import org.keycloak.saml.SAML2NameIDPolicyBuilder;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ProcessingException;

import org.jboss.logging.Logger;

/**
 * 发起 SAML 登录流程的抽象认证挑战（AuthChallenge）。
 *
 * <p>构建 AuthnRequest、配置签名与绑定，并将用户重定向或 POST 至 IdP
 * 单点登录端点。子类实现 {@link #sendAuthnRequest} 以选择 Redirect 或 POST 绑定。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractInitiateLogin implements AuthChallenge {
    protected static Logger log = Logger.getLogger(AbstractInitiateLogin.class);

    /** 当前 SAML 部署配置 */
    protected SamlDeployment deployment;
    /** 会话存储，用于保存原始请求 URI 与当前动作状态 */
    protected SamlSessionStore sessionStore;
    /** 是否在发起登录前保存原始请求 URI */
    protected boolean saveRequestUri;

    public AbstractInitiateLogin(SamlDeployment deployment, SamlSessionStore sessionStore) {
        this(deployment, sessionStore, true);
    }

    public AbstractInitiateLogin(SamlDeployment deployment, SamlSessionStore sessionStore, boolean saveRequestUri) {
        this.deployment = deployment;
        this.sessionStore = sessionStore;
        this.saveRequestUri = saveRequestUri;
    }

    @Override
    public int getResponseCode() {
        return 0;
    }

    /**
     * 执行 SAML 登录挑战：构建 AuthnRequest 并发送至 IdP。
     *
     * @param httpFacade HTTP 门面
     * @return 始终返回 true，表示已处理挑战
     */
    @Override
    public boolean challenge(HttpFacade httpFacade) {
        try {
            SAML2AuthnRequestBuilder authnRequestBuilder = buildSaml2AuthnRequestBuilder(deployment);
            BaseSAML2BindingBuilder binding = createSaml2Binding(deployment);
            if (saveRequestUri) {
                sessionStore.saveRequest();
            }

            sendAuthnRequest(httpFacade, authnRequestBuilder, binding);
            sessionStore.setCurrentAction(SamlSessionStore.CurrentAction.LOGGING_IN);
        } catch (Exception e) {
            throw new RuntimeException("Could not create authentication request.", e);
        }
        return true;
    }

    /**
     * 根据部署配置创建 SAML2 绑定构建器，并按需配置请求签名。
     *
     * @param deployment SAML 部署配置
     * @return 配置完成的绑定构建器
     */
    public static BaseSAML2BindingBuilder createSaml2Binding(SamlDeployment deployment) {
        BaseSAML2BindingBuilder binding = new BaseSAML2BindingBuilder();

        if (deployment.getIDP().getSingleSignOnService().signRequest()) {

            binding.signatureAlgorithm(deployment.getSignatureAlgorithm());
            KeyPair keypair = deployment.getSigningKeyPair();
            if (keypair == null) {
                throw new RuntimeException("Signing keys not configured");
            }
            if (deployment.getSignatureCanonicalizationMethod() != null) {
                binding.canonicalizationMethod(deployment.getSignatureCanonicalizationMethod());
            }

            binding.signWith(null, keypair);
            // TODO: 作为 KEYCLOAK-3810 的一部分，向 SAML 文档添加 KeyID
            //   <related DocumentBuilder>.addExtension(new KeycloakKeySamlExtensionGenerator(<key ID>));
            binding.signDocument();
        }
        return binding;
    }

    /**
     * 根据部署配置构建 SAML2 AuthnRequest。
     *
     * <p>设置 Issuer、NameIDPolicy、ForceAuthn/IsPassive、协议绑定及
     * AssertionConsumerService URL 等参数。</p>
     *
     * @param deployment SAML 部署配置
     * @return AuthnRequest 构建器
     */
    public static SAML2AuthnRequestBuilder buildSaml2AuthnRequestBuilder(SamlDeployment deployment) {
        String issuerURL = deployment.getEntityID();
        String nameIDPolicyFormat = deployment.getNameIDPolicyFormat();

        if (nameIDPolicyFormat == null) {
            nameIDPolicyFormat =  JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get();
        }

        SingleSignOnService sso = deployment.getIDP().getSingleSignOnService();
        SAML2AuthnRequestBuilder authnRequestBuilder = new SAML2AuthnRequestBuilder()
                .destination(sso.getRequestBindingUrl())
                .issuer(issuerURL)
                .forceAuthn(deployment.isForceAuthentication()).isPassive(deployment.isIsPassive())
                .nameIdPolicy(SAML2NameIDPolicyBuilder
                    .format(nameIDPolicyFormat)
                    .setAllowCreate(Boolean.TRUE));
        if (sso.getResponseBinding() != null) {
            String protocolBinding = JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.get();
            if (sso.getResponseBinding() == SamlDeployment.Binding.POST) {
                protocolBinding = JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get();
            }
            authnRequestBuilder.protocolBinding(protocolBinding);

        }
        if (sso.getAssertionConsumerServiceUrl() != null) {
            authnRequestBuilder.assertionConsumerUrl(sso.getAssertionConsumerServiceUrl());
        }
        return authnRequestBuilder;
    }

    /**
     * 将 AuthnRequest 通过具体绑定（Redirect 或 POST）发送至 IdP。
     *
     * @param httpFacade HTTP 门面
     * @param authnRequestBuilder AuthnRequest 构建器
     * @param binding SAML2 绑定构建器
     */
    protected abstract void sendAuthnRequest(HttpFacade httpFacade, SAML2AuthnRequestBuilder authnRequestBuilder, BaseSAML2BindingBuilder binding) throws ProcessingException, ConfigurationException, IOException;

}
