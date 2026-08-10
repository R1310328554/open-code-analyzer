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

package org.keycloak.adapters.saml.profile;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.adapters.saml.AbstractInitiateLogin;
import org.keycloak.adapters.saml.AdapterConstants;
import org.keycloak.adapters.saml.OnSessionCreated;
import org.keycloak.adapters.saml.SamlAuthenticationError;
import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlPrincipal;
import org.keycloak.adapters.saml.SamlSession;
import org.keycloak.adapters.saml.SamlSessionStore;
import org.keycloak.adapters.saml.SamlUtil;
import org.keycloak.adapters.saml.profile.webbrowsersso.WebBrowserSsoAuthenticationHandler;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.dom.saml.v2.SAML2Object;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.dom.saml.v2.assertion.AuthnStatementType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.assertion.StatementAbstractType;
import org.keycloak.dom.saml.v2.assertion.SubjectConfirmationDataType;
import org.keycloak.dom.saml.v2.assertion.SubjectConfirmationType;
import org.keycloak.dom.saml.v2.assertion.SubjectType;
import org.keycloak.dom.saml.v2.protocol.ExtensionsType;
import org.keycloak.dom.saml.v2.protocol.LogoutRequestType;
import org.keycloak.dom.saml.v2.protocol.RequestAbstractType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusCodeType;
import org.keycloak.dom.saml.v2.protocol.StatusResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusType;
import org.keycloak.rotation.KeyLocator;
import org.keycloak.saml.BaseSAML2BindingBuilder;
import org.keycloak.saml.SAML2AuthnRequestBuilder;
import org.keycloak.saml.SAMLRequestParser;
import org.keycloak.saml.SignatureAlgorithm;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.processing.api.saml.v2.sig.SAML2Signature;
import org.keycloak.saml.processing.api.util.DeflateUtil;
import org.keycloak.saml.processing.core.saml.v2.common.SAMLDocumentHolder;
import org.keycloak.saml.processing.core.saml.v2.util.AssertionUtil;
import org.keycloak.saml.processing.core.util.KeycloakKeySamlExtensionGenerator;
import org.keycloak.saml.processing.core.util.RedirectBindingSignatureUtil;
import org.keycloak.saml.processing.web.util.PostBindingUtil;
import org.keycloak.saml.validators.ConditionsValidator;
import org.keycloak.saml.validators.DestinationValidator;
import org.keycloak.saml.validators.SubjectConfirmationDataValidator;

import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.keycloak.adapters.saml.SamlPrincipal.DEFAULT_ROLE_ATTRIBUTE_NAME;

/**
 * SAML 认证处理器的抽象基类，实现请求/响应解析、签名校验、断言提取与会话建立等通用逻辑。
 *
 * <p>子类（如 {@link org.keycloak.adapters.saml.profile.webbrowsersso.WebBrowserSsoAuthenticationHandler}、
 * {@link org.keycloak.adapters.saml.profile.ecp.EcpAuthenticationHandler}）只需实现配置文件特有的
 * 登出与质询发送方式。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 */
public abstract class AbstractSamlAuthenticationHandler implements SamlAuthenticationHandler {

    /** 控制 SAML 重定向绑定解压最大字节数的系统属性名。 */
    public static final String MAX_INFLAFING_SIZE_PROP = "org.keycloak.adapters.saml.maxInflatingSize";
    /** 重定向绑定解压上限（字节），可通过系统属性覆盖。 */
    private static final long MAX_INFLAFING_SIZE = Long.getLong(MAX_INFLAFING_SIZE_PROP, DeflateUtil.DEFAULT_MAX_INFLATING_SIZE);
    /** 本类日志记录器。 */
    protected static Logger log = Logger.getLogger(WebBrowserSsoAuthenticationHandler.class);

