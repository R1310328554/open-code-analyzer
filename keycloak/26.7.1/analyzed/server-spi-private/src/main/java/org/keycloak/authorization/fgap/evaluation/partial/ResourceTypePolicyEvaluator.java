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

package org.keycloak.authorization.fgap.evaluation.partial;

import java.util.function.Consumer;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.permission.ResourcePermission;

/**
 * 按资源类型的策略评估扩展：在标准评估流程之外补充特定类型（如 Users）的关联策略。
 * <p>由 {@link FGAPPolicyEvaluator} 按 {@link ResourcePermission#getResourceType()} 分发。</p>
 *
 * An interface that should be implemented to provide additional logic when evaluating permissions for a specific resource type.
 */
public interface ResourceTypePolicyEvaluator {

    /**
     * 根据权限的资源类型执行扩展评估，并将匹配策略交给 {@code policyConsumer}。
     *
     * Evaluates the given {@code permission} based on its {@link ResourcePermission#getResourceType()}.
     *
     * @param permission the permission
     * @param authorization the authorization provider
     * @param policyConsumer the policy consumer or evaluator
     */
    void evaluate(ResourcePermission permission, AuthorizationProvider authorization, Consumer<Policy> policyConsumer);

}
