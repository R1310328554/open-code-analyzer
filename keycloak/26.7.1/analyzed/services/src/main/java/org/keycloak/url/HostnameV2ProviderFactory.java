/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.url;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.common.util.Environment;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.urls.HostnameProvider;
import org.keycloak.urls.HostnameProviderFactory;

import org.jboss.logging.Logger;

/**
 * Hostname V2 {@link HostnameProviderFactory}，解析 {@code hostname}、{@code hostname-admin} 与 backchannel 动态选项。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class HostnameV2ProviderFactory implements HostnameProviderFactory, EnvironmentDependentProviderFactory {

    private static final Logger LOGGER = Logger.getLogger(HostnameV2ProviderFactory.class);

    private static final String INVALID_HOSTNAME = "Provided hostname is neither a plain hostname nor a valid URL";
    private String hostname;
    private URI hostnameUrl;
    private URI adminUrl;
    private Boolean backchannelDynamic;

    /** 解析 hostname 相关配置并校验 strict / backchannel 约束。 */
    @Override
    public void init(Config.Scope config) {
        if (Environment.isNonServerMode()) {
            return;
        }
        // strict 模式仅用于强制要求配置 hostname
        boolean strictMode = config.getBoolean("hostname-strict", false);

        String hostnameRaw = config.get("hostname");
        if (strictMode && hostnameRaw == null) {
            throw new IllegalArgumentException("hostname is not configured; either configure hostname, or set hostname-strict to false");
        } else if (hostnameRaw != null && !strictMode) {
            // 为保持一致性：hostname 与 strict 的语义在此场景下等效
//            throw new IllegalArgumentException("hostname is configured, hostname-strict must be set to true");
            LOGGER.info("If hostname is specified, hostname-strict is effectively ignored");
        }

        // hostname 可为完整 URL 或纯主机名
        if (hostnameRaw != null) {
            if (!(hostnameRaw.startsWith("http://") || hostnameRaw.startsWith("https://"))) {
                validateAndSetHostname(hostnameRaw);
            } else {
                hostnameUrl = validateAndCreateUri(hostnameRaw, INVALID_HOSTNAME);
            }
        }

        Optional.ofNullable(config.get("hostname-admin")).ifPresent(h ->
                adminUrl = validateAndCreateUri(h, "Provided hostname-admin is not a valid URL"));

        if (adminUrl != null && hostnameUrl == null) {
            throw new IllegalArgumentException("hostname must be set to a URL when hostname-admin is set");
        }

        // 动态 backchannel 要求 hostname 为完整 URL，否则后端请求片段可能混入前端 URL
        backchannelDynamic = config.getBoolean("hostname-backchannel-dynamic", false);
        if (hostname == null && hostnameUrl == null && backchannelDynamic) {
            throw new IllegalArgumentException("hostname-backchannel-dynamic must be set to false when no hostname is provided");
        }
        if (backchannelDynamic && hostnameUrl == null) {
            throw new IllegalArgumentException("hostname-backchannel-dynamic must be set to false if hostname is not provided as full URL");
        }
    }

    /** 校验纯主机名字符串并保存。 */
    private void validateAndSetHostname(String hostname) {
        URI result;
        try {
            result = URI.create("http://"+hostname);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_HOSTNAME, e);
        }
        if (result.getHost() == null || !result.getHost().equals(hostname)) {
            throw new IllegalArgumentException(INVALID_HOSTNAME);
        }
        this.hostname = hostname;
    }

    /** 校验 URL 格式（scheme、无 userInfo/query/fragment）并规范化末尾斜杠。 */
    private URI validateAndCreateUri(String uri, String validationFailedMessage) {
        URI result;
        try {
            result = URI.create(uri.endsWith("/") ? uri : uri + "/");
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(validationFailedMessage, e);
        }
        if (!Arrays.asList("http", "https").contains(result.getScheme())) {
            throw new IllegalArgumentException(validationFailedMessage);
        }
        if (result.getRawUserInfo() != null || result.getRawQuery() != null || result.getRawFragment() != null) {
            throw new IllegalArgumentException(validationFailedMessage);
        }
        return result;
    }

    @Override
    public HostnameProvider create(KeycloakSession session) {
        return new HostnameV2Provider(session, hostname, hostnameUrl, adminUrl, backchannelDynamic);
    }

    @Override
    public String getId() {
        return "v2";
    }

    /** 仅在 {@link Profile.Feature#HOSTNAME_V2} 特性启用时可用。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.HOSTNAME_V2);
    }
}
