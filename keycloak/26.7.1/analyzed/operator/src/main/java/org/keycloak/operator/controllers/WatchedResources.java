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

package org.keycloak.operator.controllers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.operator.Utils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.javaoperatorsdk.operator.api.reconciler.Context;

/**
 * 跟踪 Keycloak Deployment 所依赖的外部 Secret/ConfigMap 等资源。
 *
 * <p>在 StatefulSet 上写入 watching/missing/watched-hash 注解，
 * 便于协调逻辑检测依赖资源是否存在，并在依赖变更时触发滚动更新。
 */
@ApplicationScoped
public class WatchedResources {

    /**
     * 待监视资源名称集合；值为 true 表示可选（缺失不报错）。
     */
    public static class Watched {
        /** 由名称列表创建 Watched，均视为非可选。 */
        public static Watched of(String... values) {
            Watched result = new Watched();
            Stream.of(values).forEach(v -> result.add(v, null));
            return result;
        }

        /** 资源名 -> 是否可选（merge 时两者均 optional 才为 optional）。 */
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();

        /** 添加监视项；optional 为 true 时缺失不计入 missing。 */
        public void add(String name, Boolean optional) {
            map.merge(name, optional != null && optional, (b1, b2) -> b1 && b2);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Watched other = (Watched) obj;
            return Objects.equals(map, other.map);
        }

    }

    /** Pod 模板注解：已监视资源内容哈希前缀。 */
    public static final String KEYCLOAK_WATCHED_HASH_ANNOTATION_PREFIX = "operator.keycloak.org/watched-";
    /** Deployment 注解：正在监视某类资源前缀。 */
    public static final String KEYCLOAK_WATCHING_ANNOTATION_PREFIX = "operator.keycloak.org/watching-";
    /** Deployment 注解：缺失的必需资源名列表前缀。 */
    public static final String KEYCLOAK_MISSING_ANNOTATION_PREFIX = "operator.keycloak.org/missing-";

    /**
     * 查询监视的资源并在 Deployment 上写入注解。
     *
     * @param watched 待查资源名与可选标志
     * @param type Kubernetes 资源类型
     * @param deployment 正在被协调的 StatefulSet（可变，将更新注解）
     * @param context 协调上下文
     */
    public <T extends HasMetadata> void annotateDeployment(Watched watched, Class<T> type, StatefulSet deployment, Context<Keycloak> context) {
        if (watched.map.isEmpty()) {
            return;
        }

        List<T> current = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        watched.map.entrySet().stream().forEach(e -> {
            var resource = context.getClient().resources(type)
            .inNamespace(deployment.getMetadata().getNamespace()).withName(e.getKey()).get();
            if (resource == null) {
                if (!e.getValue()) {
                    missing.add(e.getKey());
                }
            } else {
                current.add(resource);
            }
        });

        String plural = HasMetadata.getPlural(type);
        if (!missing.isEmpty()) {
            deployment.getMetadata().getAnnotations().put(WatchedResources.KEYCLOAK_MISSING_ANNOTATION_PREFIX + plural, String.join(", ", missing));
        }
        deployment.getMetadata().getAnnotations().put(WatchedResources.KEYCLOAK_WATCHING_ANNOTATION_PREFIX + plural, Boolean.TRUE.toString());
        deployment.getSpec().getTemplate().getMetadata().getAnnotations()
                .put(WatchedResources.KEYCLOAK_WATCHED_HASH_ANNOTATION_PREFIX + HasMetadata.getKind(type).toLowerCase() + "-hash", Utils.hash(current));
    }

    /**
     * 读取 Deployment 上记录的某类资源缺失列表。
     *
     * @param deployment StatefulSet
     * @param type 资源类型
     * @return 缺失资源名（逗号分隔），无则 empty
     */
    public Optional<String> getMissing(StatefulSet deployment, Class<?> type) {
        String plural = HasMetadata.getPlural(type);
        return Optional.ofNullable(deployment.getMetadata().getAnnotations().get(WatchedResources.KEYCLOAK_MISSING_ANNOTATION_PREFIX + plural));
    }

    /** 判断 Deployment 是否正在监视任意一类外部资源。 */
    public boolean isWatching(StatefulSet deployment) {
        return deployment.getMetadata().getAnnotations().entrySet().stream()
                .anyMatch(e -> e.getKey().startsWith(WatchedResources.KEYCLOAK_WATCHING_ANNOTATION_PREFIX)
                        && e.getValue() != null && !e.getValue().isEmpty());
    }

}
