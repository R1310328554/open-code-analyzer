/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.par.endpoints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.OAuthErrorException;
import org.keycloak.common.Profile;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.headers.SecurityHeadersProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.OIDCLoginProtocolService;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpointChecker;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.protocol.oidc.par.ParResponse;
import org.keycloak.protocol.oidc.par.clientpolicy.context.PushedAuthorizationRequestContext;
import org.keycloak.protocol.oidc.par.endpoints.request.ParEndpointRequestParserProcessor;
import org.keycloak.representations.dpop.DPoP;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.util.DPoPUtil;
import org.keycloak.utils.ProfileHelper;

import static org.keycloak.protocol.oidc.OIDCLoginProtocol.REQUEST_URI_PARAM;

/**
 * 推送授权请求（PAR）端点。
 * <p>接收并校验授权请求参数，存储为一次性 {@code request_uri}，返回 PAR 响应（RFC 9126）。</p>
 */
public class ParEndpoint extends AbstractParEndpoint {

    /** 存储键：PAR 创建时间戳（毫秒） */
    public static final String PAR_CREATED_TIME = "par.created.time";
    /** 存储键：PAR 请求关联的 DPoP 公钥指纹（JKT） */
    public static final String PAR_DPOP_PROOF_JKT = "par.dpop.proof.jkt";
    /** request_uri URN 前缀 */
    public static final String REQUEST_URI_PREFIX = "urn:ietf:params:oauth:request_uri:";
    /** request_uri URN 前缀长度，用于提取存储键 */
    public static final int REQUEST_URI_PREFIX_LENGTH = REQUEST_URI_PREFIX.length();
    /** 单次使用对象存储键前缀 */
    public static final String CACHE_KEY_PREFIX = "par:";

    /** 当前 HTTP 请求 */
    private final HttpRequest httpRequest;

    /** 已解析的授权请求 */
    private AuthorizationEndpointRequest authorizationRequest;

