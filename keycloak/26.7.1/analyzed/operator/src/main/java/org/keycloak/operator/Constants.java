/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.operator;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.operator.crds.v2beta1.deployment.ValueOrSecret;

/**
 * Keycloak Operator 全局常量：CRD 元数据、标签、端口、路径与默认配置。
 */
public final class Constants {
    /** CRD API 组名。 */
    public static final String CRDS_GROUP = "k8s.keycloak.org";
    /** CRD 稳定版本。 */
    public static final String CRDS_VERSION = "v2beta1";
    /** CRD Alpha 版本。 */
    public static final String CRDS_VERSION_ALPHA = "v2alpha1";
    /** 自定义资源简称。 */
    public static final String SHORT_NAME = "kc";
    /** 资源名称前缀。 */
    public static final String NAME = "keycloak";
    /** 资源复数名称。 */
    public static final String PLURAL_NAME = "keycloaks";
    /** Kubernetes 实例标签键。 */
    public static final String INSTANCE_LABEL = "app.kubernetes.io/instance";
    /** Kubernetes 托管方标签键。 */
    public static final String MANAGED_BY_LABEL = "app.kubernetes.io/managed-by";
    /** Operator 托管方标签值。 */
    public static final String MANAGED_BY_VALUE = "keycloak-operator";
    /** Kubernetes 组件标签键。 */
    public static final String COMPONENT_LABEL = "app.kubernetes.io/component";
    /** 暂停协调注解键。 */
    public static final String KEYCLOAK_PAUSE_ANNOTATION = "operator.keycloak.org/pause";
    /** 迁移进行中注解键。 */
    public static final String KEYCLOAK_MIGRATING_ANNOTATION = "operator.keycloak.org/migrating";
    /** 重建式更新注解键。 */
    public static final String KEYCLOAK_RECREATE_UPDATE_ANNOTATION = "operator.keycloak.org/recreate-update";
    /** 更新原因注解键。 */
    public static final String KEYCLOAK_UPDATE_REASON_ANNOTATION = "operator.keycloak.org/update-reason";
    /** 更新版本号注解键。 */
    public static final String KEYCLOAK_UPDATE_REVISION_ANNOTATION = "operator.keycloak.org/update-revision";
    /** 更新内容哈希注解键。 */
    public static final String KEYCLOAK_UPDATE_HASH_ANNOTATION = "operator.keycloak.org/update-hash";
    /** 应用标签键。 */
    public static final String APP_LABEL = "app";
    /** 客户端 ID 配置键。 */
    public static final String CLIENT_ID_KEY = "client-id";
    /** 客户端密钥配置键。 */
    public static final String CLIENT_SECRET_KEY = "client-secret";

    /** 默认标签字符串（逗号分隔 key=value）。 */
    public static final String DEFAULT_LABELS_AS_STRING = "app=keycloak,app.kubernetes.io/managed-by=keycloak-operator";

    /** HTTP Authorization 请求头名。 */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** 默认标签 Map（不可变）。 */
    public static final Map<String, String> DEFAULT_LABELS = Collections
            .unmodifiableMap(Stream.of(DEFAULT_LABELS_AS_STRING.split(",")).map(s -> s.split("="))
                    .collect(Collectors.toMap(e -> e[0], e -> e[1], (u1, u2) -> u1, TreeMap::new)));

    /** 默认发行版配置项列表（健康检查与缓存）。 */
    public static final List<ValueOrSecret> DEFAULT_DIST_CONFIG_LIST = List.of(
            new ValueOrSecret("health-enabled", "true"),
            new ValueOrSecret("cache", "ispn")
    );


    /** Keycloak HTTP 端口。 */
    public static final Integer KEYCLOAK_HTTP_PORT = 8080;
    /** Keycloak HTTPS 端口。 */
    public static final Integer KEYCLOAK_HTTPS_PORT = 8443;
    /** HTTP 端口名称。 */
    public static final String KEYCLOAK_HTTP_PORT_NAME = "http";
    /** HTTPS 端口名称。 */
    public static final String KEYCLOAK_HTTPS_PORT_NAME = "https";
    /** Service 协议。 */
    public static final String KEYCLOAK_SERVICE_PROTOCOL = "TCP";
    /** Service 资源名后缀。 */
    public static final String KEYCLOAK_SERVICE_SUFFIX = "-service";
    /** JGroups 发现 Service 端口。 */
    public static final Integer KEYCLOAK_DISCOVERY_SERVICE_PORT = 7800;
    /** JGroups 发现 TCP 端口名称。 */
    public static final String KEYCLOAK_DISCOVERY_TCP_PORT_NAME = "tcp";
    /** 发现 Service 资源名后缀。 */
    public static final String KEYCLOAK_DISCOVERY_SERVICE_SUFFIX = "-discovery";
    /** JGroups 数据通信端口。 */
    public static final Integer KEYCLOAK_JGROUPS_DATA_PORT = 7800;
    /** JGroups 故障检测端口。 */
    public static final Integer KEYCLOAK_JGROUPS_FD_PORT = 57800;
    /** JGroups 协议。 */
    public static final String KEYCLOAK_JGROUPS_PROTOCOL = "TCP";
    /** 管理接口端口。 */
    public static final Integer KEYCLOAK_MANAGEMENT_PORT = 9000;
    /** 管理端口名称。 */
    public static final String KEYCLOAK_MANAGEMENT_PORT_NAME = "management";

    /** Ingress 资源名后缀。 */
    public static final String KEYCLOAK_INGRESS_SUFFIX = "-ingress";

    /** 禁用不安全选项的特殊值。 */
    public static final String INSECURE_DISABLE = "INSECURE-DISABLE";
    /** 证书挂载目录。 */
    public static final String CERTIFICATES_FOLDER = "/mnt/certificates";

    /** Keycloak 配置目录。 */
    public static final String CONFIG_FOLDER = "/opt/keycloak/conf";
    /** 信任库目录。 */
    public static final String TRUSTSTORES_FOLDER = CONFIG_FOLDER + "/truststores";
    /** 缓存配置子目录名。 */
    public static final String CACHE_CONFIG_SUBFOLDER = "cache";
    /** 缓存配置完整目录路径。 */
    public static final String CACHE_CONFIG_FOLDER = CONFIG_FOLDER + "/" + CACHE_CONFIG_SUBFOLDER;

    /** HTTP 相对路径配置键。 */
    public static final String KEYCLOAK_HTTP_RELATIVE_PATH_KEY = "http-relative-path";
    /** 管理 HTTP 相对路径配置键。 */
    public static final String KEYCLOAK_HTTP_MANAGEMENT_RELATIVE_PATH_KEY = "http-management-relative-path";

    /** NetworkPolicy 资源名后缀。 */
    public static final String KEYCLOAK_NETWORK_POLICY_SUFFIX = "-network-policy";

    /** 重试等待时长。 */
    public static final Duration RETRY_DURATION = Duration.ofSeconds(10);
}
