/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oidc.grants.ciba.endpoints;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.CibaConfig;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OAuth2DeviceCodeModel;
import org.keycloak.models.OAuth2DeviceUserCodeModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.grants.ciba.CibaGrantType;
import org.keycloak.protocol.oidc.grants.ciba.channel.AuthenticationChannelProvider;
import org.keycloak.protocol.oidc.grants.ciba.channel.CIBAAuthenticationRequest;
import org.keycloak.protocol.oidc.grants.ciba.clientpolicy.context.BackchannelAuthenticationRequestContext;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.request.BackchannelAuthenticationEndpointRequest;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.request.BackchannelAuthenticationEndpointRequestParserProcessor;
import org.keycloak.protocol.oidc.grants.ciba.resolvers.CIBALoginUserResolver;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.managers.BruteForceProtector;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

import static org.keycloak.protocol.oidc.OIDCLoginProtocol.ID_TOKEN_HINT;
import static org.keycloak.protocol.oidc.OIDCLoginProtocol.LOGIN_HINT_PARAM;

/**
 * CIBA 后台认证端点（Backchannel Authentication Endpoint）。
 * <p>消费设备通过此端点发起后台认证请求，获取 {@code auth_req_id} 供后续轮询或 ping 模式使用。</p>
 */
public class BackchannelAuthenticationEndpoint extends AbstractCibaEndpoint {

    private static final Logger log = Logger.getLogger(BackchannelAuthenticationEndpoint.class);

    /** 当前领域模型 */
    private final RealmModel realm;

    /** binding_message 参数格式校验正则（最多 50 字符，不含空格） */
    private static final Pattern BINDING_MESSAGE_VALIDATION = Pattern.compile("^[a-zA-Z0-9-._+/!?#]{1,50}$");

    /**
     * @param session Keycloak 会话
     * @param event 事件构建器
     */
    public BackchannelAuthenticationEndpoint(KeycloakSession session, EventBuilder event) {
        super(session, event);
        this.realm = session.getContext().getRealm();
        event.event(EventType.LOGIN);
    }

    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 处理 CIBA 后台认证请求：解析参数、解析用户、经认证通道发起认证并返回 auth_req_id。
     * @return 含 auth_req_id、expires_in 及可选 interval 的 JSON 响应
     */
    public Response processGrantRequest() {
        HttpRequest httpRequest = session.getContext().getHttpRequest();
        CIBAAuthenticationRequest request = authorizeClient(httpRequest.getDecodedFormParameters());

        try {
            String authReqId = request.serialize(session);
            AuthenticationChannelProvider provider = session.getProvider(AuthenticationChannelProvider.class);

            if (provider == null) {
                throw new RuntimeException("Authentication Channel Provider not found.");
            }

            CIBALoginUserResolver resolver = session.getProvider(CIBALoginUserResolver.class);

            if (resolver == null) {
                throw new RuntimeException("CIBA Login User Resolver not setup properly.");
            }

            UserModel user = request.getUser();

            String infoUsedByAuthentication = resolver.getInfoUsedByAuthentication(user);

            if (provider.requestAuthentication(request, infoUsedByAuthentication)) {
                CibaConfig cibaPolicy = realm.getCibaPolicy();
                int poolingInterval = cibaPolicy.getPoolingInterval();

                storeAuthenticationRequest(request, cibaPolicy, authReqId);

                ObjectNode response = JsonSerialization.createObjectNode();

                response.put(CibaGrantType.AUTH_REQ_ID, authReqId)
                        .put(OAuth2Constants.EXPIRES_IN, cibaPolicy.getExpiresIn());

                if (poolingInterval > 0) {
                    response.put(OAuth2Constants.INTERVAL, poolingInterval);
                }

                return Response.ok(JsonSerialization.writeValueAsBytes(response))
                        .build();
            }
        } catch (Exception e) {
            log.warn("Unexpected failure when processing CIBA authentication request", e);
            throw new ErrorResponseException(OAuthErrorException.SERVER_ERROR, "Failed to send authentication request", Response.Status.SERVICE_UNAVAILABLE);
        }

        throw new ErrorResponseException(OAuthErrorException.SERVER_ERROR, "Unexpected response from authentication device", Response.Status.SERVICE_UNAVAILABLE);
    }

