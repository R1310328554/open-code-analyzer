package org.keycloak.authorization.policy.provider.client;

import java.util.function.BiFunction;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.authorization.ClientPolicyRepresentation;

import org.jboss.logging.Logger;

/**
 * <p>客户端策略提供方：当请求上下文中的客户端 ID 匹配策略配置的客户端列表时授予访问。
 *
 * <p>通过 {@code kc.client.id} 上下文属性与 realm 内 {@link ClientModel} 比对。
 */
public class ClientPolicyProvider implements PolicyProvider {

    private static final Logger logger = Logger.getLogger(ClientPolicyProvider.class);
    private final BiFunction<Policy, AuthorizationProvider, ClientPolicyRepresentation> representationFunction;

    /** 注入策略表示解析函数。 */
    public ClientPolicyProvider(BiFunction<Policy, AuthorizationProvider, ClientPolicyRepresentation> representationFunction) {
        this.representationFunction = representationFunction;
    }

    /** 遍历策略配置的客户端，任一匹配当前请求上下文则 grant。 */
    @Override
    public void evaluate(Evaluation evaluation) {
        ClientPolicyRepresentation representation = representationFunction.apply(evaluation.getPolicy(), evaluation.getAuthorizationProvider());
        AuthorizationProvider authorizationProvider = evaluation.getAuthorizationProvider();
        RealmModel realm = authorizationProvider.getKeycloakSession().getContext().getRealm();
        EvaluationContext context = evaluation.getContext();

        for (String client : representation.getClients()) {
            ClientModel clientModel = realm.getClientById(client);
            if (clientModel != null) {
                if (context.getAttributes().containsValue("kc.client.id", clientModel.getClientId())) {
                    evaluation.grant();
                    logger.debugf("Client policy %s matched with client %s and was granted", evaluation.getPolicy().getName(), clientModel.getClientId());
                    return;
                }
            }
        }
    }

    /** 客户端策略无额外资源需释放。 */
    @Override
    public void close() {

    }
}
