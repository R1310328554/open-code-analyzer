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
 *
 */

package org.keycloak.protocol.oidc.grants.ciba.endpoints.request;

import java.util.HashSet;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuthErrorException;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.CibaConfig;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.ServicesLogger;

/**
 * 后台认证端点请求解析处理器。
 * <p>协调表单体解析与签名 request/request_uri 解析，返回完整的 {@link BackchannelAuthenticationEndpointRequest}。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class BackchannelAuthenticationEndpointRequestParserProcessor {

    /**
     * 解析后台认证端点完整请求。
     * <p>先解析表单体，再按需解析内联 {@code request} 或远程 {@code request_uri} 签名对象。</p>
     * @param event 事件构建器
     * @param session Keycloak 会话
     * @param client 客户端模型
     * @param requestParams 表单参数
     * @param config CIBA 策略配置
     * @return 解析后的请求对象
     */
        try {
            BackchannelAuthenticationEndpointRequest request = new BackchannelAuthenticationEndpointRequest();

            BackchannelAuthenticationEndpointRequestBodyParser parser = new BackchannelAuthenticationEndpointRequestBodyParser(requestParams);
            parser.parseRequest(request);

            if (parser.getInvalidRequestMessage() != null) {
                request.invalidRequestMessage = parser.getInvalidRequestMessage();
                return request;
            }

            String requestParam = requestParams.getFirst(OIDCLoginProtocol.REQUEST_PARAM);
            String requestUriParam = requestParams.getFirst(OIDCLoginProtocol.REQUEST_URI_PARAM);

            if (requestParam != null && requestUriParam != null) {
                throw new RuntimeException("Illegal to use both 'request' and 'request_uri' parameters together");
            }

            if (requestParam != null) {
                new BackchannelAuthenticationEndpointSignedRequestParser(session, requestParam, client, config).parseRequest(request);
            } else if (requestUriParam != null) {
                // 校验 request_uri 是否在客户端允许的 requestUris 列表中
                List<String> requestUris = OIDCAdvancedConfigWrapper.fromClientModel(client).getRequestUris();
                String requestUri = RedirectUtils.verifyRedirectUri(session, client.getRootUrl(), requestUriParam, new HashSet<>(requestUris), false);
                if (requestUri == null) {
                    throw new RuntimeException("Specified 'request_uri' not allowed for this client.");
                }

                String retrievedRequest = session.getProvider(HttpClientProvider.class).getString(requestUri);
                new BackchannelAuthenticationEndpointSignedRequestParser(session, retrievedRequest, client, config).parseRequest(request);
            }

            return request;

        } catch (Exception e) {
            ServicesLogger.LOGGER.invalidRequest(e);
            event.detail(Details.REASON, e.getMessage());
            event.error(Errors.INVALID_REQUEST);
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }
}
