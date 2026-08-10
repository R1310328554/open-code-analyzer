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
package org.keycloak.operator.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.keycloak.operator.Constants;
import org.keycloak.operator.Utils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.spec.HttpManagementSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.HttpSpec;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

import static org.keycloak.operator.crds.v2beta1.CRDUtils.isTlsConfigured;

/**
 * Keycloak 主 Service 的 Dependent Resource，根据 {@link Keycloak} CR 暴露 HTTP/HTTPS 与管理端口。
 *
 * <p>继承 {@link VersionTolerantCRUDKubernetesDependentResource}，在 API 版本演进时仍能正确关联 owner reference。
 */
@KubernetesDependent(
        informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
public class KeycloakServiceDependentResource extends VersionTolerantCRUDKubernetesDependentResource<Service, Keycloak> {

    /** 注册 Service 资源类型。 */
    public KeycloakServiceDependentResource() {
        super(Service.class);
    }

    /**
     * 构建 Service 端口规格：按 TLS 与 HTTP 启用情况暴露对应端口，并始终包含管理端口。
     *
     * @param keycloak 主资源 Keycloak CR
     * @return 期望的 ServiceSpec
     */
    private ServiceSpec getServiceSpec(Keycloak keycloak) {
        var builder = new ServiceSpecBuilder().withSelector(Utils.allInstanceLabels(keycloak));

        boolean tlsConfigured = isTlsConfigured(keycloak);
        boolean httpEnabled = isHttpEnabled(keycloak);
        // 未配置 TLS 或显式启用 HTTP 时暴露 HTTP 端口
        if (!tlsConfigured || httpEnabled) {
            int containerPort = HttpSpec.httpPort(keycloak);
            int servicePort = HttpSpec.serviceHttpPort(keycloak);
            builder.addNewPort()
                    .withPort(servicePort)
                    .withTargetPort(new IntOrString(containerPort))
                    .withName(Constants.KEYCLOAK_HTTP_PORT_NAME)
                    .withProtocol(Constants.KEYCLOAK_SERVICE_PROTOCOL)
                    .endPort();
        }
        // 配置 TLS 时暴露 HTTPS 端口
        if (tlsConfigured) {
            int containerPort = HttpSpec.httpsPort(keycloak);
            int servicePort = HttpSpec.serviceHttpsPort(keycloak);
            builder.addNewPort()
                    .withPort(servicePort)
                    .withTargetPort(new IntOrString(containerPort))
                    .withName(Constants.KEYCLOAK_HTTPS_PORT_NAME)
                    .withProtocol(Constants.KEYCLOAK_SERVICE_PROTOCOL)
                    .endPort();
        }

        // 管理端口（健康检查、指标等）
        builder.addNewPort()
                .withPort(HttpManagementSpec.managementPort(keycloak))
                .withName(Constants.KEYCLOAK_MANAGEMENT_PORT_NAME)
                .withProtocol(Constants.KEYCLOAK_SERVICE_PROTOCOL)
                .endPort();

        return builder.build();
    }

    /**
     * 判断 Keycloak 实例是否启用明文 HTTP。
     *
     * @param keycloak Keycloak CR
     * @return 若 httpSpec.httpEnabled 为 true 则返回 true
     */
    static boolean isHttpEnabled(Keycloak keycloak) {
        Optional<HttpSpec> httpSpec = Optional.ofNullable(keycloak.getSpec().getHttpSpec());
        boolean httpEnabled = httpSpec.map(HttpSpec::getHttpEnabled).orElse(false);
        return httpEnabled;
    }

    /** 构建期望的 Kubernetes Service 资源。 */
    @Override
    protected Service desired(Keycloak primary, Context<Keycloak> context) {

        Map<String,String> labels = Utils.allInstanceLabels(primary);
        var optionalSpec = Optional.ofNullable(primary.getSpec().getHttpSpec());
        optionalSpec.map(HttpSpec::getLabels).ifPresent(labels::putAll);

        Map<String,String> annotations = optionalSpec.map(HttpSpec::getAnnotations).orElse(new HashMap<>());

        Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(getServiceName(primary))
                .withNamespace(primary.getMetadata().getNamespace())
                .addToLabels(labels)
                .addToAnnotations(annotations)
                .endMetadata()
                .withSpec(getServiceSpec(primary))
                .build();
        return service;
    }

    /**
     * 解析 Service 名称：优先使用 httpSpec.serviceName，否则为 CR 名 + 默认后缀。
     *
     * @param keycloak Keycloak CR
     * @return Service 资源名
     */
    public static String getServiceName(Keycloak keycloak) {
        return Optional.ofNullable(keycloak.getSpec())
                .map(spec -> spec.getHttpSpec())
                .map(HttpSpec::getServiceName)
                .orElse(keycloak.getMetadata().getName() + Constants.KEYCLOAK_SERVICE_SUFFIX);
    }
}
