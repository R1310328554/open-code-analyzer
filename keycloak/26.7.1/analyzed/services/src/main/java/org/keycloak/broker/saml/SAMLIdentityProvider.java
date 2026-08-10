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
package org.keycloak.broker.saml;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.IdentityProviderDataMarshaller;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AuthnStatementType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.assertion.SubjectType;
import org.keycloak.dom.saml.v2.metadata.AttributeConsumingServiceType;
import org.keycloak.dom.saml.v2.metadata.EndpointType;
import org.keycloak.dom.saml.v2.metadata.EntityDescriptorType;
import org.keycloak.dom.saml.v2.metadata.KeyDescriptorType;
import org.keycloak.dom.saml.v2.metadata.KeyTypes;
import org.keycloak.dom.saml.v2.metadata.LocalizedNameType;
import org.keycloak.dom.saml.v2.protocol.ArtifactResolveType;
import org.keycloak.dom.saml.v2.protocol.AuthnRequestType;
import org.keycloak.dom.saml.v2.protocol.LogoutRequestType;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.keys.PublicKeyStorageUtils;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeyManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.saml.JaxrsSAML2BindingBuilder;
import org.keycloak.protocol.saml.SAMLEncryptionAlgorithms;
import org.keycloak.protocol.saml.SamlMetadataPublicKeyLoader;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.protocol.saml.SamlService;
import org.keycloak.protocol.saml.SamlSessionUtils;
import org.keycloak.protocol.saml.mappers.SamlMetadataDescriptorUpdater;
import org.keycloak.protocol.saml.preprocessor.SamlAuthenticationPreprocessor;
import org.keycloak.protocol.saml.profile.util.Soap;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.saml.SAML2ArtifactResolveRequestBuilder;
import org.keycloak.saml.SAML2AuthnRequestBuilder;
import org.keycloak.saml.SAML2LogoutRequestBuilder;
import org.keycloak.saml.SAML2NameIDPolicyBuilder;
import org.keycloak.saml.SAML2RequestedAuthnContextBuilder;
import org.keycloak.saml.SPMetadataDescriptor;
import org.keycloak.saml.SamlProtocolExtensionsAwareBuilder.NodeGenerator;
import org.keycloak.saml.SignatureAlgorithm;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.processing.api.saml.v2.request.SAML2Request;
import org.keycloak.saml.processing.api.saml.v2.response.SAML2Response;
import org.keycloak.saml.processing.core.saml.v2.common.SAMLDocumentHolder;
import org.keycloak.saml.processing.core.saml.v2.util.SAMLMetadataUtil;
import org.keycloak.saml.processing.core.util.KeycloakKeySamlExtensionGenerator;
import org.keycloak.saml.validators.DestinationValidator;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.Booleans;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SAML 2.0 身份代理实现：发起 AuthnRequest、处理断言并完成 SP 元数据导出。
 * @author Pedro Igor
 */
public class SAMLIdentityProvider extends AbstractIdentityProvider<SAMLIdentityProviderConfig> {
    /** 日志记录器。 */
    protected static final Logger logger = Logger.getLogger(SAMLIdentityProvider.class);

    /** SAML Destination 校验器。 */
    private final DestinationValidator destinationValidator;
    /** @param session Keycloak 会话
     * @param config SAML IdP 配置
     * @param destinationValidator Destination 校验器 */
    public SAMLIdentityProvider(KeycloakSession session, SAMLIdentityProviderConfig config, DestinationValidator destinationValidator) {
        super(session, config);
        this.destinationValidator = destinationValidator;
    }

