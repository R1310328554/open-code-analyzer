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
package org.keycloak.protocol.oidc.grants.ciba.channel;

import java.io.IOException;
import java.util.Map;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpRequest;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.cors.Cors;
import org.keycloak.util.TokenUtil;

/**
 * 基于 HTTP 的 CIBA 认证通道 Provider：向外部 URI POST JSON 认证请求。
 * <p>使用 Bearer 令牌认证，成功时期望 HTTP 201 Created。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class HttpAuthenticationChannelProvider implements AuthenticationChannelProvider{

    /** 认证通道 ID 参数/声明名 */
    public static final String AUTHENTICATION_CHANNEL_ID = "authentication_channel_id";

    /** 当前 Keycloak 会话 */
    protected KeycloakSession session;
    /** 表单参数（子类/扩展用） */
    protected MultivaluedMap<String, String> formParams;
    /** 当前领域 */
    protected RealmModel realm;
    /** 客户端认证属性 */
    protected Map<String, String> clientAuthAttributes;
    /** CORS 处理器 */
    protected Cors cors;
    /** 认证通道 HTTP(S) 目标 URI */
    protected final String httpAuthenticationChannelUri;

    /**
     * @param session Keycloak 会话
     * @param httpAuthenticationRequestUri 认证通道请求 URI
     */
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.httpAuthenticationChannelUri = httpAuthenticationRequestUri;
    }

    /**
     * 向认证通道 URI 发送后台认证请求。
     * <p>构造 {@link AuthenticationChannelRequest} 并以 Bearer 令牌 POST；返回 HTTP 201 时视为成功。</p>
     * @param request 已解析的 CIBA 认证请求
     * @param infoUsedByAuthenticator 供认证器使用的登录提示信息
     * @return 通道接受请求时为 true
     */
        // 以与 auth_req_id 相同方式构造 JWS 签名/JWE 加密的认证通道 ID
        // 认证通道 ID 将后台认证请求与 AD 上的用户认证绑定
        // JWE 序列化的通道 ID 作为 Bearer 令牌，内含 client_id
        // 供通道回调端点识别发起请求的消费设备（CD）
        // that sent Backchannel Authentication Request.

        // AD 上应展示的作用域来源：
        // 1. 授权请求中显式指定的 scope 参数
        // 2. Keycloak 客户端默认作用域

        checkAuthenticationChannel();

        ClientModel client = request.getClient();

        try {
            AuthenticationChannelRequest channelRequest = new AuthenticationChannelRequest();

            channelRequest.setScope(request.getScope());
            channelRequest.setBindingMessage(request.getBindingMessage());
            channelRequest.setLoginHint(infoUsedByAuthenticator);
            channelRequest.setConsentRequired(client.isConsentRequired());
            channelRequest.setAcrValues(request.getAcrValues());
            channelRequest.setAdditionalParameters(request.getOtherClaims());

            SimpleHttpRequest simpleHttp = SimpleHttp.create(session).doPost(httpAuthenticationChannelUri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .json(channelRequest)
                    .auth(createBearerToken(request, client));
 
            int status = completeDecoupledAuthnRequest(simpleHttp, channelRequest).asStatus();

            if (status == Status.CREATED.getStatusCode()) {
                return true;
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Authentication Channel Access failed.", ioe);
        }

        return false;
    }

    /** 为通道 POST 构造短期 Bearer 访问令牌 */
    private String createBearerToken(CIBAAuthenticationRequest request, ClientModel client) {
        AccessToken bearerToken = new AccessToken();

        bearerToken.type(TokenUtil.TOKEN_TYPE_BEARER);
        bearerToken.issuer(request.getIssuer());
        bearerToken.id(request.getAuthResultId());
        bearerToken.issuedFor(client.getClientId());
        bearerToken.audience(request.getIssuer());
        bearerToken.iat(request.getIat());
        bearerToken.exp(request.getExp());
        bearerToken.subject(request.getSubject());

        return session.tokens().encode(bearerToken);
    }

    /** 校验认证通道 URI 已配置且为 http(s) 协议 */
    protected void checkAuthenticationChannel() {
        if (httpAuthenticationChannelUri == null) {
            throw new RuntimeException("Authentication Channel Request URI not set properly.");
        }
        if (!httpAuthenticationChannelUri.startsWith("http://") && !httpAuthenticationChannelUri.startsWith("https://")) {
            throw new RuntimeException("Authentication Channel Request URI not set properly.");
        }
    }

    /**
     * 扩展点：子类可覆盖以向解耦认证服务器追加 POST 数据。
     * @param simpleHttp 已配置的 HTTP 请求
     * @param channelRequest 认证通道请求体
     * @return 完善后的 HTTP 请求
     */
    protected SimpleHttpRequest completeDecoupledAuthnRequest(SimpleHttpRequest simpleHttp, AuthenticationChannelRequest channelRequest) {
        return simpleHttp;
    }

    /** Provider 关闭钩子（无资源释放） */
    @Override
    public void close() {

    }
}