    /** HTTP 请求/响应门面。 */
    protected final HttpFacade facade;
    /** SAML 会话存储。 */
    protected final SamlSessionStore sessionStore;
    /** SAML 部署配置。 */
    protected  final SamlDeployment deployment;
    /** 认证失败或需重定向时返回的质询对象。 */
    protected AuthChallenge challenge;
    /** Destination 字段校验器。 */
    private final DestinationValidator destinationValidator = DestinationValidator.forProtocolMap(null);
    /** 断言/文档提取失败时返回 403 的质询。 */
    private static final AuthChallenge CHALLENGE_EXTRACTION_FAILURE =  new AuthChallenge() {
            @Override
            public boolean challenge(HttpFacade exchange) {
                SamlAuthenticationError error = new SamlAuthenticationError(SamlAuthenticationError.Reason.EXTRACTION_FAILURE);
                exchange.getRequest().setError(error);
                exchange.getResponse().sendError(403);
                return true;
            }

            @Override
            public int getResponseCode() {
                return 403;
            }
        };
    /** SAML 签名无效时返回 403 的质询。 */
    private static final AuthChallenge CHALLENGE_INVALID_SIGNATURE = new AuthChallenge() {
            @Override
            public boolean challenge(HttpFacade exchange) {
                SamlAuthenticationError error = new SamlAuthenticationError(SamlAuthenticationError.Reason.INVALID_SIGNATURE);
                exchange.getRequest().setError(error);
                exchange.getResponse().sendError(403);
                return true;
            }

            @Override
            public int getResponseCode() {
                return 403;
            }
        };

    /**
     * 构造 SAML 认证处理器。
     *
     * @param facade       HTTP 门面
     * @param deployment   SAML 部署配置
     * @param sessionStore 会话存储
     */
    public AbstractSamlAuthenticationHandler(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        this.facade = facade;
        this.deployment = deployment;
        this.sessionStore = sessionStore;
    }

    /**
     * 根据 {@link SamlInvocationContext} 分发 SAML 请求/响应或检查缓存会话。
     *
     * @param context         SAML 调用上下文
     * @param onCreateSession 会话创建回调
     * @return 认证结果
     */
    public AuthOutcome doHandle(SamlInvocationContext context, OnSessionCreated onCreateSession) {
        String samlRequest = context.getSamlRequest();
        String samlResponse = context.getSamlResponse();
        String relayState = context.getRelayState();
        if (samlRequest != null) {
            return handleSamlRequest(samlRequest, relayState);
        } else if (samlResponse != null) {
            return handleSamlResponse(samlResponse, relayState, onCreateSession);
        } else if (sessionStore.isLoggedIn()) {
            if (verifySSL()) return failedTerminal();
            log.debug("AUTHENTICATED: was cached");
            return handleRequest();
        }
        return initiateLogin(true);
    }

    /** 已认证且无 SAML 参数时的默认处理：直接返回 {@link AuthOutcome#AUTHENTICATED}。 */
    protected AuthOutcome handleRequest() {
        return AuthOutcome.AUTHENTICATED;
    }

    /** {@inheritDoc} */
    @Override
    public AuthChallenge getChallenge() {
        return this.challenge;
    }

    /**
     * 解析并处理入站 SAML 请求（当前仅支持 {@link LogoutRequestType}）。
     *
     * @param samlRequest 编码后的 SAML 请求
     * @param relayState  关联状态
     * @return 认证结果
     */
    protected AuthOutcome handleSamlRequest(String samlRequest, String relayState) {
        SAMLDocumentHolder holder = null;
        boolean postBinding = false;
        String requestUri = facade.getRequest().getURI();
        if (facade.getRequest().getMethod().equalsIgnoreCase("GET")) {
            // 去除查询字符串，仅保留路径用于 Destination 校验
            int index = requestUri.indexOf('?');
            if (index > -1) {
                requestUri = requestUri.substring(0, index);
            }
            holder = SAMLRequestParser.parseRequestRedirectBinding(samlRequest, MAX_INFLAFING_SIZE);
        } else {
            postBinding = true;
            holder = SAMLRequestParser.parseRequestPostBinding(samlRequest);
        }
        if (holder == null) {
            log.error("Error parsing SAML document");
            return failedTerminal();
        }
        RequestAbstractType requestAbstractType = (RequestAbstractType) holder.getSamlObject();
        if (requestAbstractType.getDestination() == null && containsUnencryptedSignature(holder, postBinding)) {
            log.error("Destination field required.");
            return failed(CHALLENGE_EXTRACTION_FAILURE);
        }
        if (! destinationValidator.validate(requestUri, requestAbstractType.getDestination())) {
            log.error("Expected destination '" + requestUri + "' got '" + requestAbstractType.getDestination() + "'");
            return failedTerminal();
        }

        if (requestAbstractType instanceof LogoutRequestType) {
            if (deployment.getIDP().getSingleLogoutService().validateRequestSignature()) {
                try {
                    validateSamlSignature(holder, postBinding, GeneralConstants.SAML_REQUEST_KEY);
                } catch (VerificationException e) {
                    log.error("Failed to verify saml request signature", e);
                    return failedTerminal();
                }
            }
            LogoutRequestType logout = (LogoutRequestType) requestAbstractType;
            return logoutRequest(logout, relayState);

        } else {
            log.error("unknown SAML request type");
            return failedTerminal();
        }
    }

