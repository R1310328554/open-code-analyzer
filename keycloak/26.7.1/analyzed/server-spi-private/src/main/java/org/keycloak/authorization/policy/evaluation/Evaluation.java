/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization.policy.evaluation;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.Decision.Effect;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.provider.PolicyProvider;

/**
 * 策略评估上下文：供 {@link PolicyProvider} 针对单个 {@link ResourcePermission} 执行 grant/deny 决策。
 * <p>暴露权限、运行时上下文、当前策略及领域查询接口。</p>
 *
 * <p>An {@link Evaluation} is mainly used by {@link PolicyProvider} in order to evaluate a single
 * and specific {@link ResourcePermission} against the configured policies.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface Evaluation {

    /**
     * 返回待评估的 {@link ResourcePermission}。
     *
     * Returns the {@link ResourcePermission} to be evaluated.
     *
     * @return the permission to be evaluated
     */
    ResourcePermission getPermission();

    /**
     * 返回评估运行时上下文。
     *
     * Returns the {@link EvaluationContext}. Which provides access to the whole evaluation runtime context.
     *
     * @return the evaluation context
     */
    EvaluationContext getContext();

    /**
     * 返回当前正在评估的 {@link Policy}。
     *
     * Returns the {@link Policy}. being evaluated.
     *
     * @return the evaluation context
     */
    Policy getPolicy();

    /**
     * 返回供策略查询用户/组/角色信息的 {@link Realm} 视图。
     *
     * Returns a {@link Realm} that can be used by policies to query information.
     *
     * @return a {@link Realm} instance
     */
    Realm getRealm();

    AuthorizationProvider getAuthorizationProvider();

    /**
     * 授予所请求的权限。
     *
     * Grants the requested permission to the caller.
     */
    void grant();

    /**
     * 拒绝所请求的权限。
     *
     * Denies the requested permission.
     */
    void deny();

    /**
     * 若尚未做出决策则拒绝。
     *
     * Denies the requested permission if a decision was not made yet.
     */
    void denyIfNoEffect();

    /**
     * 返回当前策略的父策略（通常为权限策略）。
     *
     * Returns the parent policy (a permission) of the policy being evaluated.
     *
     * @return the parent policy
     */
    Policy getParentPolicy();

    Effect getEffect();

    void setEffect(Effect effect);

    /**
     * 判断 {@code grantedPolicy} 授予访问时是否包含给定作用域。
     *
     * If the given scope should be granted when the given {@code grantedPolicy} is granting access to a resource or a specific scope.
     *
     * @param grantedPolicy the policy granting access
     * @param grantedScope the scope that should be granted
     * @return {@code true} if the scope is granted. Otherwise, returns {@code false}
     */
    default boolean isGranted(Policy grantedPolicy, Scope grantedScope) {
        return grantedPolicy.getScopes().contains(grantedScope);
    }

    /**
     * 判断 {@code deniedPolicy} 是否拒绝给定作用域（默认不拒绝，子类可覆盖）。
     *
     * If the given scope should not be granted when the given {@code deniedPolicy} is associated with a resource group.
     *
     * @param deniedPolicy the policy granting access
     * @param deniedScope the scope that should be granted
     * @return {@code true} if the scope is granted. Otherwise, returns {@code false}
     */
    default boolean isDenied(Policy deniedPolicy, Scope deniedScope) {
        return false;
    }
}
