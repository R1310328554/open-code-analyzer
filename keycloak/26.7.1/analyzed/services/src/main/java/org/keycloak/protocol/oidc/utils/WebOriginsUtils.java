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

import java.util.HashSet;
import java.util.Set;

import org.keycloak.common.util.UriUtils;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;

/**
 * Web 来源（CORS）解析工具。
 * <p>合并客户端配置的 {@code webOrigins}；若含 {@link Constants#INCLUDE_REDIRECTS}，则从合法重定向 URI 提取 http(s) origin。</p>
 */
public class WebOriginsUtils {

    /**
     * 解析客户端有效 Web 来源集合。
     * @param session Keycloak 会话
     * @param client 客户端模型
     * @return 允许的来源 origin 集合
     */
    public static Set<String> resolveValidWebOrigins(KeycloakSession session, ClientModel client) {
        Set<String> origins = new HashSet<>();
        if (client.getWebOrigins() != null) {
            origins.addAll(client.getWebOrigins());
        }
        if (origins.contains(Constants.INCLUDE_REDIRECTS)) {
            origins.remove(Constants.INCLUDE_REDIRECTS);
            for (String redirectUri : RedirectUtils.resolveValidRedirects(session, client.getRootUrl(), client.getRedirectUris())) {
                if (redirectUri.startsWith("http://") || redirectUri.startsWith("https://")) {
                    origins.add(UriUtils.getOrigin(redirectUri));
                }
            }
        }
        return origins;
    }

}
