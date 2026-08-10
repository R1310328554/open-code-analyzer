package org.keycloak.authorization.permission.evaluator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.Decision;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.permission.Permissions;
import org.keycloak.authorization.policy.evaluation.DecisionPermissionCollector;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.evaluation.PolicyEvaluator;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.Permission;

/**
 * 无界权限评估器：通过 {@link Permissions#all} 枚举资源服务器下全部权限并评估。
 * <p>适用于开放授权请求（未指定具体资源/范围）。</p>
 */
public class UnboundedPermissionEvaluator implements PermissionEvaluator {

    private final EvaluationContext executionContext;
    private final AuthorizationProvider authorizationProvider;
    private final PolicyEvaluator policyEvaluator;
    private final ResourceServer resourceServer;
    private final AuthorizationRequest request;

    UnboundedPermissionEvaluator(EvaluationContext executionContext,
            AuthorizationProvider authorizationProvider, ResourceServer resourceServer,
            AuthorizationRequest request) {
        this.executionContext = executionContext;
        this.authorizationProvider = authorizationProvider;
        this.policyEvaluator = authorizationProvider.getPolicyEvaluator(resourceServer);
        this.resourceServer = resourceServer;
        this.request = request;
    }

    /** 只读模式下枚举全部权限并逐条执行策略评估。 */
    @Override
    public Decision evaluate(Decision decision) {
        StoreFactory storeFactory = authorizationProvider.getStoreFactory();

        try {
            Map<Policy, Map<Object, Decision.Effect>> decisionCache = new HashMap<>();

            storeFactory.setReadOnly(true);

            Permissions.all(resourceServer, executionContext.getIdentity(), authorizationProvider, request,
                    permission -> policyEvaluator.evaluate(permission, authorizationProvider, executionContext, decision, decisionCache));

            decision.onComplete();
        } catch (Throwable cause) {
            decision.onError(cause);
        } finally {
            storeFactory.setReadOnly(false);
        }

        return decision;
    }

    /** 评估并返回 {@link Permission} 结果集合。 */
    @Override
    public Collection<Permission> evaluate(ResourceServer resourceServer, AuthorizationRequest request) {
        DecisionPermissionCollector decision = getDecision(resourceServer, request, DecisionPermissionCollector.class);
        return decision.results();
    }

    /** 评估并返回指定类型的决策收集器。 */
    @Override
    public <D extends Decision<?>> D getDecision(ResourceServer resourceServer, AuthorizationRequest request, Class<D> decisionType) {
        DecisionPermissionCollector decision = new DecisionPermissionCollector(authorizationProvider, resourceServer, request);

        evaluate(decision);

        return decisionType.cast(decision);
    }
}
