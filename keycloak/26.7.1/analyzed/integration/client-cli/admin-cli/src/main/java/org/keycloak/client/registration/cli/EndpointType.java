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

package org.keycloak.client.registration.cli;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.keycloak.client.cli.util.HttpUtil;

/**
 * 客户端动态注册 API 端点类型枚举。
 * <p>
 * 每种类型对应 {@code /clients-registrations/{endpoint}} 路径段及 CLI {@code -e} 选项别名。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public enum EndpointType {
    /** Keycloak 默认 {@link ClientRepresentation} JSON 格式。 */
    DEFAULT("default", "default"),
    /** OpenID Connect 动态客户端注册格式。 */
    OIDC("openid-connect", "oidc", "oidc"),
    /** 适配器安装配置端点。 */
    INSTALL("install", "install", "adapter"),
    /** SAML 2.0 SP 元数据（XML）格式。 */
    SAML2("saml2-entity-descriptor", "saml2", "saml2");

    /** REST 路径中的端点段名。 */
    private String endpoint;
    /** CLI 首选显示名。 */
    private String preferredName;
    /** 可接受的别名集合。 */
    private Set<String> alternativeNames;

    private EndpointType(String endpoint, String preferredName, String ... alternativeNames) {
        this.endpoint = endpoint;
        this.preferredName = preferredName;
        this.alternativeNames = new HashSet(Arrays.asList(alternativeNames));
    }

    /**
     * 按名称或别名解析端点类型。
     *
     * @param name 端点名或别名（如 {@code oidc}、{@code saml2}）
     * @return 匹配的 {@link EndpointType}
     * @throws IllegalArgumentException 不支持的端点名
     */
    public static EndpointType of(String name) {
        if (DEFAULT.endpoint.equals(name) || DEFAULT.alternativeNames.contains(name)) {
            return DEFAULT;
        } else if (OIDC.endpoint.equals(name) || OIDC.alternativeNames.contains(name)) {
            return OIDC;
        } else if (INSTALL.endpoint.equals(name) || INSTALL.alternativeNames.contains(name)) {
            return INSTALL;
        } else if (SAML2.endpoint.equals(name) || SAML2.alternativeNames.contains(name)) {
            return SAML2;
        }
        throw new IllegalArgumentException("Endpoint not supported: " + name);
    }

    /** 返回 REST API 路径段名。 */
    public String getEndpoint() {
        return endpoint;
    }

    /** 返回 CLI 首选显示名。 */
    public String getName() {
        return preferredName;
    }

    /**
     * 返回该端点类型期望的 HTTP Content-Type。
     *
     * @param type 端点类型
     * @return {@link HttpUtil#APPLICATION_JSON} 或 {@link HttpUtil#APPLICATION_XML}
     */
    public static String getExpectedContentType(EndpointType type) {
        switch (type) {
            case DEFAULT:
            case OIDC:
                return HttpUtil.APPLICATION_JSON;
            case SAML2:
                return HttpUtil.APPLICATION_XML;
            default:
                throw new RuntimeException("Unsupported endpoint type: " + type);
        }
    }
}
