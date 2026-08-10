package org.keycloak.testsuite.authorization;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderAdminService;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * 特定地址允许策略提供者：仅当客户端 IP 为本地回环地址时授予访问，用于测试套件。
 */
public class SpecificAddressPolicyProvider implements PolicyProviderFactory<PolicyRepresentation>, PolicyProvider {

    /** {@inheritDoc} 策略在管理控制台中的显示名称。 */
    @Override
    public String getName() {
        return "Allow from specific address";
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

    /** {@inheritDoc} 返回 {@code only-from-specific-address-policy} 标识符。 */
    @Override
    public String getId() {
        return "only-from-specific-address-policy";
    }

    /**
     * {@inheritDoc}
     * 当客户端网络 IP 为 IPv4 回环（127.0.0.1）或 IPv6 回环（::1）时调用 {@link Evaluation#grant()}。
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        EvaluationContext context = evaluation.getContext();
        Attributes attributes = context.getAttributes();

        // 匹配本地回环地址（IPv4 或 IPv6）时授予访问
        if (attributes.containsValue("kc.client.network.ip_address", "127.0.0.1") || attributes.containsValue("kc.client.network.ip_address", "0:0:0:0:0:0:0:1")) {
            evaluation.grant();
        }
    }
}
