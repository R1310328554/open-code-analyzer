package org.keycloak.testsuite.authorization;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderAdminService;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * 资源所有者授予策略提供者：当请求身份与资源所有者一致时授予访问权限。
 */
public class GrantResourceOwnerPolicyProvider implements PolicyProviderFactory<PolicyRepresentation>, PolicyProvider {

    /** {@inheritDoc} 策略在管理控制台中的显示名称。 */
    @Override
    public String getName() {
        return "Allow Resource Owner";
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

    /** {@inheritDoc} 返回 {@code allow-resource-owner} 标识符。 */
    @Override
    public String getId() {
        return "allow-resource-owner";
    }

    /**
     * 比较请求身份与资源所有者；若 ID 相同则授予访问权限。
     *
     * @param evaluation 授权评估上下文
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        Resource resource = evaluation.getPermission().getResource();
        Identity identity = evaluation.getContext().getIdentity();

        if (identity.getId().equals(resource.getOwner())) {
            evaluation.grant();
        }
    }
}