    /**
     * 处理 IdP 发起的登出请求，由子类实现具体逻辑。
     *
     * @param request    登出请求
     * @param relayState 关联状态
     * @return 认证结果
     */
    protected abstract AuthOutcome logoutRequest(LogoutRequestType request, String relayState);

    /**
     * 解析并处理入站 SAML 响应（LoginResponse、LogoutResponse 或错误状态）。
     *
     * @param samlResponse    编码后的 SAML 响应
     * @param relayState        关联状态
     * @param onCreateSession   会话创建回调
     * @return 认证结果
     */
    protected AuthOutcome handleSamlResponse(String samlResponse, String relayState, OnSessionCreated onCreateSession) {
        SAMLDocumentHolder holder = null;
        boolean postBinding = false;
        String requestUri = facade.getRequest().getURI();
        if (facade.getRequest().getMethod().equalsIgnoreCase("GET")) {
            int index = requestUri.indexOf('?');
            if (index > -1) {
                requestUri = requestUri.substring(0, index);
            }
            holder = extractRedirectBindingResponse(samlResponse);
        } else {
            postBinding = true;
            holder = extractPostBindingResponse(samlResponse);
        }
        if (holder == null) {
            log.error("Error parsing SAML document");
            return failed(CHALLENGE_EXTRACTION_FAILURE);
        }
        final StatusResponseType statusResponse = (StatusResponseType) holder.getSamlObject();
        // 校验 Destination 与当前请求 URI 一致
        if (statusResponse.getDestination() == null && containsUnencryptedSignature(holder, postBinding)) {
            log.error("Destination field required.");
            return failed(CHALLENGE_EXTRACTION_FAILURE);
        }
        if (! destinationValidator.validate(requestUri, statusResponse.getDestination())) {
            log.error("Request URI '" + requestUri + "' does not match SAML request destination '" + statusResponse.getDestination() + "'");
            return failedTerminal();
        }

        if (statusResponse instanceof ResponseType) {
            try {
                if (deployment.getIDP().getSingleSignOnService().validateResponseSignature()) {
                    try {
                        validateSamlSignature(holder, postBinding, GeneralConstants.SAML_RESPONSE_KEY);
                    } catch (VerificationException e) {
                        log.error("Failed to verify saml response signature", e);

                        return failed(CHALLENGE_INVALID_SIGNATURE);
                    }
                }
                return handleLoginResponse(holder, postBinding, onCreateSession);
            } finally {
                sessionStore.setCurrentAction(SamlSessionStore.CurrentAction.NONE);
            }

        } else {
            if (sessionStore.isLoggingOut()) {
                try {
                    if (deployment.getIDP().getSingleLogoutService().validateResponseSignature()) {
                        try {
                            validateSamlSignature(holder, postBinding, GeneralConstants.SAML_RESPONSE_KEY);
                        } catch (VerificationException e) {
                            log.error("Failed to verify saml response signature", e);
                            return failedTerminal();
                        }
                    }
                    return handleLogoutResponse(holder, statusResponse, relayState);
                } finally {
                    sessionStore.setCurrentAction(SamlSessionStore.CurrentAction.NONE);
                }

            } else if (sessionStore.isLoggingIn()) {

                try {
                    // KEYCLOAK-2107 — 被动模式下用户未认证：返回 NOT_AUTHENTICATED 供上层机制处理
                    StatusType status = statusResponse.getStatus();
                    if(checkStatusCodeValue(status.getStatusCode(), JBossSAMLURIConstants.STATUS_RESPONDER.get()) && checkStatusCodeValue(status.getStatusCode().getStatusCode(), JBossSAMLURIConstants.STATUS_NO_PASSIVE.get())){
                        log.debug("Not authenticated due passive mode Status found in SAML response: " + status.toString());
                        return AuthOutcome.NOT_AUTHENTICATED;
                    }

                    return failed(createAuthChallenge403(statusResponse));
                } finally {
                    sessionStore.setCurrentAction(SamlSessionStore.CurrentAction.NONE);
                }
            }

            log.warn("Keycloak Adapter obtained Response, that is not understood. This may be because the containers " +
                    "cookies are not properly configured with SameSite settings. Refer to KEYCLOAK-14103 for more details.");

            return AuthOutcome.NOT_ATTEMPTED;
        }

    }

