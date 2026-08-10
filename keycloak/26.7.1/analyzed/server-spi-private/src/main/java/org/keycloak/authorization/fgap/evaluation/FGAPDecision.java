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

package org.keycloak.authorization.fgap.evaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.keycloak.authorization.Decision;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.Evaluation;

/**
 * FGAP 决策装饰器：将底层 {@link Decision} 包装为使用 {@link FGAPEvaluation} 的回调链，跟踪各作用域下已授权的资源集合。
 */
class FGAPDecision implements Decision<Evaluation> {

    private final Decision<Evaluation> decision;
    private final Map<Scope, Set<Resource>> scopesGrantedByResource = new HashMap<>();

    /** 委托给底层决策回调。 */
    FGAPDecision(Decision<Evaluation> decision) {
        this.decision = decision;
    }

    /** 用 {@link FGAPEvaluation} 包装评估结果后转发。 */
    @Override
    public void onDecision(Evaluation evaluation) {
        decision.onDecision(new FGAPEvaluation(evaluation, scopesGrantedByResource));
    }

    @Override
    public void onError(Throwable cause) {
        decision.onError(cause);
    }

    @Override
    public void onComplete() {
        decision.onComplete();
    }

    @Override
    public void onComplete(ResourcePermission permission) {
        decision.onComplete(permission);
    }

    @Override
    public boolean isEvaluated(String scope) {
        return decision.isEvaluated(scope);
    }
}
