package org.keycloak.testsuite.authorization;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderAdminService;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * 资源可见性属性策略提供者：根据资源的 {@code visibility} 属性决定是否授予公开访问。
 */
public class ResourceVisibilityAttributePolicyProvider implements PolicyProviderFactory<PolicyRepresentation>, PolicyProvider {

    /** {@inheritDoc} 策略在管理控制台中的显示名称。 */
    @Override
    public String getName() {
        return "Check resource visibility";
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

    /** {@inheritDoc} 返回 {@code resource-visibility-attribute-policy} 标识符。 */
    @Override
    public String getId() {
        return "resource-visibility-attribute-policy";
    }

    /**
     * 若资源被标记为公开（非 private），则授予访问权限。
     *
     * @param evaluation 授权评估上下文
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        ResourcePermission permission = evaluation.getPermission();
        Resource resource = permission.getResource();

        if (isPublic(resource)) {
            evaluation.grant();
        }
    }

    /**
     * 判断资源是否公开：{@code visibility} 属性未设置或不包含 {@code private} 时视为公开。
     *
     * @param resource 待检查的资源
     * @return 若资源为公开则返回 {@code true}
     */
    private static boolean isPublic(Resource resource) {
        List<String> values = resource.getAttributes().get("visibility");
        return values == null || !values.contains("private");
    }
}