    /** 判断 SAML 文档是否包含未加密的签名（POST 查 DOM，Redirect 查 SigAlg 参数）。 */
    private boolean containsUnencryptedSignature(SAMLDocumentHolder documentHolder, boolean postBinding) {
        if (postBinding) {
            Document signedDoc = documentHolder.getSamlDocument();
            NodeList nl = signedDoc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            return nl != null && nl.getLength() > 0;
        } else {
            String algorithm = facade.getRequest().getQueryParamValue(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);
            return algorithm != null;
        }
    }

    /** 按绑定类型校验 SAML 消息签名。 */
    private void validateSamlSignature(SAMLDocumentHolder holder, boolean postBinding, String paramKey) throws VerificationException {
        KeyLocator signatureValidationKey = deployment.getIDP().getSignatureValidationKeyLocator();
        if (postBinding) {
            verifyPostBindingSignature(holder.getSamlDocument(), signatureValidationKey);
        } else {
            String keyId = getMessageSigningKeyId(holder.getSamlObject());
            verifyRedirectBindingSignature(paramKey, signatureValidationKey, keyId);
        }
    }

    /** 从 SAML 扩展元素中提取消息签名密钥 ID。 */
    private String getMessageSigningKeyId(SAML2Object doc) {
        final ExtensionsType extensions;
        if (doc instanceof RequestAbstractType) {
            extensions = ((RequestAbstractType) doc).getExtensions();
        } else if (doc instanceof StatusResponseType) {
            extensions = ((StatusResponseType) doc).getExtensions();
        } else {
            return null;
        }

        if (extensions == null) {
            return null;
        }

        for (Object ext : extensions.getAny()) {
            if (! (ext instanceof Element)) {
                continue;
            }

            String res = KeycloakKeySamlExtensionGenerator.getMessageSigningKeyIdFromElement((Element) ext);

            if (res != null) {
                return res;
            }
        }

        return null;
    }

    /** 递归检查 SAML StatusCode 是否匹配期望值。 */
    private boolean checkStatusCodeValue(StatusCodeType statusCode, String expectedValue){
        if(statusCode != null && statusCode.getValue()!=null){
            String v = statusCode.getValue().toString();
            return expectedValue.equals(v);
        }
        return false;
    }

