/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.keycloak.representations.adapters.config.AdapterConfig;

import org.jboss.logging.Logger;

/**
 * {@link ClientCredentialsProvider} 引导与 SPI 加载工具类。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientCredentialsProviderUtils {

    private static Logger logger = Logger.getLogger(ClientCredentialsProviderUtils.class);

    /**
     * 根据适配器配置选择并初始化客户端凭据提供者。
     *
     * @param deployment 适配器部署配置
     * @return 已初始化的 {@link ClientCredentialsProvider}
     */
    public static ClientCredentialsProvider bootstrapClientAuthenticator(AdapterConfig deployment) {
        String clientId = deployment.getResource();
        Map<String, Object> clientCredentials = deployment.getCredentials();

        String authenticatorId;
        if (clientCredentials == null || clientCredentials.isEmpty()) {
            authenticatorId = ClientIdAndSecretCredentialsProvider.PROVIDER_ID;
        } else {
            authenticatorId = (String) clientCredentials.get("provider");
            if (authenticatorId == null) {
                // 若仅配置一种凭据类型，则以其键名作为 provider ID
                if (clientCredentials.size() == 1) {
                    authenticatorId = clientCredentials.keySet().iterator().next();
                } else {
                    throw new RuntimeException("Can't identify clientAuthenticator from the configuration of client '" + clientId + "' . Check your adapter configurations");
                }
            }
        }

        logger.debugf("Using provider '%s' for authentication of client '%s'", authenticatorId, clientId);

        Map<String, ClientCredentialsProvider> authenticators = new HashMap<>();
        loadAuthenticators(authenticators, ClientCredentialsProviderUtils.class.getClassLoader());
        loadAuthenticators(authenticators, Thread.currentThread().getContextClassLoader());

        ClientCredentialsProvider authenticator = authenticators.get(authenticatorId);
        if (authenticator == null) {
            throw new RuntimeException("Couldn't find ClientCredentialsProvider implementation class with id: " + authenticatorId + ". Loaded authentication providers: " + authenticators.keySet());
        }

        Object config = (clientCredentials==null) ? null : clientCredentials.get(authenticatorId);
        authenticator.init(deployment, config);

        return authenticator;
    }

    /**
     * 通过 {@link ServiceLoader} 从指定类加载器加载所有 {@link ClientCredentialsProvider} 实现。
     *
     * @param authenticators 输出 Map（按 {@link ClientCredentialsProvider#getId()} 索引）
     * @param classLoader 类加载器
     */
    public static void loadAuthenticators(Map<String, ClientCredentialsProvider> authenticators, ClassLoader classLoader) {
        Iterator<ClientCredentialsProvider> iterator = ServiceLoader.load(ClientCredentialsProvider.class, classLoader).iterator();
        while (iterator.hasNext()) {
            try {
                ClientCredentialsProvider authenticator = iterator.next();
                logger.debugf("Loaded clientCredentialsProvider %s", authenticator.getId());
                authenticators.put(authenticator.getId(), authenticator);
            } catch (ServiceConfigurationError e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to load clientCredentialsProvider with classloader: " + classLoader, e);
                }
            }
        }
    }

    /**
     * 在应用内直接发起反向通道请求时使用（参见 demo 中的 service-account 示例）。
     *
     * @param deployment 适配器配置
     * @param authenticator 已初始化的凭据提供者
     * @param requestHeaders HTTP 请求头 Map
     * @param formparams 表单参数 Map
     */
    public static void setClientCredentials(AdapterConfig deployment, ClientCredentialsProvider authenticator, Map<String, String> requestHeaders, Map<String, String> formparams) {
        authenticator.setClientCredentials(deployment, requestHeaders, formparams);
    }
}
