package org.keycloak.models.workflow.conditions;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowConditionProviderFactory;

/**
 * 角色工作流条件工厂，provider ID 为 {@value #ID}（{@code has-role}）。
 */
public class RoleWorkflowConditionFactory implements WorkflowConditionProviderFactory<RoleWorkflowConditionProvider> {

    /** 条件 provider 标识：用户是否拥有指定角色。 */
    public static final String ID = "has-role";

    @Override
    public RoleWorkflowConditionProvider create(KeycloakSession session, String expectedRole) {
        return new RoleWorkflowConditionProvider(session, expectedRole);
    }

    @Override
    public String getId() {
        return ID;
    }

}