    /**
     * 处理 IdP 返回的 LoginResponse：校验断言、提取角色与属性并建立本地会话。
     *
     * @param responseHolder  SAML 响应文档持有者
     * @param postBinding     是否为 POST 绑定
     * @param onCreateSession 会话创建回调
     * @return 认证结果
     */
    protected AuthOutcome handleLoginResponse(SAMLDocumentHolder responseHolder, boolean postBinding, OnSessionCreated onCreateSession) {
        if (!sessionStore.isLoggingIn()) {
            log.warn("Adapter obtained LoginResponse, however containers session is not aware of sending any request. " +
                    "This may be because the session cookies created by container are not properly configured " +
                    "with SameSite settings. Refer to KEYCLOAK-14103 for more details.");
        }

    	final ResponseType responseType = (ResponseType) responseHolder.getSamlObject();
        AssertionType assertion = null;
        if (isRetrayableSamlResponse(responseType)) {
            // 发起登录但不保存请求 URI（当前路径为 /saml 端点）
            return initiateLogin(false);
        } else if (!isSuccessfulSamlResponse(responseType) || responseType.getAssertions() == null || responseType.getAssertions().isEmpty()) {
            return failed(createAuthChallenge403(responseType));
        }

        Element assertionElement = null;
        boolean isAssertionEncrypted = false;
        try {
            isAssertionEncrypted = AssertionUtil.isAssertionEncrypted(responseType);
            assertionElement = isAssertionEncrypted
                    ? AssertionUtil.decryptAssertion(responseType, deployment.getDecryptionKey())
                    : AssertionUtil.getAssertionElement(responseHolder);
            assertion = responseType.getAssertions().get(0).getAssertion();
            ConditionsValidator.Builder cvb = new ConditionsValidator.Builder(assertion.getID(), assertion.getConditions(), destinationValidator);
            SubjectConfirmationDataValidator.Builder scdvb = new SubjectConfirmationDataValidator.Builder(assertion.getID(), getSubjectConfirmationData(assertion), destinationValidator)
                    .clockSkewInMillis(deployment.getIDP().getAllowedClockSkew());
            try {
                cvb.clockSkewInMillis(deployment.getIDP().getAllowedClockSkew());
                cvb.addAllowedAudience(URI.create(deployment.getEntityID()));
                if (responseType.getDestination() != null) {
                  // Destination 已与请求 URL 校验一致，可加入 Audience 与 Recipient
                  cvb.addAllowedAudience(URI.create(responseType.getDestination()));
                  scdvb.allowedRecipient(responseType.getDestination());
                }

            } catch (IllegalArgumentException ex) {
                // DeploymentBuilder 中已输出警告
            }
            if (!cvb.build().isValid() || !scdvb.build().isValid()) {
                // 条件无效时重新发起登录
                return initiateLogin(false);
            }
        } catch (Exception e) {
            log.error("Error extracting SAML assertion: " + e.getMessage());
            return failed(CHALLENGE_EXTRACTION_FAILURE);
        }

        if (deployment.getIDP().getSingleSignOnService().validateAssertionSignature()
                || (deployment.getIDP().getSingleSignOnService().validateResponseSignature()
                && postBinding && isAssertionEncrypted
                && !AssertionUtil.isSignedElement(responseHolder.getSamlDocument().getDocumentElement()))) {
            try {
                if (!AssertionUtil.isSignatureValid(assertionElement, deployment.getIDP().getSignatureValidationKeyLocator())) {
                    log.error("Failed to verify saml assertion signature");
                    return failed(CHALLENGE_INVALID_SIGNATURE);
                }
            } catch (Exception e) {
                log.error("Error processing validation of SAML assertion: " + e.getMessage());
                return failed(CHALLENGE_EXTRACTION_FAILURE);
            }
        }

        SubjectType subject = assertion.getSubject();
        SubjectType.STSubType subType = subject.getSubType();
        NameIDType subjectNameID = subType == null ? null : (NameIDType) subType.getBaseID();
        String principalName = subjectNameID == null ? null : subjectNameID.getValue();

        Set<String> roles = new HashSet<>();
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
        MultivaluedHashMap<String, String> friendlyAttributes = new MultivaluedHashMap<>();

        Set<StatementAbstractType> statements = assertion.getStatements();
        for (StatementAbstractType statement : statements) {
            if (statement instanceof AttributeStatementType) {
                AttributeStatementType attributeStatement = (AttributeStatementType) statement;
                List<AttributeStatementType.ASTChoiceType> attList = attributeStatement.getAttributes();
                for (AttributeStatementType.ASTChoiceType obj : attList) {
                    AttributeType attr = obj.getAttribute();
                    if (isRole(attr)) {
                        List<Object> attributeValues = attr.getAttributeValue();
                        if (attributeValues != null) {
                            for (Object attrValue : attributeValues) {
                                String role = getAttributeValue(attrValue);
                                log.debugv("Add role: {0}", role);
                                roles.add(role);
                            }
                        }
                    } else {
                        List<Object> attributeValues = attr.getAttributeValue();
                        if (attributeValues != null) {
                            for (Object attrValue : attributeValues) {
                                String value = getAttributeValue(attrValue);
                                if (attr.getName() != null) {
                                    attributes.add(attr.getName(), value);
                                }
                                if (attr.getFriendlyName() != null) {
                                    friendlyAttributes.add(attr.getFriendlyName(), value);
                                }
                            }
                        }
                    }

                }
            }
        }

        if (deployment.getPrincipalNamePolicy() == SamlDeployment.PrincipalNamePolicy.FROM_ATTRIBUTE) {
            if (deployment.getPrincipalAttributeName() != null) {
                String attribute = attributes.getFirst(deployment.getPrincipalAttributeName());
                if (attribute != null) principalName = attribute;
                else {
                    attribute = friendlyAttributes.getFirst(deployment.getPrincipalAttributeName());
                    if (attribute != null) principalName = attribute;
                }
            }
        }

        // 通过配置的角色映射提供器转换角色（如 LDAP 组映射）
        if (deployment.getRoleMappingsProvider() != null)  {
            roles = deployment.getRoleMappingsProvider().map(principalName, roles);
        }

        // 角色同时作为普通属性写入，主要供 Elytron ABAC 使用
        attributes.put(DEFAULT_ROLE_ATTRIBUTE_NAME, new ArrayList<>(roles));

        AuthnStatementType authn = null;
        for (Object statement : assertion.getStatements()) {
            if (statement instanceof AuthnStatementType) {
                authn = (AuthnStatementType) statement;
                break;
            }
        }


        URI nameFormat = subjectNameID == null ? null : subjectNameID.getFormat();
        String nameFormatString = nameFormat == null ? JBossSAMLURIConstants.NAMEID_FORMAT_UNSPECIFIED.get() : nameFormat.toString();
        final SamlPrincipal principal = new SamlPrincipal(assertion,
                deployment.isKeepDOMAssertion()? getAssertionDocumentFromElement(assertionElement) : null,
                principalName, principalName, nameFormatString, attributes, friendlyAttributes);
        final String sessionIndex = authn == null ? null : authn.getSessionIndex();
        final XMLGregorianCalendar sessionNotOnOrAfter = authn == null ? null : authn.getSessionNotOnOrAfter();
        SamlSession account = new SamlSession(principal, roles, sessionIndex, sessionNotOnOrAfter);
        sessionStore.saveAccount(account);
        onCreateSession.onSessionCreated(account);

        // 重定向至登录前保存的原始请求
        String redirectUri = sessionStore.getRedirectUri();
        if (redirectUri != null) {
            facade.getResponse().setHeader("Location", redirectUri);
            facade.getResponse().setStatus(302);
            facade.getResponse().end();
        } else {
            log.debug("IDP initiated invocation");
        }
        log.debug("AUTHENTICATED authn");

        return AuthOutcome.AUTHENTICATED;
    }

