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
package org.keycloak.quarkus.runtime.services.resources;

import java.util.stream.Stream;

import org.keycloak.config.HostnameV2Options;
import org.keycloak.config.HttpOptions;
import org.keycloak.config.ProxyOptions;

/**
 * 主机名调试页面相关常量：反向代理转发头列表与 Hostname V2 / HTTP 相关配置键。
 */
public class ConstantsDebugHostname {
    /** 常见 X-Forwarded-* 代理头名称。 */
    public static final String[] X_FORWARDED_PROXY_HEADERS = new String[] {
            "X-Forwarded-Host",
            "X-Forwarded-Proto",
            "X-Forwarded-Port",
            "X-Forwarded-For",
            "X-Forwarded-Prefix"
    };

    /** RFC 7239 Forwarded 头名称。 */
    public static final String FORWARDED_PROXY_HEADER = "Forwarded";

    /** 调试页展示的全部相关请求头（Host、Forwarded、X-Forwarded-*）。 */
    public static final String[] RELEVANT_HEADERS = Stream
            .concat(Stream.of("Host", FORWARDED_PROXY_HEADER), Stream.of(X_FORWARDED_PROXY_HEADERS))
            .toArray(String[]::new);

    /** Hostname V2 与 HTTP/代理相关的配置项键名。 */
    public static final String[] RELEVANT_OPTIONS_V2 = {
            HostnameV2Options.HOSTNAME.getKey(),
            HostnameV2Options.HOSTNAME_ADMIN.getKey(),
            HostnameV2Options.HOSTNAME_BACKCHANNEL_DYNAMIC.getKey(),
            HostnameV2Options.HOSTNAME_STRICT.getKey(),
            ProxyOptions.PROXY_HEADERS.getKey(),
            HttpOptions.HTTP_ENABLED.getKey(),
            HttpOptions.HTTP_RELATIVE_PATH.getKey(),
            HttpOptions.HTTP_PORT.getKey(),
            HttpOptions.HTTPS_PORT.getKey()
    };

}