    /** 返回 {@link SAMLEndpoint} 作为 SAML 回调 JAX-RS 资源。 */
    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new SAMLEndpoint(session, this, getConfig(), callback, destinationValidator);
    }

    /** 构建并签名 AuthnRequest，经 Redirect 或 POST 绑定发送至 IdP SSO URL。 */
    @Override
    public Response performLogin(AuthenticationRequest request) {
        try {
            UriInfo uriInfo = request.getUriInfo();
            RealmModel realm = request.getRealm();
            String issuerURL = getEntityId(uriInfo, realm);
            String destinationUrl = getConfig().getSingleSignOnServiceUrl();
            String nameIDPolicyFormat = getConfig().getNameIDPolicyFormat();

            if (nameIDPolicyFormat == null) {
                nameIDPolicyFormat =  JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get();
            }

            String protocolBinding = JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.get();

            String assertionConsumerServiceUrl = request.getRedirectUri();

            if (getConfig().isArtifactBindingResponse()) {
                protocolBinding = JBossSAMLURIConstants.SAML_HTTP_ARTIFACT_BINDING.get();
            } else if (getConfig().isPostBindingResponse()) {
                protocolBinding = JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get();
            }

            SAML2RequestedAuthnContextBuilder requestedAuthnContext =
                new SAML2RequestedAuthnContextBuilder()
                    .setComparison(getConfig().getAuthnContextComparisonType());

            for (String authnContextClassRef : getAuthnContextClassRefUris())
                requestedAuthnContext.addAuthnContextClassRef(authnContextClassRef);

            for (String authnContextDeclRef : getAuthnContextDeclRefUris())
                requestedAuthnContext.addAuthnContextDeclRef(authnContextDeclRef);

            Integer attributeConsumingServiceIndex = getConfig().getAttributeConsumingServiceIndex();

            String loginHint = Booleans.isTrue(getConfig().isLoginHint()) ? request.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM) : null;
            Boolean allowCreate = null;
            if (getConfig().getConfig().get(SAMLIdentityProviderConfig.ALLOW_CREATE) == null || getConfig().isAllowCreate())
                allowCreate = Boolean.TRUE;
            LoginProtocol protocol = session.getProvider(LoginProtocol.class, request.getAuthenticationSession().getProtocol());
            Boolean forceAuthn = getConfig().isForceAuthn();
            if (protocol.requireReauthentication(null, request.getAuthenticationSession()))
                forceAuthn = Boolean.TRUE;
            SAML2AuthnRequestBuilder authnRequestBuilder = new SAML2AuthnRequestBuilder()
                    .assertionConsumerUrl(assertionConsumerServiceUrl)
                    .destination(destinationUrl)
                    .issuer(issuerURL)
                    .forceAuthn(forceAuthn)
                    .protocolBinding(protocolBinding)
                    .nameIdPolicy(SAML2NameIDPolicyBuilder
                        .format(nameIDPolicyFormat)
                        .setAllowCreate(allowCreate))
                    .attributeConsumingServiceIndex(attributeConsumingServiceIndex)
                    .requestedAuthnContext(requestedAuthnContext)
                    .subject(loginHint);

            JaxrsSAML2BindingBuilder binding = new JaxrsSAML2BindingBuilder(session)
                    .relayState(request.getState().getEncoded());
            boolean postBinding = getConfig().isPostBindingAuthnRequest();

            if (getConfig().isWantAuthnRequestsSigned()) {
                KeyManager.ActiveRsaKey keys = session.keys().getActiveRsaKey(realm);

                String keyName = getConfig().getXmlSigKeyInfoKeyNameTransformer().getKeyName(keys.getKid(), keys.getCertificate());
                binding.signWith(keyName, keys.getPrivateKey(), keys.getPublicKey(), keys.getCertificate())
                        .signatureAlgorithm(getSignatureAlgorithm())
                        .signDocument();
                if (! postBinding && getConfig().isAddExtensionsElementWithKeyInfo()) {    // Only include extension if REDIRECT binding and signing whole SAML protocol message
                    authnRequestBuilder.addExtension(new KeycloakKeySamlExtensionGenerator(keyName));
                }
            }

            AuthnRequestType authnRequest = authnRequestBuilder.createAuthnRequest();
            for(Iterator<SamlAuthenticationPreprocessor> it = SamlSessionUtils.getSamlAuthenticationPreprocessorIterator(session); it.hasNext(); ) {
                authnRequest = it.next().beforeSendingLoginRequest(authnRequest, request.getAuthenticationSession());
            }

            if (authnRequest.getDestination() != null) {
                destinationUrl = authnRequest.getDestination().toString();
            }

            // 将 RequestID 存入认证会话，供响应 InResponseTo 校验
            request.getAuthenticationSession().setClientNote(SamlProtocol.SAML_REQUEST_ID_BROKER, authnRequest.getID());

            if (postBinding) {
                return binding.postBinding(SAML2Request.convert(authnRequest)).request(destinationUrl);
            } else {
                return binding.redirectBinding(SAML2Request.convert(authnRequest)).request(destinationUrl);
            }
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not create authentication request.", e);
        }
    }

    /** 返回 SP Entity ID（配置值或 realm 默认 URL）。 */
    private String getEntityId(UriInfo uriInfo, RealmModel realm) {
        String configEntityId = getConfig().getEntityId();

        if (configEntityId == null || configEntityId.isEmpty())
            return UriBuilder.fromUri(uriInfo.getBaseUri()).path("realms").path(realm.getName()).build().toString();
        else
            return configEntityId;
    }

    private List<String> getAuthnContextClassRefUris() {
        String authnContextClassRefs = getConfig().getAuthnContextClassRefs();
        if (authnContextClassRefs == null || authnContextClassRefs.isEmpty())
            return new LinkedList<String>();

        try {
            return Arrays.asList(JsonSerialization.readValue(authnContextClassRefs, String[].class));
        } catch (Exception e) {
            logger.warn("Could not json-deserialize AuthContextClassRefs config entry: " + authnContextClassRefs, e);
            return new LinkedList<String>();
        }
    }

    private List<String> getAuthnContextDeclRefUris() {
        String authnContextDeclRefs = getConfig().getAuthnContextDeclRefs();
        if (authnContextDeclRefs == null || authnContextDeclRefs.isEmpty())
            return new LinkedList<String>();

        try {
            return Arrays.asList(JsonSerialization.readValue(authnContextDeclRefs, String[].class));
        } catch (Exception e) {
            logger.warn("Could not json-deserialize AuthContextDeclRefs config entry: " + authnContextDeclRefs, e);
            return new LinkedList<String>();
        }
    }

    /** 登录完成后将 NameID、SessionIndex 与联邦令牌写入用户会话 note。 */
    @Override
    public void authenticationFinished(AuthenticationSessionModel authSession, BrokeredIdentityContext context)  {
        AssertionType assertion = (AssertionType)context.getContextData().get(SAMLEndpoint.SAML_ASSERTION);
        SubjectType subject = assertion.getSubject();
        SubjectType.STSubType subType = subject.getSubType();
        if (subType != null) {
            NameIDType subjectNameID = (NameIDType) subType.getBaseID();
            authSession.setUserSessionNote(SAMLEndpoint.SAML_FEDERATED_SUBJECT_NAMEID, subjectNameID.serializeAsString());
        }
        AuthnStatementType authn =  (AuthnStatementType)context.getContextData().get(SAMLEndpoint.SAML_AUTHN_STATEMENT);
        if (authn != null && authn.getSessionIndex() != null) {
            authSession.setUserSessionNote(SAMLEndpoint.SAML_FEDERATED_SESSION_INDEX, authn.getSessionIndex());

        }
        if (context.getContextData().containsKey(FEDERATED_ACCESS_TOKEN)) {
            authSession.setUserSessionNote(FEDERATED_ACCESS_TOKEN, (String) context.getContextData().get(FEDERATED_ACCESS_TOKEN));
        }
    }

    /** 返回存储的 SAML 断言令牌（纯文本）。 */
    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity) {
        return Response.ok(identity.getToken()).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /** 从会话或联邦身份记录获取 SAML 令牌并包装为 {@link AccessTokenResponse}。 */
    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity, UserSessionModel userSession, UserModel user) {
        String token = null;
        if (userSession != null && getConfig().isStoreTokenInSession()) {
            token = getFederatedAccessToken(userSession);
        }

        if (token == null && Booleans.isTrue(getConfig().isStoreToken())) {
            token = identity.getToken();
        }

        if (token == null) {
            return exchangeErrorResponse(session.getContext().getUri(), null, userSession, "token_expired", "No token stored.");
        }

        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setToken(Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes(GeneralConstants.SAML_CHARSET)));
        tokenResponse.setTokenType(TokenUtil.TOKEN_TYPE_BEARER);
        return Response.ok(tokenResponse).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /** 向后端 SLO URL 发送 LogoutRequest（HTTP POST）。 */
    @Override
    public void backchannelLogout(KeycloakSession session, UserSessionModel userSession, UriInfo uriInfo, RealmModel realm) {
        String singleLogoutServiceUrl = getConfig().getSingleLogoutServiceUrl();
        if (singleLogoutServiceUrl == null || singleLogoutServiceUrl.trim().equals("") || !getConfig().isBackchannelSupported()) return;
        JaxrsSAML2BindingBuilder binding = buildLogoutBinding(session, userSession, realm);
        try {
            LogoutRequestType logoutRequest = buildLogoutRequest(userSession, uriInfo, realm, singleLogoutServiceUrl);
            if (logoutRequest.getDestination() != null) {
                singleLogoutServiceUrl = logoutRequest.getDestination().toString();
            }
            int status = SimpleHttp.create(session).doPost(singleLogoutServiceUrl)
                    .param(GeneralConstants.SAML_REQUEST_KEY, binding.postBinding(SAML2Request.convert(logoutRequest)).encoded())
                    .param(GeneralConstants.RELAY_STATE, userSession.getId()).asStatus();
            boolean success = status >=200 && status < 400;
            if (!success) {
                logger.warn("Failed saml backchannel broker logout to: " + singleLogoutServiceUrl);
            }
        } catch (Exception e) {
            logger.warn("Failed saml backchannel broker logout to: " + singleLogoutServiceUrl, e);
        }

    }

    /** Keycloak 发起的浏览器 SLO：后端或前端绑定 LogoutRequest。 */
    @Override
    public Response keycloakInitiatedBrowserLogout(KeycloakSession session, UserSessionModel userSession, UriInfo uriInfo, RealmModel realm) {
        String singleLogoutServiceUrl = getConfig().getSingleLogoutServiceUrl();
        if (singleLogoutServiceUrl == null || singleLogoutServiceUrl.trim().equals("")) return null;

        if (getConfig().isBackchannelSupported()) {
            backchannelLogout(session, userSession, uriInfo, realm);
            return null;
       } else {
            try {
                LogoutRequestType logoutRequest = buildLogoutRequest(userSession, uriInfo, realm, singleLogoutServiceUrl);
                if (logoutRequest.getDestination() != null) {
                    singleLogoutServiceUrl = logoutRequest.getDestination().toString();
                }
                JaxrsSAML2BindingBuilder binding = buildLogoutBinding(session, userSession, realm);
                if (getConfig().isPostBindingLogout()) {
                    return binding.postBinding(SAML2Request.convert(logoutRequest)).request(singleLogoutServiceUrl);
                } else {
                    return binding.redirectBinding(SAML2Request.convert(logoutRequest)).request(singleLogoutServiceUrl);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** 构建带 SessionIndex/NameID 的 SAML LogoutRequest。 */
    protected LogoutRequestType buildLogoutRequest(UserSessionModel userSession, UriInfo uriInfo, RealmModel realm, String singleLogoutServiceUrl, NodeGenerator... extensions) throws ConfigurationException {
        SAML2LogoutRequestBuilder logoutBuilder = new SAML2LogoutRequestBuilder()
                .assertionExpiration(realm.getAccessCodeLifespan())
                .issuer(getEntityId(uriInfo, realm))
                .sessionIndex(userSession.getNote(SAMLEndpoint.SAML_FEDERATED_SESSION_INDEX))
                .nameId(NameIDType.deserializeFromString(userSession.getNote(SAMLEndpoint.SAML_FEDERATED_SUBJECT_NAMEID)))
                .destination(singleLogoutServiceUrl);
        LogoutRequestType logoutRequest = logoutBuilder.createLogoutRequest();
        for (NodeGenerator extension : extensions) {
            logoutBuilder.addExtension(extension);
        }
        for (Iterator<SamlAuthenticationPreprocessor> it = SamlSessionUtils.getSamlAuthenticationPreprocessorIterator(session); it.hasNext();) {
            logoutRequest = it.next().beforeSendingLogoutRequest(logoutRequest, userSession, null);
        }
        return logoutRequest;
    }

    private JaxrsSAML2BindingBuilder buildLogoutBinding(KeycloakSession session, UserSessionModel userSession, RealmModel realm) {
        JaxrsSAML2BindingBuilder binding = new JaxrsSAML2BindingBuilder(session)
                .relayState(userSession.getId());
        if (getConfig().isWantAuthnRequestsSigned()) {
            KeyManager.ActiveRsaKey keys = session.keys().getActiveRsaKey(realm);
            String keyName = getConfig().getXmlSigKeyInfoKeyNameTransformer().getKeyName(keys.getKid(), keys.getCertificate());
            binding.signWith(keyName, keys.getPrivateKey(), keys.getPublicKey(), keys.getCertificate())
                    .signatureAlgorithm(getSignatureAlgorithm())
                    .signDocument();
        }
        return binding;
    }

    /** 组装 SP EntityDescriptor，含 ACS/SLO 端点、签名/加密密钥与属性消费服务。 */
    protected EntityDescriptorType getEntityDescriptor(UriInfo uriInfo, RealmModel realm, String format) {
        URI endpoint = uriInfo.getBaseUriBuilder()
                .path("realms").path(realm.getName())
                .path("broker")
                .path(getConfig().getAlias())
                .path("endpoint")
                .build();

        List<EndpointType> assertionConsumerServices = getConfig().isPostBindingAuthnRequest()
                ? List.of(new EndpointType(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.getUri(), endpoint),
                        new EndpointType(JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.getUri(), endpoint),
                        new EndpointType(JBossSAMLURIConstants.SAML_HTTP_ARTIFACT_BINDING.getUri(), endpoint))
                : List.of(new EndpointType(JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.getUri(), endpoint),
                        new EndpointType(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.getUri(), endpoint),
                        new EndpointType(JBossSAMLURIConstants.SAML_HTTP_ARTIFACT_BINDING.getUri(), endpoint));

        List<EndpointType> singleLogoutServices = getConfig().isPostBindingLogout()
                ? List.of(new EndpointType(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.getUri(), endpoint),
                        new EndpointType(JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.getUri(), endpoint))
                : List.of(new EndpointType(JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.getUri(), endpoint),
                        new EndpointType(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.getUri(), endpoint));

        boolean wantAuthnRequestsSigned = getConfig().isWantAuthnRequestsSigned();
        boolean wantAssertionsSigned = getConfig().isWantAssertionsSigned();
        boolean wantAssertionsEncrypted = getConfig().isWantAssertionsEncrypted();
        String entityId = getEntityId(uriInfo, realm);
        String nameIDPolicyFormat = getConfig().getNameIDPolicyFormat();

        // 导出 RS256 全部 SIG 密钥（含 passive），便于 IdP 在密钥轮换后仍能验签
        //  if a key rotation happens in the meantime
        List<KeyDescriptorType> signingKeys = session.keys().getKeysStream(realm, KeyUse.SIG, Algorithm.RS256)
                .filter(key -> key.getCertificate() != null)
                .sorted(SamlService::compareKeys)
                .map(key -> {
                    try {
                        return SPMetadataDescriptor.buildKeyInfoElement(key.getKid(), PemUtils.encodeCertificate(key.getCertificate()));
                    } catch (ParserConfigurationException e) {
                        logger.warn("Failed to export SAML SP Metadata!", e);
                        throw new RuntimeException(e);
                    }
                })
                .map(key -> SPMetadataDescriptor.buildKeyDescriptorType(key, KeyTypes.SIGNING, null))
                .collect(Collectors.toList());

        // 仅导出 active ENC 密钥，促使 IdP 在轮换后尽快切换加密密钥
        String encAlg = getConfig().getEncryptionAlgorithm();
        List<KeyDescriptorType> encryptionKeys = session.keys().getKeysStream(realm)
                .filter(key -> key.getStatus().isActive() && KeyUse.ENC.equals(key.getUse())
                        && (encAlg == null || Objects.equals(encAlg, key.getAlgorithmOrDefault()))
                        && SAMLEncryptionAlgorithms.forKeycloakIdentifier(key.getAlgorithm()) != null
                        && key.getCertificate() != null)
                .sorted(SamlService::compareKeys)
                .map(key -> {
                    Element keyInfo;
                    try {
                        keyInfo = SPMetadataDescriptor.buildKeyInfoElement(key.getKid(), PemUtils.encodeCertificate(key.getCertificate()));
                    } catch (ParserConfigurationException e) {
                        logger.warn("Failed to export SAML SP Metadata!", e);
                        throw new RuntimeException(e);
                    }

                    return SPMetadataDescriptor.buildKeyDescriptorType(keyInfo, KeyTypes.ENCRYPTION, SAMLEncryptionAlgorithms.forKeycloakIdentifier(key.getAlgorithm()).getXmlEncIdentifiers());
                })
                .collect(Collectors.toList());

        EntityDescriptorType entityDescriptor = SPMetadataDescriptor.buildSPDescriptor(
            assertionConsumerServices, singleLogoutServices,
            wantAuthnRequestsSigned, wantAssertionsSigned, wantAssertionsEncrypted,
            entityId, nameIDPolicyFormat, signingKeys, encryptionKeys, getConfig().getDescriptorCacheSeconds());

        // 存在属性导入映射器时创建 AttributeConsumingService
        List<Entry<IdentityProviderMapperModel, SamlMetadataDescriptorUpdater>> metadataAttrProviders = new ArrayList<>();
        session.identityProviders().getMappersByAliasStream(getConfig().getAlias())
            .forEach(mapper -> {
                IdentityProviderMapper target = (IdentityProviderMapper) session.getKeycloakSessionFactory().getProviderFactory(IdentityProviderMapper.class, mapper.getIdentityProviderMapper());
                if (target instanceof SamlMetadataDescriptorUpdater samlMetadataDescriptorUpdater)
                    metadataAttrProviders.add(new java.util.AbstractMap.SimpleEntry<>(mapper, samlMetadataDescriptorUpdater));
            });

        if (!metadataAttrProviders.isEmpty()) {
            int attributeConsumingServiceIndex = getConfig().getAttributeConsumingServiceIndex() != null ? getConfig().getAttributeConsumingServiceIndex() : 1;
            String attributeConsumingServiceName = getConfig().getAttributeConsumingServiceName();
            // attributeConsumingServiceName 默认值
            if (attributeConsumingServiceName == null)
                attributeConsumingServiceName = realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName() ;
            AttributeConsumingServiceType attributeConsumingService = new AttributeConsumingServiceType(attributeConsumingServiceIndex);
            attributeConsumingService.setIsDefault(true);

            String currentLocale = realm.getDefaultLocale() == null ? "en" : realm.getDefaultLocale();
            LocalizedNameType attributeConsumingServiceNameElement = new LocalizedNameType(currentLocale);
            attributeConsumingServiceNameElement.setValue(attributeConsumingServiceName);
            attributeConsumingService.addServiceName(attributeConsumingServiceNameElement);

            // 向 SP 描述符添加 AttributeConsumingService
            for (EntityDescriptorType.EDTChoiceType choiceType : entityDescriptor.getChoiceType()) {
                List<EntityDescriptorType.EDTDescriptorChoiceType> descriptors = choiceType.getDescriptors();
                for (EntityDescriptorType.EDTDescriptorChoiceType descriptor : descriptors) {
                    descriptor.getSpDescriptor().addAttributeConsumerService(attributeConsumingService);
                }
            }

            // 由映射器填充请求属性
            metadataAttrProviders.forEach(mapper -> {
                SamlMetadataDescriptorUpdater metadataAttrProvider = mapper.getValue();
                metadataAttrProvider.updateMetadata(mapper.getKey(), entityDescriptor);
            });
        }

        return entityDescriptor;
    }

    /** 导出 SAML SP 元数据 XML，可选签名。 */
    @Override
    public Response export(UriInfo uriInfo, RealmModel realm, String format) {
        try
        {
            EntityDescriptorType entityDescriptor = getEntityDescriptor(uriInfo, realm, format);

            String descriptor;

            // 可选对 SP 元数据签名
            if (getConfig().isSignSpMetadata()) {
                KeyWrapper keyWrapper = session.keys().getActiveKey(realm, KeyUse.SIG, Algorithm.RS256);
                X509Certificate certificate = keyWrapper.getCertificate();
                String keyName = getConfig().getXmlSigKeyInfoKeyNameTransformer().getKeyName(keyWrapper.getKid(), certificate);
                KeyPair keyPair = new KeyPair(certificate.getPublicKey(), (PrivateKey) keyWrapper.getPrivateKey());;

                descriptor = SAMLMetadataUtil.signEntityDescriptorType(entityDescriptor, getSignatureAlgorithm(), keyName, certificate, keyPair);
            } else {
                descriptor = SAMLMetadataUtil.writeEntityDescriptorType(entityDescriptor);
            }

            return Response.ok(descriptor, MediaType.APPLICATION_XML_TYPE).build();
        } catch (Exception e) {
            logger.warn("Failed to export SAML SP Metadata!", e);
            throw new RuntimeException(e);
        }
    }

    /** 返回配置的 XML 签名算法，默认 RSA_SHA256。 */
    public SignatureAlgorithm getSignatureAlgorithm() {
        String alg = getConfig().getSignatureAlgorithm();
        if (alg != null) {
            SignatureAlgorithm algorithm = SignatureAlgorithm.valueOf(alg);
            if (algorithm != null) return algorithm;
        }
        return SignatureAlgorithm.RSA_SHA256;
    }

    /** @return {@link SAMLDataMarshaller} 用于联邦会话数据序列化 */
    @Override
    public IdentityProviderDataMarshaller getMarshaller() {
        return new SAMLDataMarshaller();
    }

    /** 启用元数据 URL 时从 IdP 元数据重新加载签名公钥。 */
    @Override
    public boolean reloadKeys() {
        if (getConfig().isEnabled() && getConfig().isUseMetadataDescriptorUrl()) {
            String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(session.getContext().getRealm().getId(), getConfig().getInternalId());
            PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
            return keyStorage.reloadKeys(modelKey, new SamlMetadataPublicKeyLoader(session, getConfig().getMetadataDescriptorUrl()));
        }
        return false;
    }

    /** SAML RelayState 规范限制约 80 字节，不支持长 state。 */
    @Override
    public boolean supportsLongStateParameter() {
        // SAML 规范限制 RelayState 约 80 字节
        return false;
    }

    /** 通过 SOAP ArtifactResolve 将 SAML Artifact 解析为 ArtifactResponse。 */
    public SAMLDocumentHolder resolveArtifact(KeycloakSession session, UriInfo uriInfo, RealmModel realm, String relayState, String samlArt) {
        // 读取 IdP 配置的 Artifact Resolution Service URL
        String artifactResolutionServiceUrl = getConfig().getArtifactResolutionServiceUrl();
        if (artifactResolutionServiceUrl == null || artifactResolutionServiceUrl.trim().isEmpty()) {
            throw new RuntimeException("Artifact Resolution Service URL is not configured for the Identity Provider.");
        }
        try {
            // 构建 ArtifactResolve 请求
            ArtifactResolveType artifactResolveRequest = buildArtifactResolveRequest(uriInfo, realm, artifactResolutionServiceUrl, samlArt);
            if (artifactResolveRequest.getDestination() != null) {
                artifactResolutionServiceUrl = artifactResolveRequest.getDestination().toString();
            }

            // convert the SAML Request object to a SAML Document (DOM)
            Document artifactResolveRequestAsDoc = SAML2Request.convert(artifactResolveRequest);

            // convert the SAML Document (DOM) to a SOAP Document (DOM)
            Document soapRequestAsDoc = buildArtifactResolveBinding(session, relayState, realm)
                    .soapBinding(artifactResolveRequestAsDoc).getDocument();

            // execute the SOAP request
            SOAPMessage soapResponse = Soap.createMessage()
                    .addMimeHeader("SOAPAction", "http://www.oasis-open.org/committees/security") // MAY in SOAP binding spec
                    .addToBody(soapRequestAsDoc)
                    .call(artifactResolutionServiceUrl, session);

            // extract the SAML Response (DOM) from the SOAP response
            Document artifactResolveResponseAsDoc = Soap.extractSoapMessage(soapResponse);

            // convert the SAML Response (DOM) to a SAML Response object and return it
            return SAML2Response.getSAML2ObjectFromDocument(artifactResolveResponseAsDoc);
        } catch (SOAPException | ConfigurationException | ProcessingException | ParsingException e) {
            logger.warn("Unable to resolve a SAML artifact to: " + artifactResolutionServiceUrl, e);
            throw new RuntimeException("Unable to resolve a SAML artifact to: " + artifactResolutionServiceUrl, e);
        }
    }

    /** 构建 ArtifactResolve 请求对象。 */
    protected ArtifactResolveType buildArtifactResolveRequest(UriInfo uriInfo, RealmModel realm, String artifactServiceUrl, String artifact, NodeGenerator... extensions) throws ConfigurationException {
        SAML2ArtifactResolveRequestBuilder artifactResolveRequestBuilder = new SAML2ArtifactResolveRequestBuilder()
                .issuer(getEntityId(uriInfo, realm))
                .destination(artifactServiceUrl)
                .artifact(artifact);
        ArtifactResolveType artifactResolveRequest = artifactResolveRequestBuilder.createArtifactResolveRequest();
        for (NodeGenerator extension : extensions) {
            artifactResolveRequestBuilder.addExtension(extension);
        }
        return artifactResolveRequest;
    }

    private JaxrsSAML2BindingBuilder buildArtifactResolveBinding(KeycloakSession session, String relayState, RealmModel realm) {
        JaxrsSAML2BindingBuilder binding = new JaxrsSAML2BindingBuilder(session).relayState(relayState);
        if (getConfig().isWantAuthnRequestsSigned()) {
            KeyManager.ActiveRsaKey keys = session.keys().getActiveRsaKey(realm);
            String keyName = getConfig().getXmlSigKeyInfoKeyNameTransformer().getKeyName(keys.getKid(), keys.getCertificate());
            binding.signWith(keyName, keys.getPrivateKey(), keys.getPublicKey(), keys.getCertificate())
                    .signatureAlgorithm(getSignatureAlgorithm())
                    .signDocument();
        }
        return binding;
    }
}
