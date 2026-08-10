/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.util.ResolveRelative;

import org.jboss.logging.Logger;

/**
 * 客户端主机校验工具：将动态主机值（如 client_session_host）与客户端已注册 URL 比对，防止 SSRF 攻击。
 * <p>修复 [CVE-2026-4874] OIDC token 端点 SSRF 漏洞。</p>
 */
public class ClientHostUtils {

    private static final Logger logger = Logger.getLogger(ClientHostUtils.class);

    /**
     * 校验 hostname 是否匹配客户端注册节点或 Management/Admin URL 中的主机。
     * <p>仅允许 [1] Management/Admin URL 内主机；[2] 已注册集群节点主机。</p>
     * @param hostname 待校验的主机名（可含端口）
     * @param client 含注册 URL 的客户端模型
     * @param session 用于相对 URL 解析的 Keycloak 会话
     * @return 匹配任一允许主机则 true
     */
    public static boolean isHostAllowedForClient(String hostname, ClientModel client, KeycloakSession session) {
        if (hostname == null || hostname.trim().isEmpty()) {
            return false;
        }

        if (client == null) {
            return false;
        }

        // 提取纯主机名（去除端口）
        String bareHostname = extractHostname(hostname);

        // 从客户端注册节点列表收集允许主机
        List<String> allowedHosts = extractHostsFromClientManagedHosts(client, hostname);

        // 从 Management URL 解析允许主机
        addHostFromUrl(client.getManagementUrl(), client, session, allowedHosts);

        // 不区分大小写比对 hostname 与允许列表
        for (String allowedHost : allowedHosts) {
            if (allowedHost != null && allowedHost.equalsIgnoreCase(bareHostname)) {
                logger.debugf("Host '%s' matches allowed host '%s' for client '%s'",
                        hostname, allowedHost, client.getClientId());
                return true;
            }
        }
        logger.debugf("Host '%s' does not match any registered URL for client '%s'. Allowed hosts: %s",
                hostname, client.getClientId(), allowedHosts);
        return false;
    }

    private static String extractHostname(String hostPort) {
        if (hostPort == null) {
            return null;
        }

        try {
            // 输入为 hostname:port，需补 scheme 才能解析
            return new URI("https://" + hostPort).getHost();
        } catch (URISyntaxException e) {
            logger.debugf("Could not parse hostname: %s", hostPort);
            return null;
        }
    }

    private static void addHostFromUrl(String url, ClientModel client, KeycloakSession session, List<String> hosts) {
        if (url == null || url.isEmpty()) {
            return;
        }

        try {
            String resolved = ResolveRelative.resolveRelativeUri(session, client.getRootUrl(), url);
            String host = new URL(resolved).getHost();
            if (host != null) {
                if (!hosts.contains(host)) {
                    hosts.add(host);
                }
            }
        } catch (MalformedURLException e) {
            logger.debugf("Could not extract host from URL: %s", url);
        }
    }

    private static List<String> extractHostsFromClientManagedHosts(ClientModel client, String hostname) {
        List<String> allowedHosts = new ArrayList<>();
        if (hostname == null || hostname.trim().isEmpty()) {
            return allowedHosts;
        }
        Optional.ofNullable(client.getRegisteredNodes())
                .ifPresent(nodes -> nodes.forEach((host, timestamp) -> {
                    allowedHosts.add(host);
                }));
        return allowedHosts;
    }
}