    /**
     * 将认证请求持久化到单次使用对象存储（复用设备码存储机制）。
     * <p>TODO: 评估是否需要专用存储，或继续复用 {@link SingleUseObjectProvider}。</p>
     * @param request CIBA 认证请求
     * @param cibaConfig CIBA 策略配置
     * @param authReqId 认证请求标识（ping 模式使用）
     */
    private void storeAuthenticationRequest(CIBAAuthenticationRequest request, CibaConfig cibaConfig, String authReqId) {
        ClientModel client = request.getClient();
        int expiresIn = cibaConfig.getExpiresIn();
        int poolingInterval = cibaConfig.getPoolingInterval();
        String cibaMode = cibaConfig.getBackchannelTokenDeliveryMode(client);

        // 仅在 ping 模式设置 authReqId；poll 模式无需在缓存中存储较大标识
        if (!CibaConfig.CIBA_PING_MODE.equals(cibaMode)) {
            authReqId = null;
        }

        OAuth2DeviceCodeModel deviceCode = OAuth2DeviceCodeModel.create(realm, client,
                request.getId(), request.getScope(), null, expiresIn, poolingInterval, request.getClientNotificationToken(), authReqId,
                Collections.emptyMap(), null, null);
        String authResultId = request.getAuthResultId();
        OAuth2DeviceUserCodeModel userCode = new OAuth2DeviceUserCodeModel(realm, deviceCode.getDeviceCode(),
                authResultId);

        // 缓存寿命略长于设备码过期时间，以便客户端能收到 expired_token 错误
        int lifespanSeconds = expiresIn + poolingInterval + 10;

        SingleUseObjectProvider singleUseStore = session.singleUseObjects();

        singleUseStore.put(deviceCode.serializeKey(), lifespanSeconds, deviceCode.toMap());
        singleUseStore.put(userCode.serializeKey(), lifespanSeconds, userCode.serializeValue());
    }

