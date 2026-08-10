/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.par.endpoints.request;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.protocol.oidc.endpoints.request.AuthzEndpointRequestObjectParser;

/**
 * PAR 端点请求对象（JAR）解析器。
 * <p>解析 PAR 请求中的 signed/encrypted request 对象；按规范忽略直接参数，以 request 对象值为准。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ParEndpointRequestObjectParser extends AuthzEndpointRequestObjectParser {

    /**
     * @param session Keycloak 会话
     * @param requestObject 请求对象 JWT/字符串
     * @param client 客户端
     */
    public ParEndpointRequestObjectParser(KeycloakSession session, String requestObject, ClientModel client) {
        super(session, requestObject, client);
    }

    /** PAR 规范：始终以 request 对象中的值覆盖直接参数 @return 新值 */
    @Override
    protected <T> T replaceIfNotNull(T previousVal, T newVal) {
        // 按规范强制使用 request 对象值，忽略直接提交的参数
        return newVal;
    }

    /** PAR 下不校验重复 response_type（直接参数已被忽略） */
    @Override
    protected void validateResponseTypeParameter(String responseType, AuthorizationEndpointRequest request) {
        // 直接参数被忽略，无需校验重复 response_type；以 request 对象值为准
    }
}
