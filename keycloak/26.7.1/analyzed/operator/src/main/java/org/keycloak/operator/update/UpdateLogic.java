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

import java.util.Optional;

import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakStatusAggregator;

import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

/**
 * Keycloak CR 更新决策 API，在创建或更新 {@link StatefulSet} 之前介入协调流程。
 *
 * <p>实现类可中断协调以执行兼容性检查、更新 Job 等前置任务，
 * 并通过 {@link org.keycloak.operator.ContextUtils#storeUpdateType(Context, UpdateType, String)}
 * 记录滚动或重建更新类型。
 */
public interface UpdateLogic {

    /**
     * 检查现有 {@link StatefulSet} 是否存在，并决定以何种 {@link UpdateType} 更新。
     *
     * <p>应通过 {@link org.keycloak.operator.ContextUtils#storeUpdateType(Context, UpdateType, String)}
     * 持久化决策；若尚无 {@link StatefulSet}，则无需决策且不得调用该方法。
     *
     * <p>返回非空 {@link Optional} 时中断协调，阻止 {@link StatefulSet} 被更新，直至下次事件。
     *
     * @return 需要中断协调时返回 {@link UpdateControl}，否则返回空 {@link Optional}
     */
    Optional<UpdateControl<Keycloak>> decideUpdate();

    /**
     * 将更新类型与原因写入 Keycloak CR 状态。
     *
     * @param statusAggregator 用于聚合 status 条件的 {@link KeycloakStatusAggregator}
     */
    void updateStatus(KeycloakStatusAggregator statusAggregator);

}
