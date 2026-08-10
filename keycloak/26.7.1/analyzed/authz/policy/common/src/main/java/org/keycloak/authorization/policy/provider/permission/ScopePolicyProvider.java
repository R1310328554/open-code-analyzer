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
package org.keycloak.authorization.policy.provider.permission;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.authorization.Decision;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.DefaultEvaluation;
import org.keycloak.authorization.policy.evaluation.Evaluation;

import org.jboss.logging.Logger;

/**
 * 基于作用域的权限策略：按 {@link ResourcePermission} 粒度缓存评估结果。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ScopePolicyProvider extends AbstractPermissionProvider {

    private static final Logger logger = Logger.getLogger(ScopePolicyProvider.class);

    /**
     * 优先复用权限级决策缓存；尚无决策时委托父类评估并写入缓存。
     *
     * @param evaluation 当前授权评估上下文
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        logger.debugf("Scope policy %s evaluating using parent class", evaluation.getPolicy().getName());
        DefaultEvaluation defaultEvaluation = DefaultEvaluation.class.cast(evaluation);
        Map<Policy, Map<Object, Decision.Effect>> decisionCache = defaultEvaluation.getDecisionCache();
        Policy policy = defaultEvaluation.getParentPolicy();
        Map<Object, Decision.Effect> decisions = decisionCache.computeIfAbsent(policy, p -> new HashMap<>());
        ResourcePermission permission = evaluation.getPermission();

        Decision.Effect effect = decisions.get(permission);

        if (effect != null) {
            defaultEvaluation.setEffect(effect);
            return;
        }

        Decision.Effect decision = defaultEvaluation.getEffect();

        if (decision == null) {
            super.evaluate(evaluation);

            decisions.put(permission, defaultEvaluation.getEffect());
        }
    }
}
