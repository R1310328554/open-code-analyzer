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

package org.keycloak.operator.update;

import org.keycloak.operator.controllers.KeycloakDeploymentDependentResource;

import io.fabric8.kubernetes.api.model.apps.StatefulSet;

/**
 * {@link KeycloakDeploymentDependentResource} 支持的 StatefulSet 更新方式。
 */
public enum UpdateType {
    /**
     * 先关闭现有集群再更新 {@link StatefulSet}（全量重建）。
     */
    RECREATE,
    /**
     * 直接更新 {@link StatefulSet} 并执行滚动发布。
     */
    ROLLING
}
