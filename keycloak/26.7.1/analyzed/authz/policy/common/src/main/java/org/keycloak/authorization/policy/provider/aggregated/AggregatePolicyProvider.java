/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.policy.provider.aggregated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.Decision;
import org.keycloak.authorization.fgap.evaluation.partial.PartialEvaluationPolicyProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.DecisionResultCollector;
import org.keycloak.authorization.policy.evaluation.DefaultEvaluation;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.Result;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.ResourceType;

import org.jboss.logging.Logger;

/**
 * <p>聚合策略提供方：按关联子策略的决策结果与 {@link DecisionStrategy} 综合判定授权。
 *
 * <p>支持运行时评估（{@link Evaluation}）与部分评估（FGAP）两种路径。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AggregatePolicyProvider implements PolicyProvider, PartialEvaluationPolicyProvider {
    private static final Logger logger = Logger.getLogger(AggregatePolicyProvider.class);

    /** 依次评估关联策略，按决策策略汇总 grant/deny。 */
    @Override
    public void evaluate(Evaluation evaluation) {
        logger.debugf("Aggregate policy %s evaluating using parent class", evaluation.getPolicy().getName());
        DecisionResultCollector decision = new DecisionResultCollector() {
            @Override
            protected void onComplete(Result result) {
                if (isGranted(result.getResults().iterator().next())) {
                    evaluation.grant();
                } else {
                    evaluation.deny();
                }
            }
        };
        AuthorizationProvider authorization = evaluation.getAuthorizationProvider();
        Policy policy = evaluation.getPolicy();
        DefaultEvaluation defaultEvaluation = DefaultEvaluation.class.cast(evaluation);
        Map<Policy, Map<Object, Decision.Effect>> decisionCache = defaultEvaluation.getDecisionCache();
        ResourcePermission permission = evaluation.getPermission();

        for (Policy associatedPolicy : policy.getAssociatedPolicies()) {
            Map<Object, Decision.Effect> decisions = decisionCache.computeIfAbsent(associatedPolicy, p -> new HashMap<>());
            Decision.Effect effect = decisions.get(permission);
            DefaultEvaluation eval = new DefaultEvaluation(evaluation.getPermission(), evaluation.getContext(), policy, associatedPolicy, decision, authorization, decisionCache);

            if (effect == null) {
                PolicyProvider policyProvider = authorization.getProvider(associatedPolicy.getType());

                policyProvider.evaluate(eval);

                eval.denyIfNoEffect();
                decisions.put(permission, eval.getEffect());
            } else {
                eval.setEffect(effect);
            }
        }

        decision.onComplete(permission);
    }

    /** 聚合策略无额外资源需释放。 */
    @Override
    public void close() {

    }

    /** 仅处理类型为 aggregate 的策略。 */
    @Override
    public boolean supports(Policy policy) {
        return AggregatePolicyProviderFactory.ID.equals(policy.getType());
    }

    /** 查询依赖本聚合策略的管理权限策略（FGAP 部分评估）。 */
    @Override
    public Stream<Policy> getPermissions(KeycloakSession session, ResourceType resourceType, ResourceType groupResourceType, UserModel subject) {
        AuthorizationProvider provider = session.getProvider(AuthorizationProvider.class);
        RealmModel realm = session.getContext().getRealm();
        ClientModel adminPermissionsClient = realm.getAdminPermissionsClient();
        StoreFactory storeFactory = provider.getStoreFactory();
        ResourceServer resourceServer = storeFactory.getResourceServerStore().findByClient(adminPermissionsClient);

        return storeFactory.getPolicyStore().findDependentPolicies(resourceServer, resourceType.getType(), groupResourceType == null ? null : groupResourceType.getType(), AggregatePolicyProviderFactory.ID, null, List.of());
    }

    /** 对部分评估路径：按 AFFIRMATIVE/UNANIMOUS/CONSENSUS 统计子策略 grant 数。 */
    @Override
    public boolean evaluate(KeycloakSession session, Policy policy, UserModel subject) {
        DecisionStrategy decisionStrategy = policy.getDecisionStrategy();
        Set<Policy> associatedPolicies = policy.getAssociatedPolicies();
        int grants = 0;

        for (Policy associatedPolicy : associatedPolicies) {
            PolicyProvider policyProvider = session.getProvider(AuthorizationProvider.class).getProvider(associatedPolicy.getType());

            if (policyProvider instanceof PartialEvaluationPolicyProvider partialPolicyProvider) {
                if (partialPolicyProvider.evaluate(session, associatedPolicy, subject)) {
                    grants++;
                }
            } else {
                return false;
            }
        }

        if (grants == 0) {
            return false;
        }

        return switch (decisionStrategy) {
            case AFFIRMATIVE -> true;
            case UNANIMOUS -> grants == associatedPolicies.size();
            case CONSENSUS -> grants > associatedPolicies.size() - grants;
        };
    }
}
