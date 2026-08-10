/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.client;

import java.util.Map;

import org.keycloak.protocol.oidc.client.authentication.ClientCredentialsProvider;
import org.keycloak.protocol.oidc.client.authentication.ClientCredentialsProviderUtils;
import org.keycloak.representations.adapters.config.AdapterConfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * 授权客户端配置，扩展 {@link AdapterConfig} 并提供 HTTP 客户端与凭证提供者。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class Configuration extends AdapterConfig {

    @JsonIgnore
    private HttpClient httpClient;

    @JsonIgnore
    private ClientCredentialsProvider clientCredentialsProvider;

    public Configuration() {

    }

    /**
     * 创建新实例。
     *
     * @param authServerUrl 授权服务器 URL，例如 http://{server}:{port}/auth（不可为 {@code null}）
     * @param realm 领域名称（不可为 {@code null}）
     * @param clientId 客户端 ID（不可为 {@code null}）
     * @param clientCredentials 客户端凭证映射（不可为 {@code null}）
     * @param httpClient 用于向服务器发送请求的 {@link HttpClient}；为 {@code null} 时使用默认实例
     */
    public Configuration(String authServerUrl, String realm, String clientId, Map<String, Object> clientCredentials, HttpClient httpClient) {
        this.authServerUrl = authServerUrl;
        setAuthServerUrl(authServerUrl);
        setRealm(realm);
        setResource(clientId);
        setCredentials(clientCredentials);
        this.httpClient = httpClient;
    }

    /** 懒加载默认 {@link HttpClient}。 */
    public HttpClient getHttpClient() {
        if (this.httpClient == null) {
            this.httpClient = HttpClients.createDefault();
        }
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setClientCredentialsProvider(ClientCredentialsProvider clientCredentialsProvider) {
        this.clientCredentialsProvider = clientCredentialsProvider;
    }

    /** 按需引导并返回客户端凭证提供者。 */
    public ClientCredentialsProvider getClientCredentialsProvider() {
        if (clientCredentialsProvider == null) {
            clientCredentialsProvider = ClientCredentialsProviderUtils.bootstrapClientAuthenticator(this);
        }
        return clientCredentialsProvider;
    }
}
