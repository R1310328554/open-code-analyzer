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

package org.keycloak.adapters.saml.profile.ecp;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;

import org.keycloak.adapters.saml.AbstractInitiateLogin;
import org.keycloak.adapters.saml.OnSessionCreated;
import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlSessionStore;
import org.keycloak.adapters.saml.profile.AbstractSamlAuthenticationHandler;
import org.keycloak.adapters.saml.profile.SamlAuthenticationHandler;
import org.keycloak.adapters.saml.profile.SamlInvocationContext;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.dom.saml.v2.protocol.LogoutRequestType;
import org.keycloak.saml.BaseSAML2BindingBuilder;
import org.keycloak.saml.SAML2AuthnRequestBuilder;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.processing.core.saml.v2.util.DocumentUtil;
import org.keycloak.saml.processing.web.util.PostBindingUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * ECP（Enhanced Client or Proxy）配置文件下的 SAML 认证处理器。
 *
 * <p>支持 PAOS 绑定与 SOAP 封装，适用于非浏览器客户端（如 curl、Web 服务代理）
 * 通过 SAML ECP Profile 完成认证。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class EcpAuthenticationHandler extends AbstractSamlAuthenticationHandler {

    /** PAOS 请求头名称。 */
    public static final String PAOS_HEADER = "PAOS";
    /** PAOS 内容类型。 */
    public static final String PAOS_CONTENT_TYPE = "application/vnd.paos+xml";
    /** ECP 扩展命名空间前缀。 */
    private static final String NS_PREFIX_PROFILE_ECP = "ecp";
    /** SAML 协议命名空间前缀。 */
    private static final String NS_PREFIX_SAML_PROTOCOL = "samlp";
    /** SAML 断言命名空间前缀。 */
    private static final String NS_PREFIX_SAML_ASSERTION = "saml";
    /** PAOS 绑定命名空间前缀。 */
    private static final String NS_PREFIX_PAOS_BINDING = "paos";

    /**
     * 判断当前 HTTP 请求是否应使用 ECP 处理器。
     *
     * @param httpFacade HTTP 门面
     * @return 若 Accept/Content-Type 含 PAOS 类型则返回 {@code true}
     */
    public static boolean canHandle(HttpFacade httpFacade) {
        HttpFacade.Request request = httpFacade.getRequest();
        String acceptHeader = request.getHeader("Accept");
        String contentTypeHeader = request.getHeader("Content-Type");

        return (acceptHeader != null && acceptHeader.contains(PAOS_CONTENT_TYPE) && request.getHeader(PAOS_HEADER) != null)
                || (contentTypeHeader != null && contentTypeHeader.contains(PAOS_CONTENT_TYPE));
    }

    /**
     * 工厂方法：创建 ECP 认证处理器。
     *
     * @param facade       HTTP 门面
     * @param deployment   SAML 部署配置
     * @param sessionStore 会话存储
     * @return ECP 认证处理器实例
     */
    public static SamlAuthenticationHandler create(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        return new EcpAuthenticationHandler(facade, deployment, sessionStore);
    }

    /** 私有构造器，通过 {@link #create} 实例化。 */
    private  EcpAuthenticationHandler(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        super(facade, deployment, sessionStore);
    }

    /**
     * ECP 配置文件不支持 IdP 发起的 LogoutRequest。
     *
     * @param request    登出请求（未使用）
     * @param relayState 关联状态（未使用）
     * @return 始终抛出 {@link RuntimeException}
     */
    @Override
    protected AuthOutcome logoutRequest(LogoutRequestType request, String relayState) {
        throw new RuntimeException("Not supported.");
    }


    /**
     * 处理 ECP 认证：PAOS 头存在时走标准流程；否则从 SOAP Body 解析 SAML 响应。
     *
     * @param onCreateSession 会话创建回调
     * @return 认证结果
     */
    @Override
    public AuthOutcome handle(OnSessionCreated onCreateSession) {
        String header = facade.getRequest().getHeader(PAOS_HEADER);

        if (header != null) {
            return doHandle(new SamlInvocationContext(), onCreateSession);
        } else {
            try {
                MessageFactory messageFactory = MessageFactory.newInstance();
                SOAPMessage soapMessage = messageFactory.createMessage(null, facade.getRequest().getInputStream());
                SOAPBody soapBody = soapMessage.getSOAPBody();
                Node authnRequestNode = soapBody.getFirstChild();
                Document document = DocumentUtil.createDocument();

                document.appendChild(document.importNode(authnRequestNode, true));

                String samlResponse = PostBindingUtil.base64Encode(DocumentUtil.asString(document));

                return doHandle(new SamlInvocationContext(null, samlResponse, null), onCreateSession);
            } catch (Exception e) {
                throw new RuntimeException("Error creating fault message.", e);
            }
        }
    }

    /**
     * 创建 ECP 质询：通过 SOAP 消息封装 AuthnRequest 并附带 PAOS/ECP 头。
     *
     * @param saveChallenge 是否保存原始请求 URI 以便登录后重定向
     * @return ECP 登录质询实现
     */
    @Override
    protected AbstractInitiateLogin createChallenge(boolean saveChallenge) {
        return new AbstractInitiateLogin(deployment, sessionStore, saveChallenge) {
            @Override
            protected void sendAuthnRequest(HttpFacade httpFacade, SAML2AuthnRequestBuilder authnRequestBuilder, BaseSAML2BindingBuilder binding) {
                try {
                    MessageFactory messageFactory = MessageFactory.newInstance();
                    SOAPMessage message = messageFactory.createMessage();

                    SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();

                    envelope.addNamespaceDeclaration(NS_PREFIX_SAML_ASSERTION, JBossSAMLURIConstants.ASSERTION_NSURI.get());
                    envelope.addNamespaceDeclaration(NS_PREFIX_SAML_PROTOCOL, JBossSAMLURIConstants.PROTOCOL_NSURI.get());
                    envelope.addNamespaceDeclaration(NS_PREFIX_PAOS_BINDING, JBossSAMLURIConstants.PAOS_BINDING.get());
                    envelope.addNamespaceDeclaration(NS_PREFIX_PROFILE_ECP, JBossSAMLURIConstants.ECP_PROFILE.get());

                    createPaosRequestHeader(envelope);
                    createEcpRequestHeader(envelope);

                    SOAPBody body = envelope.getBody();

                    body.addDocument(binding.postBinding(authnRequestBuilder.toDocument()).getDocument());

                    message.writeTo(httpFacade.getResponse().getOutputStream());
                } catch (Exception e) {
                    throw new RuntimeException("Could not create AuthnRequest.", e);
                }
            }

            /** 构造 ECP Request SOAP 头，指定 SP 与 IdP 信息。 */
            private void createEcpRequestHeader(SOAPEnvelope envelope) throws SOAPException {
                SOAPHeader headers = envelope.getHeader();
                SOAPHeaderElement ecpRequestHeader = headers.addHeaderElement(envelope.createQName(JBossSAMLConstants.REQUEST.get(), NS_PREFIX_PROFILE_ECP));

                ecpRequestHeader.setMustUnderstand(true);
                ecpRequestHeader.setActor("http://schemas.xmlsoap.org/soap/actor/next");
                ecpRequestHeader.addAttribute(envelope.createName("ProviderName"), deployment.getEntityID());
                ecpRequestHeader.addAttribute(envelope.createName("IsPassive"), "0");
                ecpRequestHeader.addChildElement(envelope.createQName("Issuer", "saml")).setValue(deployment.getEntityID());
                ecpRequestHeader.addChildElement(envelope.createQName("IDPList", "samlp"))
                        .addChildElement(envelope.createQName("IDPEntry", "samlp"))
                        .addAttribute(envelope.createName("ProviderID"), deployment.getIDP().getEntityID())
                        .addAttribute(envelope.createName("Name"), deployment.getIDP().getEntityID())
                        .addAttribute(envelope.createName("Loc"), deployment.getIDP().getSingleSignOnService().getRequestBindingUrl());
            }

            /** 构造 PAOS Request SOAP 头，声明 ECP Profile 与响应消费 URL。 */
            private void createPaosRequestHeader(SOAPEnvelope envelope) throws SOAPException {
                SOAPHeader headers = envelope.getHeader();
                SOAPHeaderElement paosRequestHeader = headers.addHeaderElement(envelope.createQName(JBossSAMLConstants.REQUEST.get(), NS_PREFIX_PAOS_BINDING));

                paosRequestHeader.setMustUnderstand(true);
                paosRequestHeader.setActor("http://schemas.xmlsoap.org/soap/actor/next");
                paosRequestHeader.addAttribute(envelope.createName("service"), JBossSAMLURIConstants.ECP_PROFILE.get());
                paosRequestHeader.addAttribute(envelope.createName("responseConsumerURL"), getResponseConsumerUrl());
            }

            /** 返回断言消费服务 URL，用于 PAOS 头的 responseConsumerURL 属性。 */
            private String getResponseConsumerUrl() {
                return (deployment.getIDP() == null
                  || deployment.getIDP().getSingleSignOnService() == null
                  || deployment.getIDP().getSingleSignOnService().getAssertionConsumerServiceUrl() == null
                ) ? null
                  : deployment.getIDP().getSingleSignOnService().getAssertionConsumerServiceUrl().toString();
            }
        };
    }
}
