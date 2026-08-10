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

package org.keycloak.common.constants;

/**
 * Keycloak OpenAPI 注解与 Admin REST API 文档相关常量。
 *
 * <p>集中定义 SmallRye OpenAPI 配置文件名与各 API 分组标签，供服务端与代码生成器引用。</p>
 */
public class KeycloakOpenAPI {

    protected KeycloakOpenAPI() { }

    /** OpenAPI 扩展配置文件常量。 */
    public static class Profiles {

        /** Admin API 专用 SmallRye profile 名称。 */
        public static final String ADMIN = "x-smallrye-profile-admin";

        private Profiles() { }
    }

    /** Admin REST API 相关 OpenAPI 标签。 */
    public static class Admin {

        private Admin() { }

        /** 按资源域划分的 API 操作标签名。 */
        public static class Tags {
            public static final String ATTACK_DETECTION = "Attack Detection";
            public static final String AUTHENTICATION_MANAGEMENT = "Authentication Management";
            public static final String CLIENTS = "Clients";
            public static final String CLIENTS_V2 = "Clients (v2)";
            public static final String CLIENT_ATTRIBUTE_CERTIFICATE = "Client Attribute Certificate";
            public static final String CLIENT_INITIAL_ACCESS = "Client Initial Access";
            public static final String CLIENT_REGISTRATION_POLICY = "Client Registration Policy";
            public static final String CLIENT_ROLE_MAPPINGS = "Client Role Mappings";
            public static final String CLIENT_SCOPES = "Client Scopes";
            public static final String COMPONENT = "Component";
            public static final String GROUPS = "Groups";
            public static final String IDENTITY_PROVIDERS = "Identity Providers";
            public static final String KEY = "Key";
            public static final String PROTOCOL_MAPPERS = "Protocol Mappers";
            public static final String REALMS_ADMIN = "Realms Admin";
            public static final String ROLES = "Roles";
            public static final String ROLES_BY_ID = "Roles (by ID)";
            public static final String ROLE_MAPPER = "Role Mapper";
            public static final String ROOT = "Root";
            public static final String SCOPE_MAPPINGS = "Scope Mappings";
            public static final String USERS = "Users";
            public static final String ORGANIZATIONS = "Organizations";
            public static final String WORKFLOWS = "Workflows";
            public static final String SSF = "SSF";
            private Tags() { }
        }

    }

    /** Shared Signals Framework (SSF) API 相关 OpenAPI 标签。 */
    public static class Ssf {

        private Ssf() { }

        public static class Tags {
            /** SSF Transmitter 端点分组标签。 */
            public static final String TRANSMITTER = "SSF Transmitter";
            private Tags() { }
        }
    }
}
