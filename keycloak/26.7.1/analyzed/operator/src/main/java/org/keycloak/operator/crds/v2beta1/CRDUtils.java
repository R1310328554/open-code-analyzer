/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.operator.crds.v2beta1;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.keycloak.operator.Constants;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.FeatureSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.HttpSpec;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetSpec;
import io.javaoperatorsdk.operator.api.reconciler.Context;

/**
 * Keycloak v2beta1 CRD 相关的只读工具方法集合。
 *
 * <p>封装 TLS、JGroups、管理端点、配置项合并及 StatefulSet 注解解析等常用判断逻辑。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public final class CRDUtils {
    /** 健康检查开关对应的配置键名。 */
    private static final String HEALTH_ENABLED = "health-enabled";
    /** HTTP 管理接口健康检查开关对应的配置键名。 */
    public static final String HTTP_MANAGEMENT_HEALTH_ENABLED = "http-management-health-enabled";
    /** 指标（Metrics）开关对应的配置键名。 */
    public static final String METRICS_ENABLED = "metrics-enabled";
    /** 是否启用旧版可观测性管理接口的配置键名。 */
    public static final String LEGACY_MANAGEMENT_ENABLED = "legacy-observability-interface";

    /** 判断 Keycloak CR 是否配置了有效的 HTTPS TLS Secret。 */
    public static boolean isTlsConfigured(Keycloak keycloakCR) {
        var tlsSecret = keycloakSpecOf(keycloakCR).map(KeycloakSpec::getHttpSpec).map(HttpSpec::getTlsSecret);
        return tlsSecret.isPresent() && !tlsSecret.get().trim().isEmpty();
    }

    /** 判断是否应启用 JGroups 集群通信（multi-site 或 clusterless 特性启用时关闭）。 */
    public static boolean isJGroupEnabled(Keycloak keycloak) {
        // 若启用了 multi-site 或 clusterless 特性，则不启用 JGroups
        return CRDUtils.keycloakSpecOf(keycloak)
                .map(KeycloakSpec::getFeatureSpec)
                .map(FeatureSpec::getEnabledFeatures)
                .filter(features -> features.contains("multi-site") || features.contains("clusterless"))
                .isEmpty();
    }

    /** 判断管理端点（指标/健康）是否应对外暴露。 */
    public static boolean isManagementEndpointEnabled(Keycloak keycloak) {
        var options = configuredOptions(keycloak);
        // 启用旧版管理接口时不单独暴露新管理端点
        if (Boolean.parseBoolean(options.get(LEGACY_MANAGEMENT_ENABLED))) {
            return false;
        }

        return Boolean.parseBoolean(options.get(METRICS_ENABLED)) || (Boolean.parseBoolean(options.get(HEALTH_ENABLED))
                && Boolean.parseBoolean(options.getOrDefault(HTTP_MANAGEMENT_HEALTH_ENABLED, Boolean.toString(true))));
    }

    /** 合并默认分发配置与 CR 中 {@code additionalOptions}，得到完整 Keycloak 配置项映射。 */
    public static Map<String, String> configuredOptions(Keycloak keycloak) {
        Map<String, String> options = new HashMap<>();
        // 先写入默认配置项
        Constants.DEFAULT_DIST_CONFIG_LIST
              .forEach(valueOrSecret -> options.put(valueOrSecret.getName(), valueOrSecret.getValue()));
        // 再用 CR 中显式配置的项覆盖
        keycloakSpecOf(keycloak)
              .map(KeycloakSpec::getAdditionalOptions)
              .stream()
              .flatMap(Collection::stream)
              .forEach(valueOrSecret -> options.put(valueOrSecret.getName(), valueOrSecret.getValue()));
        return options;
    }

    /** 安全提取 {@link Keycloak} CR 的 {@link KeycloakSpec}。 */
    public static Optional<KeycloakSpec> keycloakSpecOf(Keycloak keycloak) {
        return Optional.ofNullable(keycloak)
                .map(Keycloak::getSpec);
    }

    /** 从 StatefulSet 模板中取第一个容器定义。 */
    public static Optional<Container> firstContainerOf(StatefulSet statefulSet) {
        return Optional.ofNullable(statefulSet)
                .map(StatefulSet::getSpec)
                .map(StatefulSetSpec::getTemplate)
                .map(PodTemplateSpec::getSpec)
                .map(PodSpec::getContainers)
                .filter(Predicate.not(List::isEmpty))
                .map(containers -> containers.get(0));
    }

    /** 将 Java 对象序列化为 Jackson {@link JsonNode}，供 CR 补丁或比较使用。 */
    public static <T> JsonNode toJsonNode(T value, Context<Keycloak> context) {
        final var kubernetesSerialization = context.getClient().getKubernetesSerialization();
        return kubernetesSerialization.convertValue(value, JsonNode.class);
    }

    /** 读取 StatefulSet 注解，判断当前更新是否为 Recreate 策略。 */
    public static Optional<Boolean> fetchIsRecreateUpdate(StatefulSet statefulSet) {
        var value = statefulSet.getMetadata().getAnnotations().get(Constants.KEYCLOAK_RECREATE_UPDATE_ANNOTATION);
        return Optional.ofNullable(value).map(Boolean::parseBoolean);
    }

    /** 读取 Operator 写入的更新原因注解。 */
    public static Optional<String> findUpdateReason(StatefulSet statefulSet) {
        return Optional.ofNullable(statefulSet.getMetadata().getAnnotations().get(Constants.KEYCLOAK_UPDATE_REASON_ANNOTATION));
    }

    /** 读取 StatefulSet 上记录的 Keycloak 更新修订版本号。 */
    public static Optional<String> getRevision(StatefulSet statefulSet) {
        return Optional.ofNullable(statefulSet)
                .map(StatefulSet::getMetadata)
                .map(ObjectMeta::getAnnotations)
                .map(annotations -> annotations.get(Constants.KEYCLOAK_UPDATE_REVISION_ANNOTATION));
    }

    /** 读取 StatefulSet 上记录的 spec 内容哈希，用于检测是否需要滚动更新。 */
    public static Optional<String> getUpdateHash(StatefulSet statefulSet) {
        return Optional.ofNullable(statefulSet)
                .map(StatefulSet::getMetadata)
                .map(ObjectMeta::getAnnotations)
                .map(annotations -> annotations.get(Constants.KEYCLOAK_UPDATE_HASH_ANNOTATION));
    }
}