    /**
     * 认证客户端、解析请求参数、解析目标用户并组装 {@link CIBAAuthenticationRequest}。
     * @param params 表单参数
     * @return 已组装的 CIBA 认证请求
     */
    private CIBAAuthenticationRequest authorizeClient(MultivaluedMap<String, String> params) {
        ClientModel client = null;
        try {
            client = authenticateClient();
        } catch (WebApplicationException wae) {
            OAuth2ErrorRepresentation errorRep = (OAuth2ErrorRepresentation)wae.getResponse().getEntity();
            throw new ErrorResponseException(errorRep.getError(), errorRep.getErrorDescription(), Response.Status.UNAUTHORIZED);
        }
        BackchannelAuthenticationEndpointRequest endpointRequest = BackchannelAuthenticationEndpointRequestParserProcessor.parseRequest(event, session, client, params, realm.getCibaPolicy());
        UserModel user = resolveUser(endpointRequest, realm.getCibaPolicy().getAuthRequestedUserHint());

        CIBAAuthenticationRequest request = new CIBAAuthenticationRequest(session, user, client);

        request.setClient(client);

        String scope = endpointRequest.getScope();
        if (scope == null) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "missing parameter : scope",
                    Response.Status.BAD_REQUEST);
        }
        if (!TokenManager.isValidScope(session, scope, client, user)) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_SCOPE, "Invalid scopes: " + scope,
                    Response.Status.BAD_REQUEST);
        }
        request.setScope(scope);

        // 可选参数
        if (endpointRequest.getBindingMessage() != null) {
            validateBindingMessage(endpointRequest.getBindingMessage());
            request.setBindingMessage(endpointRequest.getBindingMessage());
        }
        if (endpointRequest.getAcr() != null) request.setAcrValues(endpointRequest.getAcr());

        CibaConfig policy = realm.getCibaPolicy();

        // 根据 Auth Req ID 创建 JWE 编码的 auth_req_id
        Integer expiresIn = Optional.ofNullable(endpointRequest.getRequestedExpiry()).orElse(policy.getExpiresIn());

        request.exp(request.getIat() + expiresIn.longValue());

        StringBuilder scopes = new StringBuilder(Optional.ofNullable(request.getScope()).orElse(""));
        client.getClientScopes(true)
                .forEach((key, value) -> {
                    if (value.isDisplayOnConsentScreen())
                        scopes.append(" ").append(value.getName());
                });
        request.setScope(scopes.toString());

        if (endpointRequest.getClientNotificationToken() != null) {
            if (!policy.getBackchannelTokenDeliveryMode(client).equals(CibaConfig.CIBA_PING_MODE)) {
                throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST,
                        "Client Notification token supported only for the ping mode", Response.Status.BAD_REQUEST);
            }
            if (endpointRequest.getClientNotificationToken().length() > 1024) {
                throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST,
                        "Client Notification token length is limited to 1024 characters", Response.Status.BAD_REQUEST);
            }
            request.setClientNotificationToken(endpointRequest.getClientNotificationToken());
        }
        if (endpointRequest.getClientNotificationToken() == null && policy.getBackchannelTokenDeliveryMode(client).equals(CibaConfig.CIBA_PING_MODE)) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST,
                    "Client Notification token needs to be provided with the ping mode", Response.Status.BAD_REQUEST);
        }

        if (endpointRequest.getUserCode() != null) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "User code not supported",
                    Response.Status.BAD_REQUEST);
        }

        extractAdditionalParams(endpointRequest, request);

        try {
            session.clientPolicy().triggerOnEvent(new BackchannelAuthenticationRequestContext(endpointRequest, request, params));
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw new ErrorResponseException(cpe.getError(), cpe.getErrorDetail(), Response.Status.BAD_REQUEST);
        }

        return request;
    }

    /**
     * 将端点请求中的附加参数复制到 CIBA 认证请求的 other claims。
     * @param endpointRequest 后台认证端点请求
     * @param request CIBA 认证请求
     */
    protected void extractAdditionalParams(BackchannelAuthenticationEndpointRequest endpointRequest, CIBAAuthenticationRequest request) {
        for (String paramName : endpointRequest.getAdditionalReqParams().keySet()) {
            request.setOtherClaims(paramName, endpointRequest.getAdditionalReqParams().get(paramName));
        }
    }

    /**
     * 校验 binding_message 格式（长度与字符集）。
     * @param bindingMessage 绑定消息字符串
     */
    protected void validateBindingMessage(String bindingMessage) {
        if (!BINDING_MESSAGE_VALIDATION.matcher(bindingMessage).matches()) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_BINDING_MESSAGE, "the binding_message value has to be max 50 characters in length and must contain only basic plain-text characters without spaces",
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 根据领域配置的用户提示类型（login_hint / id_token_hint / login_hint_token）解析目标用户。
     * @param endpointRequest 后台认证端点请求
     * @param authRequestedUserHint 领域配置的用户提示参数名
     * @return 已解析且启用的用户模型
     */
    private UserModel resolveUser(BackchannelAuthenticationEndpointRequest endpointRequest, String authRequestedUserHint) {
        CIBALoginUserResolver resolver = session.getProvider(CIBALoginUserResolver.class);

        if (resolver == null) {
            throw new RuntimeException("CIBA Login User Resolver not setup properly.");
        }

        String userHint;
        UserModel user;

        if (authRequestedUserHint.equals(LOGIN_HINT_PARAM)) {
            userHint = endpointRequest.getLoginHint();
            if (userHint == null)
                throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "missing parameter : login_hint",
                        Response.Status.BAD_REQUEST);
            user = resolver.getUserFromLoginHint(userHint);
        } else if (authRequestedUserHint.equals(ID_TOKEN_HINT)) {
            userHint = endpointRequest.getIdTokenHint();
            if (userHint == null)
                throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "missing parameter : id_token_hint",
                        Response.Status.BAD_REQUEST);
            user = resolver.getUserFromIdTokenHint(userHint);
        } else if (authRequestedUserHint.equals(CibaGrantType.LOGIN_HINT_TOKEN)) {
            userHint = endpointRequest.getLoginHintToken();
            if (userHint == null)
                throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "missing parameter : login_hint_token",
                        Response.Status.BAD_REQUEST);
            user = resolver.getUserFromLoginHintToken(userHint);
        } else {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST,
                    "invalid user hint", Response.Status.BAD_REQUEST);
        }


        BruteForceProtector protector = session.getProvider(BruteForceProtector.class);
        boolean isInvalidUser = (user == null || !user.isEnabled());
        if (!isInvalidUser && AuthenticatorUtils.getDisabledByBruteForceEventError(protector, session, realm, user) != null) {
            isInvalidUser = true;
        }

        if (isInvalidUser) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "invalid_user", Response.Status.BAD_REQUEST);
        }

        return user;
    }
}