    /** 设置质询并返回 {@link AuthOutcome#FAILED}。 */
    private AuthOutcome failed(AuthChallenge challenge) {
        this.challenge = challenge;
        return AuthOutcome.FAILED;
    }

    /**
     * 标记失败但不向调用方返回质询（终端失败）。
     *
     * @return {@link AuthOutcome#FAILED}
     */
    private AuthOutcome failedTerminal() {
        return failed(null);
    }

    /** 从断言中提取 Bearer 类型的 SubjectConfirmationData。 */
    private SubjectConfirmationDataType getSubjectConfirmationData(AssertionType assertion) {
        if (assertion != null
                && assertion.getSubject() != null
                && assertion.getSubject().getConfirmation() != null) {
            return assertion.getSubject().getConfirmation().stream()
                    .filter(c -> JBossSAMLURIConstants.SUBJECT_CONFIRMATION_BEARER.get().equals(c.getMethod()))
                    .findFirst()
                    .map(SubjectConfirmationType::getSubjectConfirmationData)
                    .orElse(null);
        }
        return null;
    }

    /** 判断 SAML 响应 Status 是否为 Success。 */
    private boolean isSuccessfulSamlResponse(ResponseType responseType) {
        return responseType != null
          && responseType.getStatus() != null
          && responseType.getStatus().getStatusCode() != null
          && responseType.getStatus().getStatusCode().getValue() != null
          && Objects.equals(responseType.getStatus().getStatusCode().getValue().toString(), JBossSAMLURIConstants.STATUS_SUCCESS.get());
    }

    /** 判断是否为可重试的认证过期响应（AuthenticationExpiredMessage）。 */
    private boolean isRetrayableSamlResponse(ResponseType responseType) {
        if (responseType == null || responseType.getStatus() == null) {
            return false;
        }

        StatusType status = responseType.getStatus();
        return status.getStatusCode() != null
          && AdapterConstants.AUTHENTICATION_EXPIRED_MESSAGE.equals(status.getStatusMessage())
          && status.getStatusCode().getValue() != null
          && Objects.equals(status.getStatusCode().getValue().toString(), JBossSAMLURIConstants.STATUS_RESPONDER.get())
          && status.getStatusCode().getStatusCode() != null
          && status.getStatusCode().getStatusCode().getValue() != null
          && Objects.equals(status.getStatusCode().getStatusCode().getValue().toString(), JBossSAMLURIConstants.STATUS_AUTHNFAILED.get());
    }

    /** 将断言 DOM 元素封装为独立 Document（用于 keepDOMAssertion 配置）。 */
    private Document getAssertionDocumentFromElement(final Element assertionElement) {
        if (assertionElement == null) {
            return null;
        }
        try {
            Document assertionDoc = DocumentUtil.createDocument();
            assertionDoc.adoptNode(assertionElement);
            assertionDoc.appendChild(assertionElement);
            return assertionDoc;
        } catch (ConfigurationException e) {
            log.warn("Cannot obtain DOM assertion document", e);
            return null;
        }
    }

