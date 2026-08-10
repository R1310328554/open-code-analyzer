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

package org.keycloak.testsuite.rest.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.PemUtils;
import org.keycloak.constants.AdapterConstants;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.AsymmetricSignatureSignerContext;
import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.MacSignatureSignerContext;
import org.keycloak.crypto.ServerECDSASignatureSignerContext;
import org.keycloak.crypto.ServerEdDSASignatureSignerContext;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jwe.JWEConstants;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKBuilder;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.Constants;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.grants.ciba.CibaGrantType;
import org.keycloak.protocol.oidc.grants.ciba.channel.AuthenticationChannelRequest;
import org.keycloak.protocol.oidc.grants.ciba.channel.HttpAuthenticationChannelProvider;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.ClientNotificationEndpointRequest;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.clientpolicy.executor.IntentClientBindCheckExecutor;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.testsuite.rest.TestApplicationResourceProviderFactory;
import org.keycloak.testsuite.rest.representation.TestAuthenticationChannelRequest;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.resteasy.reactive.NoCache;

import static org.keycloak.common.crypto.CryptoConstants.EC_KEY_SECP256R1;
import static org.keycloak.common.crypto.CryptoConstants.EC_KEY_SECP384R1;
import static org.keycloak.common.crypto.CryptoConstants.EC_KEY_SECP521R1;