    /** 构建 PAR 端点 URL @param baseUriBuilder 基础 URI 构建器 @return PAR 请求路径构建器 */
    public static UriBuilder parUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = OIDCLoginProtocolService.tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(OIDCLoginProtocolService.class, "resolveExtension").resolveTemplate("extension", ParRootEndpoint.PROVIDER_ID, false).path(ParRootEndpoint.class, "request");
    }

    /** @param session Keycloak 会话 @param event 事件构建器 */
    public ParEndpoint(KeycloakSession session, EventBuilder event) {
        super(session, event);
    this.httpRequest = session.getContext().getHttpRequest();
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    /** 处理 PAR POST 请求：校验、存储参数并返回 request_uri @return 201 Created 与 {@link ParResponse} */
    public Response request() {

        ProfileHelper.requireFeature(Profile.Feature.PAR);

        cors = Cors.builder().auth().allowedMethods("POST").auth().exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS);

        event.event(EventType.PUSHED_AUTHORIZATION_REQUEST);

        checkSsl();
        checkRealm();
        authorizeClient();

        MultivaluedMap<String, String> decodedFormParameters = httpRequest.getDecodedFormParameters();

        if (decodedFormParameters.containsKey(REQUEST_URI_PARAM)) {
            throw errorResponseException(OAuthErrorException.INVALID_REQUEST, "It is not allowed to include request_uri to PAR.", Response.Status.BAD_REQUEST);
        }

        // RFC 9449 §10.1：处理 DPoP 请求头
        DPoPUtil.handleDPoPHeader(session, event, cors, null);

        try {
            authorizationRequest = ParEndpointRequestParserProcessor.parseRequest(event, session, client, decodedFormParameters);
        } catch (Exception e) {
            if (!decodedFormParameters.containsKey(OIDCLoginProtocol.REQUEST_PARAM)) {
                throw errorResponseException(OAuthErrorException.INVALID_REQUEST, e.getMessage(), Response.Status.BAD_REQUEST);
            }
            throw errorResponseException(OAuthErrorException.INVALID_REQUEST_OBJECT, e.getMessage(), Response.Status.BAD_REQUEST);
        }

        try {
            session.clientPolicy().triggerOnEvent(new PushedAuthorizationRequestContext(client, authorizationRequest, decodedFormParameters));
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw errorResponseException(cpe.getError(), cpe.getErrorDetail(), Response.Status.BAD_REQUEST);
        }

        AuthorizationEndpointChecker checker = new AuthorizationEndpointChecker()
                .event(event)
                .client(client)
                .realm(realm)
                .request(authorizationRequest)
                .session(session);

        try {
            checker.checkRedirectUri();
        } catch (AuthorizationEndpointChecker.AuthorizationCheckException ex) {
            throw errorResponseException(OAuthErrorException.INVALID_REQUEST, "Invalid parameter: redirect_uri", Response.Status.BAD_REQUEST);
        }

        try {
            checker.checkResponseType();
        } catch (AuthorizationEndpointChecker.AuthorizationCheckException ex) {
            if (ex.getError().equals(OAuthErrorException.UNSUPPORTED_RESPONSE_TYPE)) {
                throw errorResponseException(OAuthErrorException.INVALID_REQUEST, "Unsupported response type", Response.Status.BAD_REQUEST);
            } else {
                checker.throwAsCorsErrorResponseException(cors, ex);
            }
        }

        try {
            checker.checkValidScope();
        } catch (AuthorizationEndpointChecker.AuthorizationCheckException ex) {
            // PAR 将 scope 校验失败映射为 invalid_request
            throw errorResponseException(OAuthErrorException.INVALID_REQUEST, ex.getErrorDescription(), Response.Status.BAD_REQUEST);
        }

        try {
            checker.checkInvalidRequestMessage();
            checker.checkOIDCRequest();
            checker.checkOIDCParams();
            checker.checkPKCEParams();
            checker.checkParDPoPParams();
        } catch (AuthorizationEndpointChecker.AuthorizationCheckException ex) {
            checker.throwAsCorsErrorResponseException(cors, ex);
        }

        Map<String, String> params = new HashMap<>();

        String key = SecretGenerator.getInstance().generateSecureID();
        String requestUri = REQUEST_URI_PREFIX + key;

        int expiresIn = realm.getParPolicy().getRequestUriLifespan();

        flattenDecodedFormParametersToParamsMap(decodedFormParameters, params);

        params.put(PAR_CREATED_TIME, String.valueOf(System.currentTimeMillis()));
        // 若存在 DPoP 证明，后续令牌请求须匹配同一公钥指纹
        DPoP dpop = session.getAttribute(DPoPUtil.DPOP_SESSION_ATTRIBUTE, DPoP.class);
        if (dpop != null) {
            params.put(PAR_DPOP_PROOF_JKT, dpop.getThumbprint());
        }

        SingleUseObjectProvider singleUseStore = session.singleUseObjects();
        singleUseStore.put(CACHE_KEY_PREFIX + key, expiresIn, params);

        ParResponse parResponse = new ParResponse(requestUri, expiresIn);

        session.getProvider(SecurityHeadersProvider.class).options().allowEmptyContentType();
        return cors.add(Response.status(Response.Status.CREATED)
                .entity(parResponse)
                .type(MediaType.APPLICATION_JSON_TYPE));
    }

    /**
     * 将表单多值映射扁平化为普通 Map（单次使用存储仅接受 Map）。
     * <p>多值参数仅保留首个值；空值参数作为占位写入且不会覆盖已有空值。</p>
     * @param decodedFormParameters 请求体表单参数
     * @param params 目标参数 Map
     */
    public static void flattenDecodedFormParametersToParamsMap(
            MultivaluedMap<String, String> decodedFormParameters,
            Map<String, String> params) {

        for (var parameterEntry : decodedFormParameters.entrySet()) {
            String parameterName = parameterEntry.getKey();
            List<String> parameterValues = parameterEntry.getValue();

            if (parameterValues.isEmpty()) {
                // 空参数作为占位符写入，且仅在键不存在时写入，避免意外覆盖
                params.putIfAbsent(parameterName, null);
            } else {
                // 多值列表仅取第一个值；会覆盖先前写入的空占位参数
                params.put(parameterName, parameterValues.get(0));
            }

        }
    }

}
