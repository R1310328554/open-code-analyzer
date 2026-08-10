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

package org.keycloak.constants;

/**
 * Keycloak 适配器（Adapter）与服务器交互时使用的 URL 端点、请求参数及 Cookie 名称常量。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface AdapterConstants {

    // URL 端点参数名
    public static final String K_LOGOUT = "k_logout";
    public static final String K_PUSH_NOT_BEFORE = "k_push_not_before";
    public static final String K_TEST_AVAILABLE = "k_test_available";
    public static final String K_QUERY_BEARER_TOKEN = "k_query_bearer_token";
    public static final String K_JWKS = "k_jwks";

    // 该参数名在 Keycloak Subsystem 类 org.keycloak.subsystem.extensionKeycloakAdapterConfigDeploymentProcessor 中再次定义。
    // 两处重复定义是为避免 Keycloak Subsystem 与 Undertow 集成模块之间的依赖。
    String AUTH_DATA_PARAM_NAME = "org.keycloak.json.adapterConfig";

    // codeToToken 请求中由适配器传入、并保存至 ClientSession 的属性：适配器侧 HttpSession 的 ID
    public static final String CLIENT_SESSION_STATE = "client_session_state";

    // codeToToken 请求中由适配器传入、并保存至 ClientSession 的属性：承载 HttpSession 的适配器主机名
    public static final String CLIENT_SESSION_HOST = "client_session_host";

    // registerNode 请求中用于登记新加入集群的应用节点的主机属性
    public static final String CLIENT_CLUSTER_HOST = "client_cluster_host";

    // 适配器侧存储令牌信息的 Cookie 名；仅在 tokenStore 配置为 COOKIE 时使用
    public static final String KEYCLOAK_ADAPTER_STATE_COOKIE = "KEYCLOAK_ADAPTER_STATE";

    // 请求参数：指定用于认证用户的外部身份提供者标识（IdP hint）
    String KC_IDP_HINT = "kc_idp_hint";
}