    /** 从断言属性值节点提取字符串（支持 String、Node、NameIDType）。 */
    private String getAttributeValue(Object attrValue) {
        if (attrValue == null) {
            return "";
        } else if (attrValue instanceof String) {
            return (String) attrValue;
        } else if (attrValue instanceof Node) {
            Node roleNode = (Node) attrValue;
            return roleNode.getFirstChild().getNodeValue();
        } else if (attrValue instanceof NameIDType) {
            NameIDType nameIdType = (NameIDType) attrValue;
            return nameIdType.getValue();
        } else {
            log.warn("Unable to extract unknown SAML assertion attribute value type: " + attrValue.getClass().getName());
        }
        return null;
    }

    /** 判断 SAML 属性是否表示角色（按 name 或 friendlyName 匹配部署配置）。 */
    protected boolean isRole(AttributeType attribute) {
        return (attribute.getName() != null && deployment.getRoleAttributeNames().contains(attribute.getName())) || (attribute.getFriendlyName() != null && deployment.getRoleAttributeNames().contains(attribute.getFriendlyName()));
    }

    /**
     * 处理 IdP 返回的 LogoutResponse：在 relayState 为 {@code logout} 时清除本地会话。
     *
     * @param holder       SAML 文档持有者
     * @param responseType 状态响应
     * @param relayState   关联状态
     * @return 认证结果
     */
    protected AuthOutcome handleLogoutResponse(SAMLDocumentHolder holder, StatusResponseType responseType, String relayState) {
        boolean loggedIn = sessionStore.isLoggedIn();
        if (!loggedIn || !"logout".equals(relayState)) {
            return AuthOutcome.NOT_ATTEMPTED;
        }
        sessionStore.logoutAccount();
        return AuthOutcome.LOGGED_OUT;
    }

    /** 从重定向绑定参数解析 SAML 响应。 */
    protected SAMLDocumentHolder extractRedirectBindingResponse(String response) {
        return SAMLRequestParser.parseRequestRedirectBinding(response, MAX_INFLAFING_SIZE);
    }


    /** 从 POST 绑定 Base64 载荷解析 SAML 响应。 */
    protected SAMLDocumentHolder extractPostBindingResponse(String response) {
        byte[] samlBytes = PostBindingUtil.base64Decode(response);
        return SAMLRequestParser.parseResponseDocument(samlBytes);
    }


    /**
     * 发起 IdP 登录：创建质询并返回 {@link AuthOutcome#NOT_ATTEMPTED}。
     *
     * @param saveRequestUri 是否保存当前请求 URI 以便登录后重定向
     * @return 认证结果
     */
    protected AuthOutcome initiateLogin(boolean saveRequestUri) {
        challenge = createChallenge(saveRequestUri);
        return AuthOutcome.NOT_ATTEMPTED;
    }

    /**
     * 创建登录质询实现，由子类覆盖以支持不同配置文件（Browser/ECP）。
     *
     * @param saveRequestUri 是否保存原始请求
     * @return 登录质询
     */
    protected AbstractInitiateLogin createChallenge(boolean saveRequestUri) {
        return new AbstractInitiateLogin(deployment, sessionStore, saveRequestUri) {
            @Override
            protected void sendAuthnRequest(HttpFacade httpFacade, SAML2AuthnRequestBuilder authnRequestBuilder, BaseSAML2BindingBuilder binding) throws ProcessingException, ConfigurationException, IOException {
                if (isAutodetectedBearerOnly(httpFacade.getRequest())) {
                    httpFacade.getResponse().setStatus(401);
                    httpFacade.getResponse().end();
                }
                else {
                    Document document = authnRequestBuilder.toDocument();
                    SamlDeployment.Binding samlBinding = deployment.getIDP().getSingleSignOnService().getRequestBinding();
                    SamlUtil.sendSaml(true, httpFacade, deployment.getIDP().getSingleSignOnService().getRequestBindingUrl(), binding, document, samlBinding);
                }
            }
        };
    }

    /** 校验当前请求是否满足部署配置的 SSL 要求。 */
    protected boolean verifySSL() {
        if (!facade.getRequest().isSecure() && deployment.getSslRequired().isRequired(facade.getRequest().getRemoteAddr())) {
            log.warn("SSL is required to authenticate");
            return true;
        }
        return false;
    }

