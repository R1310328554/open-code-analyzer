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

package org.keycloak.protocol.oidc.client.authentication;

import java.util.Map;

import org.keycloak.OAuth2Constants;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.util.BasicAuthHelper;

import org.jboss.logging.Logger;

/**
 * 传统 OAuth2 客户端认证：基于 client_id 与 client_secret（机密客户端用 Basic Auth，公开客户端仅传 client_id）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientIdAndSecretCredentialsProvider implements ClientCredentialsProvider {

    private static Logger logger = Logger.getLogger(ClientIdAndSecretCredentialsProvider.class);

    /** 提供者 ID，与 {@link CredentialRepresentation#SECRET} 一致。 */
    public static final String PROVIDER_ID = CredentialRepresentation.SECRET;

    /** 客户端密钥。 */
    private String clientSecret;

    /** @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /**
     * 从配置读取 client_secret。
     *
     * @param deployment 适配器配置
     * @param config 密钥字符串（可为 null）
     */
    @Override
    public void init(AdapterConfig deployment, Object config) {
        clientSecret = (config == null ? null : config.toString());
    }

    /**
     * 机密客户端写入 RFC 6749 Basic Authorization 头；公开客户端将 client_id 放入表单参数。
     *
     * @param deployment 适配器配置
     * @param requestHeaders HTTP 请求头
     * @param formParams 表单参数
     */
    @Override
    public void setClientCredentials(AdapterConfig deployment, Map<String, String> requestHeaders, Map<String, String> formParams) {
        String clientId = deployment.getResource();

        if (!deployment.isPublicClient()) {
            if (clientSecret != null) {
                String authorization = BasicAuthHelper.RFC6749.createHeader(clientId, clientSecret);
                requestHeaders.put("Authorization", authorization);
            } else {
                logger.warnf("Client '%s' doesn't have secret available", clientId);
            }
        } else {
            formParams.put(OAuth2Constants.CLIENT_ID, clientId);
        }
    }
}
