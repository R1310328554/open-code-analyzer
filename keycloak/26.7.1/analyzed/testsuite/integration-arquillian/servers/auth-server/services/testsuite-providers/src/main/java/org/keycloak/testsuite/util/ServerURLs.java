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

package org.keycloak.testsuite.util;

import static java.lang.Integer.parseInt;

/**
 * 测试套件服务器 URL 工具类：从系统属性读取认证服务器与应用服务器的
 * 主机、端口、协议等配置，并构建上下文根 URL。
 */
public class ServerURLs {
    /** 认证服务器是否要求 SSL。 */
    public static final boolean AUTH_SERVER_SSL_REQUIRED = Boolean.parseBoolean(System.getProperty("auth.server.ssl.required", "true"));
    /** 认证服务器 HTTP 端口。 */
    public static final String AUTH_SERVER_PORT_HTTP = System.getProperty("auth.server.http.port", "8180");
    /** 认证服务器 HTTPS 端口。 */
    public static final String AUTH_SERVER_PORT_HTTPS = System.getProperty("auth.server.https.port", "8543");
    /** 当前使用的认证服务器端口（按 SSL 要求选择 HTTP/HTTPS）。 */
    public static final String AUTH_SERVER_PORT = AUTH_SERVER_SSL_REQUIRED ? AUTH_SERVER_PORT_HTTPS : AUTH_SERVER_PORT_HTTP;
    /** 认证服务器 URL 协议（http 或 https）。 */
    public static final String AUTH_SERVER_SCHEME = AUTH_SERVER_SSL_REQUIRED ? "https" : "http";
    /** 认证服务器主机名。 */
    public static final String AUTH_SERVER_HOST = System.getProperty("auth.server.host", "localhost");
    /** 认证服务器第二主机名（集群测试用）。 */
    public static final String AUTH_SERVER_HOST2 = System.getProperty("auth.server.host2", AUTH_SERVER_HOST);

    /** 应用服务器是否要求 SSL。 */
    public static final boolean APP_SERVER_SSL_REQUIRED = Boolean.parseBoolean(System.getProperty("app.server.ssl.required", "false"));
    /** 应用服务器端口。 */
    public static final String APP_SERVER_PORT = APP_SERVER_SSL_REQUIRED ? System.getProperty("app.server.https.port", "8643") : System.getProperty("app.server.http.port", "8280");
    /** 应用服务器 URL 协议。 */
    public static final String APP_SERVER_SCHEME = APP_SERVER_SSL_REQUIRED ? "https" : "http";
    /** 应用服务器主机名。 */
    public static final String APP_SERVER_HOST = System.getProperty("app.server.host", "localhost");

    /** 返回认证服务器上下文根 URL（无集群端口偏移）。 */
    public static String getAuthServerContextRoot() {
        return getAuthServerContextRoot(0);
    }

    /**
     * 返回带集群端口偏移的认证服务器上下文根 URL。
     *
     * @param clusterPortOffset 端口偏移量
     */
    public static String getAuthServerContextRoot(int clusterPortOffset) {
        return removeDefaultPorts(String.format("%s://%s:%s", AUTH_SERVER_SCHEME, AUTH_SERVER_HOST, parseInt(AUTH_SERVER_PORT) + clusterPortOffset));
    }

    /** 返回应用服务器上下文根 URL（无集群端口偏移）。 */
    public static String getAppServerContextRoot() {
        return getAppServerContextRoot(0);
    }

    /**
     * 返回带集群端口偏移的应用服务器上下文根 URL。
     *
     * @param clusterPortOffset 端口偏移量
     */
    public static String getAppServerContextRoot(int clusterPortOffset) {
        return removeDefaultPorts(String.format("%s://%s:%s", APP_SERVER_SCHEME, APP_SERVER_HOST, parseInt(APP_SERVER_PORT) + clusterPortOffset));
    }

    /**
     * 从 URL 中移除默认端口 80 和 443。
     *
     * @param url 原始 URL
     * @return 去除默认端口后的 URL
     */
    public static String removeDefaultPorts(String url) {
        return url != null ? url.replaceFirst("(.*)(:80)(\\/.*)?$", "$1$3").replaceFirst("(.*)(:443)(\\/.*)?$", "$1$3") : null;
    }

    /** 从系统属性解析端口号，解析失败时抛出运行时异常。 */
    private static int parsePort(String property) {
        try {
            return parseInt(System.getProperty(property));
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Failed to get " + property, ex);
        }
    }
}