/**
 * OIDC 集成测试端点资源，提供密钥生成、请求对象签名、CIBA 与 Intent 绑定等测试 API。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TestingOIDCEndpointsApplicationResource {

    /** PEM 格式私钥的响应键名。 */
    public static final String PRIVATE_KEY = "privateKey";
    /** PEM 格式公钥的响应键名。 */
    public static final String PUBLIC_KEY = "publicKey";

    /** 共享 OIDC 客户端测试数据。 */
    private final TestApplicationResourceProviderFactory.OIDCClientData clientData;
    /** 认证通道请求映射，键为 binding message。 */
    private final ConcurrentMap<String, TestAuthenticationChannelRequest> authenticationChannelRequests;
    /** CIBA 客户端通知请求映射。 */
    private final ConcurrentMap<String, ClientNotificationEndpointRequest> cibaClientNotifications;
    /** Intent ID 到客户端 ID 的绑定映射。 */
    private final ConcurrentMap<String, String> intentClientBindings;

    /**
     * 构造 OIDC 测试端点资源。
     *
     * @param oidcClientData OIDC 客户端共享数据
     * @param authenticationChannelRequests 认证通道请求存储
     * @param cibaClientNotifications CIBA 通知存储
     * @param intentClientBindings Intent 绑定存储
     */
    public TestingOIDCEndpointsApplicationResource(TestApplicationResourceProviderFactory.OIDCClientData oidcClientData,
            ConcurrentMap<String, TestAuthenticationChannelRequest> authenticationChannelRequests, ConcurrentMap<String, ClientNotificationEndpointRequest> cibaClientNotifications,
            ConcurrentMap<String, String> intentClientBindings) {
        this.clientData = oidcClientData;
        this.authenticationChannelRequests = authenticationChannelRequests;
        this.cibaClientNotifications = cibaClientNotifications;
        this.intentClientBindings = intentClientBindings;
    }

    /** 按 JWA 算法生成签名或加密密钥对并加入客户端数据。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/generate-keys")
    @NoCache
    public Map<String, String> generateKeys(@QueryParam("jwaAlgorithm") String jwaAlgorithm,
                                            @QueryParam("crv") String curve,
                                            @QueryParam("advertiseJWKAlgorithm") Boolean advertiseJWKAlgorithm,
                                            @QueryParam("keepExistingKeys") Boolean keepExistingKeys,
                                            @QueryParam("kid") String kid) {
        try {
            KeyPair keyPair = null;
            KeyUse keyUse = KeyUse.SIG;
            if (jwaAlgorithm == null) jwaAlgorithm = Algorithm.RS256;
            String keyType = null;

            switch (jwaAlgorithm) {
                case Algorithm.RS256:
                case Algorithm.RS384:
                case Algorithm.RS512:
                case Algorithm.PS256:
                case Algorithm.PS384:
                case Algorithm.PS512:
                    keyType = KeyType.RSA;
                    keyPair = KeyUtils.generateRsaKeyPair(2048);
                    break;
                case Algorithm.ES256:
                    keyType = KeyType.EC;
                    keyPair = KeyUtils.generateEcKeyPair(EC_KEY_SECP256R1);
                    break;
                case Algorithm.ES384:
                    keyType = KeyType.EC;
                    keyPair = KeyUtils.generateEcKeyPair(EC_KEY_SECP384R1);
                    break;
                case Algorithm.ES512:
                    keyType = KeyType.EC;
                    keyPair = KeyUtils.generateEcKeyPair(EC_KEY_SECP521R1);
                    break;
                case Algorithm.EdDSA:
                    if (curve == null) {
                        curve = Algorithm.Ed25519;
                    }
                    keyType = KeyType.OKP;
                    keyPair = KeyUtils.generateEddsaKeyPair(curve);
                    break;
                case JWEConstants.RSA1_5:
                case JWEConstants.RSA_OAEP:
                case JWEConstants.RSA_OAEP_256:
                    // JWE KEK 密钥加密用途
                    keyType = KeyType.RSA;
                    keyUse = KeyUse.ENC;
                    keyPair = KeyUtils.generateRsaKeyPair(2048);
                    break;
                default :
                    throw new RuntimeException("Unsupported signature algorithm");
            }

            TestApplicationResourceProviderFactory.OIDCKeyData keyData = new TestApplicationResourceProviderFactory.OIDCKeyData();
            keyData.setKid(kid); // 可为 null，此时将自动生成
            keyData.setKeyPair(keyPair);
            keyData.setKeyType(keyType);
            keyData.setCurve(curve);
            if (advertiseJWKAlgorithm == null || Boolean.TRUE.equals(advertiseJWKAlgorithm)) {
                keyData.setKeyAlgorithm(jwaAlgorithm);
            } else {
                keyData.setKeyAlgorithm(null);
            }
            keyData.setKeyUse(keyUse);
            clientData.addKey(keyData, keepExistingKeys != null && keepExistingKeys);
        } catch (Exception e) {
            throw new BadRequestException("Error generating signing keypair", e);
        }
        return getKeysAsPem();
    }

    /** 以 PEM 格式返回当前第一组密钥对。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get-keys-as-pem")
    public Map<String, String> getKeysAsPem() {
        TestApplicationResourceProviderFactory.OIDCKeyData keyData = clientData.getFirstKey();
        String privateKeyPem = PemUtils.encodeKey(keyData.getSigningKeyPair().getPrivate());
        String publicKeyPem = PemUtils.encodeKey(keyData.getSigningKeyPair().getPublic());

        Map<String, String> res = new HashMap<>();
        res.put(PRIVATE_KEY, privateKeyPem);
        res.put(PUBLIC_KEY, publicKeyPem);
        return res;
    }

    /** 以 Base64 编码 DER 格式返回当前第一组密钥对。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get-keys-as-base64")
    public Map<String, String> getKeysAsBase64() {
        // PemUtils.decodePrivateKey/decodePublicKey 仅支持 RSA，EC 密钥需直接编码 DER
        TestApplicationResourceProviderFactory.OIDCKeyData keyData = clientData.getFirstKey();
        String privateKeyPem = Base64.getEncoder().encodeToString(keyData.getSigningKeyPair().getPrivate().getEncoded());
        String publicKeyPem = Base64.getEncoder().encodeToString(keyData.getSigningKeyPair().getPublic().getEncoded());

        Map<String, String> res = new HashMap<>();
        res.put(PRIVATE_KEY, privateKeyPem);
        res.put(PUBLIC_KEY, publicKeyPem);
        return res;
    }

    /** 返回当前客户端密钥对应的 JWKS。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get-jwks")
    @NoCache
    public JSONWebKeySet getJwks() {
        Stream<JWK> keysStream = clientData.getKeys().stream()
                .map(keyData -> {
                    KeyPair keyPair = keyData.getKeyPair();
                    String keyAlgorithm = keyData.getKeyAlgorithm();
                    String keyType = keyData.getKeyType();
                    KeyUse keyUse = keyData.getKeyUse();
                    String kid = keyData.getKid();

                    JWKBuilder builder = JWKBuilder.create().algorithm(keyAlgorithm).kid(kid);

                    if (KeyType.RSA.equals(keyType)) {
                        return builder.rsa(keyPair.getPublic(), keyUse);
                    } else if (KeyType.EC.equals(keyType)) {
                        return builder.ec(keyPair.getPublic());
                    } else if (KeyType.OKP.equals(keyType)) {
                        return builder.okp(keyPair.getPublic());
                    } else {
                        throw new IllegalArgumentException("Unknown keyType: " + keyType);
                    }
                });

        JSONWebKeySet keySet = new JSONWebKeySet();
        keySet.setKeys(keysStream.toArray(JWK[]::new));
        return keySet;
        
    }


    /** 根据查询参数构造并签名 OIDC 授权请求对象。 */
    @GET
    @Path("/set-oidc-request")
    @Produces(org.keycloak.utils.MediaType.APPLICATION_JWT)
    @NoCache
    public void setOIDCRequest(@QueryParam("realmName") String realmName, @QueryParam("clientId") String clientId,
                               @QueryParam("redirectUri") String redirectUri, @QueryParam("maxAge") String maxAge,
                                @QueryParam("state") String state,
                               @QueryParam("jwaAlgorithm") String jwaAlgorithm) {

        Map<String, Object> oidcRequest = new HashMap<>();
        oidcRequest.put(OIDCLoginProtocol.CLIENT_ID_PARAM, clientId);
        oidcRequest.put(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, OAuth2Constants.CODE);
        oidcRequest.put(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);

        if (state != null) {
            oidcRequest.put(OIDCLoginProtocol.STATE_PARAM, state);
        }

        if (maxAge != null) {
            oidcRequest.put(OIDCLoginProtocol.MAX_AGE_PARAM, Integer.parseInt(maxAge));
        }

        setOidcRequest(oidcRequest, jwaAlgorithm);
    }

    /** 注册 Base64 编码的请求对象并签名。 */
    @GET
    @Path("/register-oidc-request")
    @Produces(org.keycloak.utils.MediaType.APPLICATION_JWT)
    @NoCache
    public void registerOIDCRequest(@QueryParam("requestObject") String encodedRequestObject, @QueryParam("jwaAlgorithm") String jwaAlgorithm) {
        AuthorizationEndpointRequestObject oidcRequest = deserializeOidcRequest(encodedRequestObject);
        setOidcRequest(oidcRequest, jwaAlgorithm);
    }

    /** 使用对称密钥（客户端密钥）注册并签名请求对象。 */
    @GET
    @Path("/register-oidc-request-symmetric-sig")
    @Produces(org.keycloak.utils.MediaType.APPLICATION_JWT)
    @NoCache
    public void registerOIDCRequestSymmetricSig(@QueryParam("requestObject") String encodedRequestObject, @QueryParam("jwaAlgorithm") String jwaAlgorithm, @QueryParam("clientSecret") String clientSecret) {
        AuthorizationEndpointRequestObject oidcRequest = deserializeOidcRequest(encodedRequestObject);
        setOidcRequest(oidcRequest, jwaAlgorithm, clientSecret);
    }

    /** 反序列化 Base64Url 编码的授权端点请求对象。 */
    private AuthorizationEndpointRequestObject deserializeOidcRequest(String encodedRequestObject) {
        byte[] serializedRequestObject = Base64Url.decode(encodedRequestObject);
        AuthorizationEndpointRequestObject oidcRequest = null;
        try {
            oidcRequest = JsonSerialization.readValue(serializedRequestObject, AuthorizationEndpointRequestObject.class);
        } catch (IOException e) {
            throw new BadRequestException("deserialize request object failed : " + e.getMessage());
        }
        return oidcRequest;
    }

    /** 使用非对称密钥签名并保存 OIDC 请求对象。 */
    private void setOidcRequest(Object oidcRequest, String jwaAlgorithm) {
        if (!isSupportedAlgorithm(jwaAlgorithm)) throw new BadRequestException("Unknown argument: " + jwaAlgorithm);

        if ("none".equals(jwaAlgorithm)) {
            clientData.setOidcRequest(new JWSBuilder().jsonContent(oidcRequest).none());
        } else if (clientData.getFirstKey() == null) {
            throw new BadRequestException("signing key not set");
        } else {
            TestApplicationResourceProviderFactory.OIDCKeyData keyData = clientData.getFirstKey();
            PrivateKey privateKey = keyData.getSigningKeyPair().getPrivate();
            String kid = keyData.getKid() != null ? keyData.getKid() : KeyUtils.createKeyId(keyData.getSigningKeyPair().getPublic());
            KeyWrapper keyWrapper = new KeyWrapper();
            keyWrapper.setAlgorithm(keyData.getSigningKeyAlgorithm());
            keyWrapper.setKid(kid);
            keyWrapper.setPrivateKey(privateKey);
            SignatureSignerContext signer;
            switch (keyData.getSigningKeyAlgorithm()) {
                case Algorithm.ES256:
                case Algorithm.ES384:
                case Algorithm.ES512:
                    signer = new ServerECDSASignatureSignerContext(keyWrapper);
                    break;
                case Algorithm.EdDSA:
                    keyWrapper.setCurve(keyData.getCurve());
                    signer = new ServerEdDSASignatureSignerContext(keyWrapper);
                    break;
                default:
                    signer = new AsymmetricSignatureSignerContext(keyWrapper);
            }
            clientData.setOidcRequest(new JWSBuilder().kid(kid).jsonContent(oidcRequest).sign(signer));
        }
    }

    /** 使用 HMAC 对称密钥签名并保存 OIDC 请求对象。 */
    private void setOidcRequest(Object oidcRequest, String jwaAlgorithm, String clientSecret) {
        if (!isSupportedAlgorithm(jwaAlgorithm)) throw new BadRequestException("Unknown argument: " + jwaAlgorithm);
        if ("none".equals(jwaAlgorithm)) {
            clientData.setOidcRequest(new JWSBuilder().jsonContent(oidcRequest).none());
        } else {
            SignatureSignerContext signer;
            switch (jwaAlgorithm) {
                case Algorithm.HS256:
                case Algorithm.HS384:
                case Algorithm.HS512:
                    KeyWrapper keyWrapper = new KeyWrapper();
                    SecretKey secretKey = new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), JavaAlgorithm.getJavaAlgorithm(jwaAlgorithm));
                    keyWrapper.setSecretKey(secretKey);
                    String kid = KeyUtils.createKeyId(secretKey);
                    keyWrapper.setKid(kid);
                    keyWrapper.setAlgorithm(jwaAlgorithm);
                    keyWrapper.setUse(KeyUse.SIG);
                    keyWrapper.setType(KeyType.OCT);
                    signer = new MacSignatureSignerContext(keyWrapper);
                    clientData.setOidcRequest(new JWSBuilder().kid(kid).jsonContent(oidcRequest).sign(signer));
                    break;
                default:
                    throw new BadRequestException("Unknown jwaAlgorithm: " + jwaAlgorithm);
            }
        }
    }

    /** 判断签名算法是否在测试端点支持范围内。 */
    private boolean isSupportedAlgorithm(String signingAlgorithm) {
        if (signingAlgorithm == null) return false;
        boolean ret = false;
        switch (signingAlgorithm) {
            case "none":
            case Algorithm.RS256:
            case Algorithm.RS384:
            case Algorithm.RS512:
            case Algorithm.PS256:
            case Algorithm.PS384:
            case Algorithm.PS512:
            case Algorithm.ES256:
            case Algorithm.ES384:
            case Algorithm.ES512:
            case Algorithm.EdDSA:
            case Algorithm.HS256:
            case Algorithm.HS384:
            case Algorithm.HS512:
            case JWEConstants.RSA1_5:
            case JWEConstants.RSA_OAEP:
            case JWEConstants.RSA_OAEP_256:
                ret = true;
        }
        return ret;
    }

    /** 返回当前已保存的 OIDC 请求 JWT 字符串。 */
    @GET
    @Path("/get-oidc-request")
    @Produces(org.keycloak.utils.MediaType.APPLICATION_JWT)
    @NoCache
    public String getOIDCRequest() {
        return clientData.getOidcRequest();
    }

    /** 设置 sector identifier 关联的重定向 URI 列表。 */
    @GET
    @Path("/set-sector-identifier-redirect-uris")
    @Produces(MediaType.APPLICATION_JSON)
    public void setSectorIdentifierRedirectUris(@QueryParam("redirectUris") List<String> redirectUris) {
        clientData.setSectorIdentifierRedirectUris(new ArrayList<>());
        clientData.getSectorIdentifierRedirectUris().addAll(redirectUris);
    }

    /** 返回 sector identifier 关联的重定向 URI 列表。 */
    @GET
    @Path("/get-sector-identifier-redirect-uris")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getSectorIdentifierRedirectUris() {
        return clientData.getSectorIdentifierRedirectUris();
    }

    /** 授权端点请求对象 POJO，映射 OIDC/CIBA 授权参数。 */
    public static class AuthorizationEndpointRequestObject extends JsonWebToken {

        /** 客户端 ID。 */
        @JsonProperty(OIDCLoginProtocol.CLIENT_ID_PARAM)
        String clientId;

        /** 响应类型。 */
        @JsonProperty(OIDCLoginProtocol.RESPONSE_TYPE_PARAM)
        String responseType;

        /** 响应模式。 */
        @JsonProperty(OIDCLoginProtocol.RESPONSE_MODE_PARAM)
        String responseMode;

        /** 重定向 URI。 */
        @JsonProperty(OIDCLoginProtocol.REDIRECT_URI_PARAM)
        String redirectUriParam;

        /** OAuth state 参数。 */
        @JsonProperty(OIDCLoginProtocol.STATE_PARAM)
        String state;

        /** 请求的 scope。 */
        @JsonProperty(OIDCLoginProtocol.SCOPE_PARAM)
        String scope;

        /** 登录提示。 */
        @JsonProperty(OIDCLoginProtocol.LOGIN_HINT_PARAM)
        String loginHint;

        /** prompt 参数。 */
        @JsonProperty(OIDCLoginProtocol.PROMPT_PARAM)
        String prompt;

        /** nonce 参数。 */
        @JsonProperty(OIDCLoginProtocol.NONCE_PARAM)
        String nonce;

        /** max_age 参数。 */
        Integer max_age;

        /** UI 语言偏好。 */
        @JsonProperty(OIDCLoginProtocol.UI_LOCALES_PARAM)
        String uiLocales;

        /** 认证上下文类参考（ACR）。 */
        @JsonProperty(OIDCLoginProtocol.ACR_PARAM)
        String acr;

        /** OAuth display 参数。 */
        @JsonProperty(OAuth2Constants.DISPLAY)
        String display;

        /** PKCE code challenge。 */
        @JsonProperty(OIDCLoginProtocol.CODE_CHALLENGE_PARAM)
        String codeChallenge;

        /** PKCE code challenge 方法。 */
        @JsonProperty(OIDCLoginProtocol.CODE_CHALLENGE_METHOD_PARAM)
        String codeChallengeMethod;

        /** DPoP 公钥 JKT 指纹。 */
        @JsonProperty(OIDCLoginProtocol.DPOP_JKT)
        String dpopJkt;

        /** 身份提供者提示。 */
        @JsonProperty(AdapterConstants.KC_IDP_HINT)
        String idpHint;

        /** Keycloak 用户操作提示。 */
        @JsonProperty(Constants.KC_ACTION)
        String action;

        // CIBA 相关参数

        /** CIBA 客户端通知令牌。 */
        @JsonProperty(CibaGrantType.CLIENT_NOTIFICATION_TOKEN)
        String clientNotificationToken;

        /** CIBA login hint 令牌。 */
        @JsonProperty(CibaGrantType.LOGIN_HINT_TOKEN)
        String loginHintToken;

        /** ID Token hint。 */
        @JsonProperty(OIDCLoginProtocol.ID_TOKEN_HINT)
        String idTokenHint;

        /** CIBA 用户码。 */
        @JsonProperty(CibaGrantType.USER_CODE)
        String userCode;

        /** CIBA 绑定消息。 */
        @JsonProperty(CibaGrantType.BINDING_MESSAGE)
        String bindingMessage;

        /** CIBA 请求的令牌有效期。 */
        Integer requested_expiry;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId =  clientId;
        }

        public String getResponseType() {
            return responseType;
        }

        public void setResponseType(String responseType) {
            this.responseType = responseType;
        }

        public String getResponseMode() {
            return responseMode;
        }

        public void setResponseMode(String responseMode) {
            this.responseMode = responseMode;
        }

        public String getRedirectUriParam() {
            return redirectUriParam;
        }

        public void setRedirectUriParam(String redirectUriParam) {
            this.redirectUriParam = redirectUriParam;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getLoginHint() {
            return loginHint;
        }

        public void setLoginHint(String loginHint) {
            this.loginHint = loginHint;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public Integer getMax_age() {
            return max_age;
        }

        public void setMax_age(Integer max_age) {
            this.max_age = max_age;
        }

        public String getUiLocales() {
            return uiLocales;
        }

        public void setUiLocales(String uiLocales) {
            this.uiLocales = uiLocales;
        }

        public String getAcr() {
            return acr;
        }

        public void setAcr(String acr) {
            this.acr = acr;
        }

        public String getCodeChallenge() {
            return codeChallenge;
        }

        public void setCodeChallenge(String codeChallenge) {
            this.codeChallenge = codeChallenge;
        }

        public String getCodeChallengeMethod() {
            return codeChallengeMethod;
        }

        public void setCodeChallengeMethod(String codeChallengeMethod) {
            this.codeChallengeMethod = codeChallengeMethod;
        }

        public String getDpopJkt() {
            return dpopJkt;
        }

        public void setDpopJkt(String dpopJkt) {
            this.dpopJkt = dpopJkt;
        }

        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }

        public String getIdpHint() {
            return idpHint;
        }

        public void setIdpHint(String idpHint) {
            this.idpHint = idpHint;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getClientNotificationToken() {
            return clientNotificationToken;
        }

        public void setClientNotificationToken(String clientNotificationToken) {
            this.clientNotificationToken = clientNotificationToken;
        }

        public String getLoginHintToken() {
            return loginHintToken;
        }

        public void setLoginHintToken(String loginHintToken) {
            this.loginHintToken = loginHintToken;
        }

        public String getIdTokenHint() {
            return idTokenHint;
        }

        public void setIdTokenHint(String idTokenHint) {
            this.idTokenHint = idTokenHint;
        }

        public String getBindingMessage() {
            return bindingMessage;
        }

        public void setBindingMessage(String bindingMessage) {
            this.bindingMessage = bindingMessage;
        }

        public String getUserCode() {
            return userCode;
        }

        public void setUserCode(String userCode) {
            this.userCode = userCode;
        }

        public Integer getRequested_expiry() {
            return requested_expiry;
        }

        public void setRequested_expiry(Integer requested_expiry) {
            this.requested_expiry = requested_expiry;
        }

    }

    /** 接收 CIBA 认证通道请求并存储供测试检索。 */
    @POST
    @Path("/request-authentication-channel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response requestAuthenticationChannel(@Context HttpHeaders headers, AuthenticationChannelRequest request) {
        String rawBearerToken = AppAuthManager.extractAuthorizationHeaderToken(headers);
        AccessToken bearerToken;

        try {
            bearerToken = new JWSInput(rawBearerToken).readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new RuntimeException("Failed to parse bearer token", e);
        }

        // 必填参数校验
        String authenticationChannelId = bearerToken.getId();
        if (authenticationChannelId == null) throw new BadRequestException("missing parameter : " + HttpAuthenticationChannelProvider.AUTHENTICATION_CHANNEL_ID);

        String loginHint = request.getLoginHint();
        if (loginHint == null) throw new BadRequestException("missing parameter : " + CibaGrantType.LOGIN_HINT);

        if (request.getConsentRequired() == null)
            throw new BadRequestException("missing parameter : " + CibaGrantType.IS_CONSENT_REQUIRED);

        String scope = request.getScope();
        if (scope == null) throw new BadRequestException("missing parameter : " + OAuth2Constants.SCOPE);

        // 可选参数；用于测试场景
        String bindingMessage = request.getBindingMessage();
        if (bindingMessage != null && bindingMessage.equals("GODOWN")) throw new BadRequestException("intentional error : GODOWN");

        // binding_message 可为 null；无 binding_message 的 CIBA 流程在测试机制中每个测试方法仅接受一次
        if (bindingMessage == null) bindingMessage = ChannelRequestDummyKey;
        authenticationChannelRequests.put(bindingMessage, new TestAuthenticationChannelRequest(request, rawBearerToken));

        return Response.status(Status.CREATED).build();
    }

    /** 按 binding message 检索已存储的认证通道请求。 */
    @GET
    @Path("/get-authentication-channel")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public TestAuthenticationChannelRequest getAuthenticationChannel(@QueryParam("bindingMessage") String bindingMessage) {
        if (bindingMessage == null) bindingMessage = ChannelRequestDummyKey;
        return authenticationChannelRequests.get(bindingMessage);
    }

    /** 无 binding message 时使用的占位键。 */
    private static final String ChannelRequestDummyKey = "channel_request_dummy_key";


    /** 接收 CIBA 客户端通知并存储。 */
    @POST
    @Path("/push-ciba-client-notification")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response cibaClientNotificationEndpoint(@Context HttpHeaders headers, ClientNotificationEndpointRequest request) {
        String clientNotificationToken = AppAuthManager.extractAuthorizationHeaderToken(headers);
        ClientNotificationEndpointRequest existing = cibaClientNotifications.putIfAbsent(clientNotificationToken, request);
        if (existing != null) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "There is already entry for clientNotification " + clientNotificationToken + ". Make sure to cleanup after previous tests.",
                    Response.Status.BAD_REQUEST);
        }
        return Response.noContent().build();
    }


    /** 取出并移除指定通知令牌的 CIBA 客户端通知。 */
    @GET
    @Path("/get-pushed-ciba-client-notification")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public ClientNotificationEndpointRequest getPushedCibaClientNotification(@QueryParam("clientNotificationToken") String clientNotificationToken) {
        ClientNotificationEndpointRequest request = cibaClientNotifications.remove(clientNotificationToken);
        if (request == null) {
            request = new ClientNotificationEndpointRequest();
        }
        return request;
    }

    /** 将 Intent ID 绑定到指定客户端 ID。 */
    @GET
    @Path("/bind-intent-with-client")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response bindIntentWithClient(@QueryParam("intentId") String intentId, @QueryParam("clientId") String clientId) {
        intentClientBindings.put(intentId, clientId);
        return Response.noContent().build();
    }

    /** 检查 Intent 是否已绑定到指定客户端。 */
    @POST
    @Path("/check-intent-client-bound")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public IntentClientBindCheckExecutor.IntentBindCheckResponse checkIntentClientBound(IntentClientBindCheckExecutor.IntentBindCheckRequest request) {
        IntentClientBindCheckExecutor.IntentBindCheckResponse response = new IntentClientBindCheckExecutor.IntentBindCheckResponse();
        response.setIsBound(Boolean.FALSE);
        if (intentClientBindings.containsKey(request.getIntentId()) && intentClientBindings.get(request.getIntentId()).equals(request.getClientId())) {
            response.setIsBound(Boolean.TRUE);
        }
        return response;
    }
}
