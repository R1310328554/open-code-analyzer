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

package org.keycloak.operator.crds.v2beta1.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.keycloak.operator.Utils;
import org.keycloak.operator.crds.v2beta1.StatusCondition;

/**
 * 增量构建 {@link KeycloakStatus} 的聚合器，负责合并条件消息与 lastTransitionTime。
 *
 * <p>协调过程中通过 {@link #addNotReadyMessage}、{@link #addErrorMessage} 等方法收集信息，
 * 最终由 {@link #build()} 生成完整 status。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class KeycloakStatusAggregator {
    /** Ready 条件实例。 */
    private final KeycloakStatusCondition readyCondition = new KeycloakStatusCondition();
    /** HasErrors 条件实例。 */
    private final KeycloakStatusCondition hasErrorsCondition = new KeycloakStatusCondition();
    /** RollingUpdate 条件实例。 */
    private final KeycloakStatusCondition rollingUpdate = new KeycloakStatusCondition();
    /** Recreate 更新类型条件实例。 */
    private final KeycloakStatusCondition updateType = new KeycloakStatusCondition();

    /** 未就绪原因消息列表，写入 Ready 条件 message。 */
    private final List<String> notReadyMessages = new ArrayList<>();
    /** 错误或警告消息列表，写入 HasErrors 条件 message。 */
    private final List<String> errorMessages = new ArrayList<>();
    /** 滚动更新进度消息列表。 */
    private final List<String> rollingUpdateMessages = new ArrayList<>();

    /** 底层 status Builder，承载非 condition 字段。 */
    private final KeycloakStatusBuilder statusBuilder;
    /** 上一版 status 中的条件映射，用于保留 lastTransitionTime。 */
    private final Map<String, KeycloakStatusCondition> existingConditions;
    /** 本次协调观察到的 CR generation。 */
    private final Long observedGeneration;

    /**
     * @param generation 写入各条件的 observedGeneration
     */
    public KeycloakStatusAggregator(Long generation) {
        this(null, generation);
    }

    /**
     * @param current 作为基础的当前 status，用于继承未变更的条件时间戳
     * @param generation 写入各条件的 observedGeneration
     */
    public KeycloakStatusAggregator(KeycloakStatus current, Long generation) {
        if (current != null) {
            statusBuilder = new KeycloakStatusBuilder(current);
            existingConditions = getConditionMap(current.getConditions());
        } else {
            statusBuilder = new KeycloakStatusBuilder();
            existingConditions = Map.of();
        }

        observedGeneration = generation;

        readyCondition.setType(KeycloakStatusCondition.READY);

        hasErrorsCondition.setType(KeycloakStatusCondition.HAS_ERRORS);

        rollingUpdate.setType(KeycloakStatusCondition.ROLLING_UPDATE);

        updateType.setType(KeycloakStatusCondition.UPDATE_TYPE);
    }

    /** 将条件列表按 type 索引为 Map。 */
    public static <T extends StatusCondition> Map<String, T> getConditionMap(List<T> conditions) {
        return Optional.ofNullable(conditions).orElse(List.of()).stream().collect(Collectors.toMap(StatusCondition::getType, Function.identity()));
    }

    /** 记录未就绪原因并将 Ready 设为 False。 */
    public KeycloakStatusAggregator addNotReadyMessage(String message) {
        readyCondition.setStatus(false);
        readyCondition.setObservedGeneration(observedGeneration);
        notReadyMessages.add(message);
        return this;
    }

    /** 记录错误并将 HasErrors 设为 True。 */
    public KeycloakStatusAggregator addErrorMessage(String message) {
        hasErrorsCondition.setStatus(true);
        hasErrorsCondition.setObservedGeneration(observedGeneration);
        errorMessages.add(message);
        return this;
    }

    /** 记录警告（前缀 warning:），不强制 HasErrors 为 True。 */
    public KeycloakStatusAggregator addWarningMessage(String message) {
        errorMessages.add("warning: " + message);
        hasErrorsCondition.setObservedGeneration(observedGeneration);
        return this;
    }

    /** 记录滚动更新进行中消息。 */
    public KeycloakStatusAggregator addRollingUpdateMessage(String message) {
        rollingUpdate.setStatus(true);
        rollingUpdate.setObservedGeneration(observedGeneration);
        rollingUpdateMessages.add(message);
        return this;
    }

    /** 设置本次更新是否使用了 Recreate 策略。 */
    public void addUpdateType(boolean recreate, String message) {
        updateType.setStatus(recreate);
        updateType.setObservedGeneration(observedGeneration);
        updateType.setMessage(message);
    }

    /** 清除 Recreate 更新类型条件（设为 Unknown）。 */
    public void resetUpdateType() {
        updateType.setStatus(null);
        updateType.setObservedGeneration(observedGeneration);
        updateType.setMessage(null);
    }

    /**
     * 对 status 的非 condition 字段应用变更。
     */
    public KeycloakStatusAggregator apply(Consumer<KeycloakStatusBuilder> toApply) {
        statusBuilder.withConditions(List.of());
        toApply.accept(statusBuilder);
        if (!statusBuilder.getConditions().isEmpty()) {
            throw new AssertionError("use addXXXMessage methods to modify conditions");
        }
        return this;
    }

    /** 汇总各条件默认值与消息，生成最终 {@link KeycloakStatus}。 */
    public KeycloakStatus build() {
        // 条件仅单向更新——以下逻辑决定何时采用默认/反向状态
        if (readyCondition.getStatus() == null && !Boolean.TRUE.equals(hasErrorsCondition.getStatus())) {
            readyCondition.setStatus(true);
            readyCondition.setObservedGeneration(observedGeneration);
        }
        if (readyCondition.getObservedGeneration() != null) {
            readyCondition.setMessage(String.join("\n", notReadyMessages));
        }

        if (hasErrorsCondition.getStatus() == null && readyCondition.getObservedGeneration() != null) {
            hasErrorsCondition.setStatus(false);
            hasErrorsCondition.setObservedGeneration(observedGeneration);
        }
        if (hasErrorsCondition.getObservedGeneration() != null) {
            hasErrorsCondition.setMessage(String.join("\n", errorMessages));
        }

        if (rollingUpdate.getStatus() == null && readyCondition.getObservedGeneration() != null) {
            rollingUpdate.setStatus(false);
            rollingUpdate.setObservedGeneration(observedGeneration);
        }
        if (rollingUpdate.getObservedGeneration() != null) {
            rollingUpdate.setMessage(String.join("\n", rollingUpdateMessages));
        }

        String now = Utils.iso8601Now();
        updateConditionFromExisting(readyCondition, existingConditions, now);
        updateConditionFromExisting(hasErrorsCondition, existingConditions, now);
        updateConditionFromExisting(rollingUpdate, existingConditions, now);
        updateConditionFromExisting(updateType, existingConditions, now);

        return statusBuilder
                .withObservedGeneration(observedGeneration)
                .withConditions(List.of(readyCondition, hasErrorsCondition, rollingUpdate, updateType))
                .build();
    }

    /**
     * 根据已有条件更新 lastTransitionTime：状态与消息均未变则保留原时间戳。
     *
     * @param condition 待写入的新条件
     * @param existingConditions 上一版条件映射
     * @param now 当前 ISO8601 时间
     */
    public static void updateConditionFromExisting(StatusCondition condition, Map<String, ? extends StatusCondition> existingConditions, String now) {
        var existing = existingConditions.get(condition.getType());
        if (existing == null) {
            if (condition.getObservedGeneration() != null) {
                condition.setLastTransitionTime(now);
            }
        } else if (condition.getObservedGeneration() == null) {
            // 未观察到新 generation 时沿用旧条件
            condition.setLastTransitionTime(existing.getLastTransitionTime());
            condition.setObservedGeneration(existing.getObservedGeneration());
            condition.setStatus(existing.getStatus());
            if (condition.getMessage() == null) {
                condition.setMessage(existing.getMessage());
            }
        } else if (Objects.equals(existing.getStatus(), condition.getStatus())
                && Objects.equals(existing.getMessage(), condition.getMessage())) {
           condition.setLastTransitionTime(existing.getLastTransitionTime());
        } else {
           condition.setLastTransitionTime(now);
        }
    }
}
