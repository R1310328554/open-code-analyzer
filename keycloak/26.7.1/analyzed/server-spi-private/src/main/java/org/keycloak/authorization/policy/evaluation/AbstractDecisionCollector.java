/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authorization.policy.evaluation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.keycloak.authorization.Decision;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.Result.PolicyResult;
import org.keycloak.representations.idm.authorization.DecisionStrategy;

/**
 * 抽象决策收集器：实现 {@link Decision} 回调，按 {@link ResourcePermission} 聚合策略评估结果。
 * <p>子类在 {@link #onComplete(Result)} 或 {@link #onComplete(Collection)} 中消费汇总后的 {@link Result}。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public abstract class AbstractDecisionCollector implements Decision<Evaluation> {

    protected final Map<ResourcePermission, Result> results = new LinkedHashMap<>();

    /** 接收单次策略评估决策并写入 {@link #results} 映射。 */
    @Override
    public void onDecision(Evaluation evaluation) {
        Policy parentPolicy = evaluation.getParentPolicy();
        ResourcePermission permission = evaluation.getPermission();

        if (parentPolicy != null) {
            if (parentPolicy.equals(evaluation.getPolicy())) {
                results.computeIfAbsent(permission, permission1 -> {
                    for (Result result : results.values()) {
                        Result.PolicyResult policyResult = result.getPolicy(parentPolicy);

                        if (policyResult != null) {
                            Result newResult = new Result(permission1, evaluation);
                            Result.PolicyResult newPolicyResult = newResult.policy(parentPolicy);

                            for (Result.PolicyResult associatePolicy : policyResult.getAssociatedPolicies()) {
                                newPolicyResult.policy(associatePolicy.getPolicy(), associatePolicy.getEffect());
                            }

                            Map<String, Set<String>> claims = result.getPermission().getClaims();

                            if (!claims.isEmpty()) {
                                permission1.addClaims(claims);
                            }

                            return newResult;
                        }
                    }

                    return new Result(permission1, evaluation);
                }).policy(parentPolicy);
            } else {
                results.computeIfAbsent(permission, p -> new Result(p, evaluation)).policy(parentPolicy).policy(evaluation.getPolicy(), evaluation.getEffect());
            }
        } else {
            results.computeIfAbsent(permission, p -> new Result(p, evaluation)).setStatus(evaluation.getEffect());
        }
    }

    /** 全部评估完成时回调所有已收集结果。 */
    @Override
    public void onComplete() {
        onComplete(results.values());
    }

    /** 针对单个 {@link ResourcePermission} 的评估完成时回调。 */
    @Override
    public void onComplete(ResourcePermission permission) {
        Result result = results.get(permission);

        if (result != null) {
            onComplete(result);
        }
    }

    /** 单条结果完成时的钩子，供子类重写。 */
    protected void onComplete(Result result) {

    }

    /** 多条结果批量完成时的钩子，供子类重写。 */
    protected void onComplete(Collection<Result> permissions) {

    }

    /** 根据父策略的 {@link DecisionStrategy}（AFFIRMATIVE/CONSENSUS/UNANIMOUS）判定是否授予。 */
    protected boolean isGranted(Result.PolicyResult policyResult) {
        Policy policy = policyResult.getPolicy();
        DecisionStrategy decisionStrategy = policy.getDecisionStrategy();

        switch (decisionStrategy) {
            case AFFIRMATIVE:
                for (Result.PolicyResult decision : policyResult.getAssociatedPolicies()) {
                    if (Effect.PERMIT.equals(decision.getEffect())) {
                        return true;
                    }
                }
                return false;
            case CONSENSUS:
                int grantCount = 0;
                int denyCount = policy.getAssociatedPolicies().size();

                for (Result.PolicyResult decision : policyResult.getAssociatedPolicies()) {
                    if (decision.getEffect().equals(Effect.PERMIT)) {
                        grantCount++;
                        denyCount--;
                    }
                }

                return grantCount > denyCount;
            default:
                // defaults to UNANIMOUS
                for (Result.PolicyResult decision : policyResult.getAssociatedPolicies()) {
                    if (Effect.DENY.equals(decision.getEffect())) {
                        return false;
                    }
                }
                return true;
        }
    }

    /** 检查给定作用域名称是否已被任一已处理策略关联。 */
    @Override
    public boolean isEvaluated(String scope) {
        for (Result result : results.values()) {
            for (PolicyResult policyResult : result.getResults()) {
                Policy policy = policyResult.getPolicy();

                if (policy.getScopes().stream().anyMatch(s -> s.getName().equals(scope))) {
                    return true;
                }
            }
        }

        return false;
    }
}
