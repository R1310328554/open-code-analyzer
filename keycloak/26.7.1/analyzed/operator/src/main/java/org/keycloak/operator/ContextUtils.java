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

package org.keycloak.operator;

import java.util.Optional;

import org.keycloak.operator.controllers.KeycloakDistConfigurator;
import org.keycloak.operator.controllers.WatchedResources;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.realmimport.KeycloakRealmImport;
import org.keycloak.operator.update.UpdateType;

import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.javaoperatorsdk.operator.api.reconciler.Context;

/**
 * Operator 协调上下文工具类，在 {@link Context} 的工作流上下文中存取共享对象。
 *
 * <p>用于在同一协调周期内的 Dependent Resource 之间传递 StatefulSet 快照、更新类型、配置等信息。
 */
public final class ContextUtils {

    // 上下文键名常量
    /** Keycloak CR 实例键。 */
    public static final String KEYCLOAK = "keycloak";
    /** 当前（旧）StatefulSet 键。 */
    public static final String OLD_DEPLOYMENT_KEY = "current_stateful_set";
    /** 期望（新）StatefulSet 键。 */
    public static final String NEW_DEPLOYMENT_KEY = "desired_new_stateful_set";
    /** 更新类型键。 */
    public static final String UPDATE_TYPE_KEY = "update_type";
    /** 更新原因键。 */
    public static final String UPDATE_REASON_KEY = "update_reason";
    /** Operator 配置键。 */
    public static final String OPERATOR_CONFIG_KEY = "operator_config";
    /** 已监视资源集合键。 */
    public static final String WATCHED_RESOURCES_KEY = "watched_resources";
    /** 发行版配置器键。 */
    public static final String DIST_CONFIGURATOR_KEY = "dist_configurator";

    private ContextUtils() {}

    /** 存储当前 StatefulSet 快照。 */
    public static void storeCurrentStatefulSet(Context<?> context, StatefulSet statefulSet) {
        context.managedWorkflowAndDependentResourceContext().put(OLD_DEPLOYMENT_KEY, statefulSet);
    }

    /** 获取当前 StatefulSet 快照。 */
    public static Optional<StatefulSet> getCurrentStatefulSet(Context<?> context) {
        return context.managedWorkflowAndDependentResourceContext().get(OLD_DEPLOYMENT_KEY, StatefulSet.class);
    }

    /** 存储期望的 StatefulSet 定义。 */
    public static void storeDesiredStatefulSet(Context<?> context, StatefulSet statefulSet) {
        context.managedWorkflowAndDependentResourceContext().put(NEW_DEPLOYMENT_KEY, statefulSet);
    }

    /** 获取期望的 StatefulSet 定义（必须存在）。 */
    public static StatefulSet getDesiredStatefulSet(Context<?> context) {
        return context.managedWorkflowAndDependentResourceContext().getMandatory(NEW_DEPLOYMENT_KEY, StatefulSet.class);
    }

    /** 存储更新类型与原因。 */
    public static void storeUpdateType(Context<?> context, UpdateType updateType, String reason) {
        context.managedWorkflowAndDependentResourceContext().put(UPDATE_TYPE_KEY, updateType);
        context.managedWorkflowAndDependentResourceContext().put(UPDATE_REASON_KEY, reason);
    }

    /** 获取更新类型。 */
    public static Optional<UpdateType> getUpdateType(Context<?> context) {
        return context.managedWorkflowAndDependentResourceContext().get(UPDATE_TYPE_KEY, UpdateType.class);
    }

    /** 获取更新原因（必须存在）。 */
    public static String getUpdateReason(Context<?> context) {
        return context.managedWorkflowAndDependentResourceContext().getMandatory(UPDATE_REASON_KEY, String.class);
    }

    /** 存储 Operator 运行时配置。 */
    public static void storeOperatorConfig(Context<?> context, Config operatorConfig) {
        context.managedWorkflowAndDependentResourceContext().put(OPERATOR_CONFIG_KEY, operatorConfig);
    }

    /** 获取 Operator 运行时配置（必须存在）。 */
    public static Config getOperatorConfig(Context<?> context) {
        return context.managedWorkflowAndDependentResourceContext().getMandatory(OPERATOR_CONFIG_KEY, Config.class);
    }

    /** 存储已监视的 Kubernetes 资源集合。 */
    public static void storeWatchedResources(Context<?> context, WatchedResources watchedResources) {
        context.managedWorkflowAndDependentResourceContext().put(WATCHED_RESOURCES_KEY, watchedResources);
    }

    /** 获取已监视的 Kubernetes 资源集合（必须存在）。 */
    public static WatchedResources getWatchedResources(Context<?> context) {
        return context.managedWorkflowAndDependentResourceContext().getMandatory(WATCHED_RESOURCES_KEY, WatchedResources.class);
    }

    /** 存储 Keycloak 发行版配置器。 */
    public static void storeDistConfigurator(Context<?> context, KeycloakDistConfigurator distConfigurator) {
        context.managedWorkflowAndDependentResourceContext().put(DIST_CONFIGURATOR_KEY, distConfigurator);
    }

    /** 获取 Keycloak 发行版配置器（必须存在）。 */
    public static KeycloakDistConfigurator getDistConfigurator(Context<?> context) {
        return context.managedWorkflowAndDependentResourceContext().getMandatory(DIST_CONFIGURATOR_KEY, KeycloakDistConfigurator.class);
    }

    /** 存储 Keycloak CR 实例（Realm Import 协调流程使用）。 */
    public static void storeKeycloak(Context<KeycloakRealmImport> context, Keycloak existingKeycloak) {
        context.managedWorkflowAndDependentResourceContext().put(KEYCLOAK, existingKeycloak);
    }

    /** 获取 Keycloak CR 实例（必须存在）。 */
    public static Keycloak getKeycloak(Context<?> context) {
        return context.managedWorkflowAndDependentResourceContext().getMandatory(KEYCLOAK, Keycloak.class);
    }
}
