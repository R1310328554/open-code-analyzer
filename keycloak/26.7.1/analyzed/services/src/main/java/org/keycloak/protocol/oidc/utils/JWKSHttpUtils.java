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

package org.keycloak.protocol.oidc.utils;

import java.io.IOException;

import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

/**
 * JWKS HTTP 工具：通过 HTTP 客户端拉取远程 JWKS 并反序列化。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JWKSHttpUtils {

    /**
     * 向指定 JWKS URI 发送 GET 请求并解析为 {@link JSONWebKeySet}。
     * @param session Keycloak 会话（提供 HTTP 客户端）
     * @param jwksURI JWKS 端点 URI
     * @return 解析后的 JSON Web Key Set
     * @throws IOException HTTP 或 JSON 解析失败
     */
        String keySetString = session.getProvider(HttpClientProvider.class).getString(jwksURI);
        return JsonSerialization.readValue(keySetString, JSONWebKeySet.class);
    }
}