    /** 校验 POST 绑定 SAML 文档的 XML 签名。 */
    public void verifyPostBindingSignature(Document document, KeyLocator keyLocator) throws VerificationException {
        SAML2Signature saml2Signature = new SAML2Signature();
        try {
            if (!saml2Signature.validate(document, keyLocator)) {
                throw new VerificationException("Invalid signature on document");
            }
        } catch (ProcessingException e) {
            throw new VerificationException("Error validating signature", e);
        }
    }

    /** 校验重定向绑定查询字符串的签名。 */
    private void verifyRedirectBindingSignature(String paramKey, KeyLocator keyLocator, String keyId) throws VerificationException {
        String request = facade.getRequest().getQueryParamValue(paramKey);
        String algorithm = facade.getRequest().getQueryParamValue(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);
        String signature = facade.getRequest().getQueryParamValue(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
        String decodedAlgorithm = facade.getRequest().getQueryParamValue(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);

        if (request == null) {
            throw new VerificationException("SAML Request was null");
        }
        if (algorithm == null) throw new VerificationException("SigAlg was null");
        if (signature == null) throw new VerificationException("Signature was null");

        // Shibboleth 在重定向绑定中不对文档签名
        // todo 可考虑增加配置开关

        String relayState = facade.getRequest().getQueryParamValue(GeneralConstants.RELAY_STATE);
        KeycloakUriBuilder builder = KeycloakUriBuilder.fromPath("/")
                .queryParam(paramKey, request);
        if (relayState != null) {
            builder.queryParam(GeneralConstants.RELAY_STATE, relayState);
        }
        builder.queryParam(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY, algorithm);
        String rawQuery = builder.build().getRawQuery();

        try {
            //byte[] decodedSignature = RedirectBindingUtil.urlBase64Decode(signature);
            byte[] decodedSignature = Base64.getMimeDecoder().decode(signature);
            byte[] rawQueryBytes = rawQuery.getBytes(StandardCharsets.UTF_8);

            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.getFromXmlMethod(decodedAlgorithm);

            if (!RedirectBindingSignatureUtil.validateRedirectBindingSignature(signatureAlgorithm, rawQueryBytes, decodedSignature, keyLocator, keyId)) {
                throw new VerificationException("Invalid query param signature");
            }
        } catch (Exception e) {
            throw new VerificationException(e);
        }
    }

    /**
     * 自动检测 Bearer-Only 请求（XHR、JSF partial、SOAP 或非 HTML Accept）。
     *
     * @param request HTTP 请求
     * @return 若应返回 401 而非重定向则返回 {@code true}
     */
    protected boolean isAutodetectedBearerOnly(HttpFacade.Request request) {
        if (!deployment.isAutodetectBearerOnly()) return false;

        String headerValue = facade.getRequest().getHeader(GeneralConstants.HTTP_HEADER_X_REQUESTED_WITH);
        if (headerValue != null && headerValue.equalsIgnoreCase("XMLHttpRequest")) {
            return true;
        }

        headerValue = facade.getRequest().getHeader("Faces-Request");
        if (headerValue != null && headerValue.startsWith("partial/")) {
            return true;
        }

        headerValue = facade.getRequest().getHeader("SOAPAction");
        if (headerValue != null) {
            return true;
        }

        List<String> accepts = facade.getRequest().getHeaders("Accept");
        if (accepts == null) accepts = Collections.emptyList();

        for (String accept : accepts) {
            if (accept.contains("text/html") || accept.contains("text/*") || accept.contains("*/*")) {
                return false;
            }
        }

        return true;
    }

    /** 创建指定 HTTP 错误码与 SAML 错误类型的认证质询。 */
    private static AuthChallenge createAuthChallenge(final int httpError, final SamlAuthenticationError error) {
        return new AuthChallenge() {
            @Override
            public boolean challenge(HttpFacade exchange) {
                exchange.getRequest().setError(error);
                exchange.getResponse().sendError(httpError);
                return true;
            }

            @Override
            public int getResponseCode() {
                return httpError;
            }
        };
    }

    /** 基于 SAML 错误状态创建 403 质询。 */
    private static AuthChallenge createAuthChallenge403(final StatusResponseType responseType) {
        return createAuthChallenge(403, new SamlAuthenticationError(SamlAuthenticationError.Reason.ERROR_STATUS, responseType));
    }

}
