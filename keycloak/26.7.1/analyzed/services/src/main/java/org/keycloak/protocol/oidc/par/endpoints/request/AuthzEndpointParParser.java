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

package org.keycloak.protocol.oidc.par.endpoints.request;

import java.util.Map;
import java.util.Set;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.protocol.oidc.endpoints.request.AuthzEndpointRequestParser;
import org.keycloak.protocol.oidc.par.endpoints.ParEndpoint;

import org.jboss.logging.Logger;

import static org.keycloak.protocol.oidc.par.endpoints.ParEndpoint.CACHE_KEY_PREFIX;
import static org.keycloak.protocol.oidc.par.endpoints.ParEndpoint.PAR_CREATED_TIME;
import static org.keycloak.protocol.oidc.par.endpoints.ParEndpoint.PAR_DPOP_PROOF_JKT;

/**
 * PAR request_uri 授权端点请求解析器。
 * <p>从单次使用存储中加载 PAR 参数，并在后续授权请求中解析；支持 JAR 请求对象与 DPoP JKT 传递。</p>
 */
public class AuthzEndpointParParser extends AuthzEndpointRequestParser {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(AuthzEndpointParParser.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 请求客户端 */
    private final ClientModel client;
    /** 从 PAR 存储加载的请求参数 */
    private final Map<String, String> requestParams;

    /**
     * 加载并校验 PAR 参数。
     * @param session Keycloak 会话
     * @param client 客户端
     * @param requestUri PAR 返回的 request_uri
     */
    public AuthzEndpointParParser(KeycloakSession session, ClientModel client, String requestUri) {
        super(session);
        this.session = session;
        this.client = client;
        Map<String, String> retrievedRequest = getRequestObject(session, requestUri);
        if (retrievedRequest == null) {
            throw new RuntimeException("PAR not found, not issued or used multiple times.");
        }

        RealmModel realm = session.getContext().getRealm();
        int expiresIn = realm.getParPolicy().getRequestUriLifespan();
        long created = Long.parseLong(retrievedRequest.get(PAR_CREATED_TIME));
        if (System.currentTimeMillis() - created < (expiresIn * 1000L)) {
            requestParams = retrievedRequest;
        } else {
            throw new RuntimeException("PAR expired.");
        }
        // 若 PAR 阶段存在 DPoP 证明，将 JKT 写入会话供后续令牌请求校验
        String dpopJkt = retrievedRequest.get(PAR_DPOP_PROOF_JKT);
        if (dpopJkt != null) {
            session.setAttribute(PAR_DPOP_PROOF_JKT, dpopJkt);
        }
    }

    /** 解析 PAR 参数；若含 request 对象则优先使用 JAR 解析 @param request 授权端点请求 */
    @Override
    public void parseRequest(AuthorizationEndpointRequest request) {
        String requestParam = requestParams.get(OIDCLoginProtocol.REQUEST_PARAM);

        if (requestParam != null) {
            // PAR 使用 JAR 注册时解析 request 对象；其参数优先于直接提交的参数
            new ParEndpointRequestObjectParser(session, requestParam, client).parseRequest(request);
        } else {
            super.parseRequest(request);
        }
    }

    @Override
    protected String getParameter(String paramName) {
        return requestParams.get(paramName);
    }

    @Override
    protected Integer getIntParameter(String paramName) {
        String paramVal = requestParams.get(paramName);
        return paramVal == null ? null : Integer.valueOf(paramVal);
    }

    @Override
    protected Set<String> keySet() {
        return requestParams.keySet();
    }
    
    /** 读取 PAR 存储的参数（不删除） @param requestUri request_uri @return 参数 Map，不存在时返回 null */
    public static Map<String, String> getRequestObject(KeycloakSession session, String requestUri) {
        String key = getRequestObjectKey(requestUri);
        SingleUseObjectProvider singleUseStore = session.singleUseObjects();
        Map<String, String> retrievedRequest = singleUseStore.get(CACHE_KEY_PREFIX + key);
        return retrievedRequest;
    }

    /**
     * 一次性 request_uri 在授权完成时消耗，而非访问授权端点时（FAPI2 PAR 一致性测试）。
     */
    /** 移除并返回 PAR 存储的参数（授权完成时调用） @return 已存储的参数 Map */
    public static Map<String, String> removeRequestObject(KeycloakSession session, String requestUri) {
        String key = getRequestObjectKey(requestUri);
        return session.singleUseObjects().remove(CACHE_KEY_PREFIX + key);
    }

    private static String getRequestObjectKey(String requestUri) {
        try {
            return requestUri.substring(ParEndpoint.REQUEST_URI_PREFIX_LENGTH);
        } catch (RuntimeException re) {
            logger.warnf(re,"Unable to parse request_uri: %s", requestUri);
            throw new RuntimeException("Unable to parse request_uri");
        }
    }
}
