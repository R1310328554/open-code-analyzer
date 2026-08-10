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

package org.keycloak.protocol.oidc.endpoints.request;

import java.util.HashSet;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.Profile;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCConfigAttributes;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.par.endpoints.request.AuthzEndpointParParser;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.ErrorPageException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.util.AuthorizationContextUtil;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;

/**
 * 授权端点请求解析协调器。
 * <p>先解析查询串，再按需合并 {@code request}/{@code request_uri}（含 PAR），并可选构建参数化 scope 的 {@link org.keycloak.rar.AuthorizationRequestContext}。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationEndpointRequestParserProcessor {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(AuthorizationEndpointRequestParserProcessor.class);

    /**
     * 解析完整授权端点请求。
     * @param event 事件构建器
     * @param session Keycloak 会话
     * @param client 客户端
     * @param requestParams HTTP 查询/表单参数
     * @param endpointType 端点类型（影响 response_type 是否必填）
     * @return 解析结果；无效时 {@link AuthorizationEndpointRequest#getInvalidRequestMessage()} 非空
     */
    public static AuthorizationEndpointRequest parseRequest(EventBuilder event, KeycloakSession session, ClientModel client, MultivaluedMap<String, String> requestParams, EndpointType endpointType) {
        try {
            AuthorizationEndpointRequest request = new AuthorizationEndpointRequest();
            boolean isResponseTypeParameterRequired = isResponseTypeParameterRequired(requestParams, endpointType);
            AuthzEndpointQueryStringParser parser = new AuthzEndpointQueryStringParser(session, requestParams, isResponseTypeParameterRequired);
            parser.parseRequest(request);

            if (parser.getInvalidRequestMessage() != null) {
                request.invalidRequestMessage = parser.getInvalidRequestMessage();
            }
            if (request.getInvalidRequestMessage() != null) {
                return request;
            }

            String requestParam = requestParams.getFirst(OIDCLoginProtocol.REQUEST_PARAM);
            String requestUriParam = requestParams.getFirst(OIDCLoginProtocol.REQUEST_URI_PARAM);

            if (requestParam != null && requestUriParam != null) {
                throw new RuntimeException("Illegal to use both 'request' and 'request_uri' parameters together");
            }

            String requestObjectRequired = OIDCAdvancedConfigWrapper.fromClientModel(client).getRequestObjectRequired();

            if (OIDCConfigAttributes.REQUEST_OBJECT_REQUIRED_REQUEST_OR_REQUEST_URI.equals(requestObjectRequired)
                    && requestParam == null && requestUriParam == null) {
                throw new RuntimeException("Client is required to use 'request' or 'request_uri' parameter.");
            } else if (OIDCConfigAttributes.REQUEST_OBJECT_REQUIRED_REQUEST.equals(requestObjectRequired)
                    && requestParam == null) {
                throw new RuntimeException("Client is required to use 'request' parameter.");
            } else if (OIDCConfigAttributes.REQUEST_OBJECT_REQUIRED_REQUEST_URI.equals(requestObjectRequired)
                    && requestUriParam == null) {
                throw new RuntimeException("Client is required to use 'request_uri' parameter.");
            }

            if (requestParam != null) {
                new AuthzEndpointRequestObjectParser(session, requestParam, client).parseRequest(request);
            } else if (requestUriParam != null) {
                // 区分 PAR（urn:ietf:params:oauth:request_uri:）与普通 Request Object URI
                RequestUriType requestUriType = getRequestUriType(requestUriParam);
                if (requestUriType == RequestUriType.PAR) {
                    new AuthzEndpointParParser(session, client, requestUriParam).parseRequest(request);
                } else {
                    // 校验 request_uri 是否在客户端允许的 request_uris 列表内
                    List<String> requestUris = OIDCAdvancedConfigWrapper.fromClientModel(client).getRequestUris();
                    String requestUri = RedirectUtils.verifyRedirectUri(session, client.getRootUrl(), requestUriParam, new HashSet<>(requestUris), false);
                    if (requestUri == null) {
                        throw new RuntimeException("Specified 'request_uri' not allowed for this client.");
                    }
                    String retrievedRequest = session.getProvider(HttpClientProvider.class).getString(requestUri);
                    new AuthzEndpointRequestObjectParser(session, retrievedRequest, client).parseRequest(request);
                }
            }

            if (Profile.isFeatureEnabled(Profile.Feature.PARAMETERIZED_SCOPES)) {
                request.authorizationRequestContext = AuthorizationContextUtil.getAuthorizationRequestContextFromScopes(session, client, request.getScope());
            }

            return request;

        } catch (Exception e) {
            ServicesLogger.LOGGER.invalidRequest(e);
            event.detail(Details.REASON, e.getMessage());
            event.error(Errors.INVALID_REQUEST);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
        }
    }

    /**
     * 从请求参数提取唯一 {@code client_id}。
     * @return 客户端 ID
     * @throws org.keycloak.services.ErrorPageException 缺失或重复时
     */
    public static String getClientId(EventBuilder event, KeycloakSession session, MultivaluedMap<String, String> requestParams) {
        List<String> clientParam = requestParams.get(OIDCLoginProtocol.CLIENT_ID_PARAM);
        if (clientParam != null && clientParam.size() == 1) {
            return clientParam.get(0);
        } else {
            String errorMessage = "Parameter 'client_id' not present or present multiple times in the HTTP request parameters";
            logger.warnf(errorMessage);
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.INVALID_REQUEST);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
        }
    }

    /** 判断 {@code request_uri} 为 PAR 还是外部 Request Object URL。 */
    public static RequestUriType getRequestUriType(String requestUri) {
        if (requestUri == null) {
            throw new RuntimeException("'request_uri' parameter is null");
        }

        return requestUri.toLowerCase().startsWith("urn:ietf:params:oauth:request_uri:")
                       ? RequestUriType.PAR
                       : RequestUriType.REQUEST_OBJECT;
    }


    // OIDC 授权端点通常要求 response_type；PAR 请求等场景例外
    // The only exception when it is not mandatory is the case when request to authentication endpoint was sent after PAR request
    private static boolean isResponseTypeParameterRequired(MultivaluedMap<String, String> requestParams, EndpointType endpointType) {
        if (endpointType != EndpointType.OIDC_AUTH_ENDPOINT) return false;

        String scopeParam = requestParams.getFirst(OAuth2Constants.SCOPE);
        if (!TokenUtil.isOIDCRequest(scopeParam)) {
            return false;
        }

        String requestUriParam = requestParams.getFirst(OIDCLoginProtocol.REQUEST_URI_PARAM);
        if (requestUriParam != null && getRequestUriType(requestUriParam) == RequestUriType.PAR) {
            return false;
        }

        return true;
    }

    /** 授权请求来源端点类型。 */
    public enum EndpointType {
        /** 标准 OIDC 授权端点。 */
        OIDC_AUTH_ENDPOINT,
        /** OAuth 2.0 设备授权端点。 */
        OAUTH2_DEVICE_ENDPOINT,
        /** Docker 注册端点。 */
        DOCKER_ENDPOINT
    }

}
