package org.keycloak.protocol.oid4vc.clientpolicy;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.ClientPolicyRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyException;

/**
 * OID4VCI 凭证客户端策略常量与查询辅助。
 * <p>定义内置策略实例及按名称查找 Realm 客户端策略的方法。</p>
 */
public abstract class CredentialClientPolicies {

    /**
     * 控制给定 {@code credential_configuration_id} 是否必须先通过 Credential Offer 提供。
     */
    public static PredicateCredentialClientPolicy VC_POLICY_CREDENTIAL_OFFER_REQUIRED = new PredicateCredentialClientPolicy(
            "oid4vci-offer-required", "vc.policy.offer.required", true, false);

    /**
     * 在当前 Realm 的客户端策略列表中按名称查找策略。
     * @param session Keycloak 会话
     * @param policyName 策略名称
     * @return 匹配的 {@link ClientPolicyRepresentation}，未找到时 {@code null}
     */
    public static ClientPolicyRepresentation findClientPolicyByName(KeycloakSession session, String policyName)  {
        try {
            RealmModel realm = session.getContext().getRealm();
            return session.clientPolicy().getClientPolicies(realm, false).getPolicies().stream()
                    .filter(cp -> cp.getName().equals(policyName))
                    .findFirst().orElse(null);
        } catch (ClientPolicyException ex) {
            throw new RuntimeException("Cannot access client policies", ex);
        }
    }
}
