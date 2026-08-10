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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.keycloak.authorization.Decision.Effect;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.permission.ResourcePermission;

/**
 * 单条 {@link ResourcePermission} 的策略评估结果：聚合父策略及其关联子策略的 {@link Effect}。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class Result {

    private final ResourcePermission permission;
    private final Map<String, PolicyResult> results = new LinkedHashMap<>();
    private final Evaluation evaluation;
    private Effect status = Effect.DENY;

    /** 绑定待评估权限与评估上下文。 */
    public Result(ResourcePermission permission, Evaluation evaluation) {
        this.permission = permission;
        this.evaluation = evaluation;
    }

    public ResourcePermission getPermission() {
        return permission;
    }

    public Collection<PolicyResult> getResults() {
        return results.values();
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    /** 获取或创建指定父策略的 {@link PolicyResult}。 */
    public PolicyResult policy(Policy policy) {
        PolicyResult result = results.get(policy.getId());
        
        if (result == null) {
            result = new PolicyResult(policy);
            results.put(policy.getId(), result);
        }

        return result;
    }

    /** 设置顶层决策效果（无父策略时使用）。 */
    public void setStatus(final Effect status) {
        this.status = status;
    }

    public Effect getEffect() {
        return status;
    }

    public PolicyResult getPolicy(Policy policy) {
        return results.get(policy.getId());
    }

    /** 单个策略及其关联子策略的评估结果。 */
    public static class PolicyResult {

        private final Policy policy;
        private final Map<String, PolicyResult> associatedPolicies = new HashMap<>();
        private Effect effect = Effect.DENY;

        public PolicyResult(Policy policy, Effect status) {
            this.policy = policy;
            this.effect = status;
        }

        public PolicyResult(Policy policy) {
            this(policy, Effect.DENY);
        }

        /** 记录关联子策略及其效果。 */
        public PolicyResult policy(Policy policy, Effect effect) {
            PolicyResult policyResult = associatedPolicies.get(policy.getId());

            if (policyResult == null) {
                policyResult = new PolicyResult(policy, effect);
                associatedPolicies.put(policy.getId(), policyResult);
            } else {
                policyResult.setEffect(effect);
            }

            return policyResult;
        }

        public Policy getPolicy() {
            return policy;
        }

        public Collection<PolicyResult> getAssociatedPolicies() {
            return associatedPolicies.values();
        }

        public Effect getEffect() {
            return effect;
        }

        public void setEffect(final Effect status) {
            this.effect = status;
        }
    }
}
