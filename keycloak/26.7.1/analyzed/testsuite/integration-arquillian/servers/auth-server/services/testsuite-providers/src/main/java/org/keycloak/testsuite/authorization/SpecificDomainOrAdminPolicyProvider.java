package org.keycloak.testsuite.authorization;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderAdminService;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * 特定域名或管理员策略提供者：当用户为 realm 管理员或邮箱域名匹配时授予访问，用于测试套件。
 */
public class SpecificDomainOrAdminPolicyProvider implements PolicyProviderFactory<PolicyRepresentation>, PolicyProvider {

    /** {@inheritDoc} 策略在管理控制台中的显示名称。 */
    @Override
    public String getName() {
        return "Allow from Specific Domain or Admin";
    }

    /** {@inheritDoc} 策略分组为测试套件专用。 */
    @Override
    public String getGroup() {
        return "Test Suite";
    }

    /** {@inheritDoc} 返回自身作为策略提供者实例。 */
    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return this;
    }

    /** {@inheritDoc} 返回空的策略表示对象。 */
    @Override
    public PolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        return new PolicyRepresentation();
    }

    /** {@inheritDoc} 策略表示类型为 {@link PolicyRepresentation}。 */
    @Override
    public Class getRepresentationType() {
        return PolicyRepresentation.class;
    }

    /** {@inheritDoc} 不提供管理 REST 资源。 */
    @Override
    public PolicyProviderAdminService getAdminResource(ResourceServer resourceServer, AuthorizationProvider authorization) {
        return null;
    }

    /** {@inheritDoc} 通过会话创建时不返回实例（由 {@link #create(AuthorizationProvider)} 提供）。 */
    @Override
    public PolicyProvider create(KeycloakSession session) {
        return null;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回 {@code only-from-specific-domain-or-admin-policy} 标识符。 */
    @Override
    public String getId() {
        return "only-from-specific-domain-or-admin-policy";
    }

    /**
     * {@inheritDoc}
     * 当身份拥有 {@code admin} realm 角色或邮箱以 {@code @keycloak.org} 结尾时调用 {@link Evaluation#grant()}。
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        Identity identity = evaluation.getContext().getIdentity();
        String email = identity.getAttributes().getValue("email").asString(0);

        // 管理员角色或 keycloak.org 域名邮箱均授予访问
        if (identity.hasRealmRole("admin") || email.endsWith("@keycloak.org")) {
            evaluation.grant();
        }
    }
}
