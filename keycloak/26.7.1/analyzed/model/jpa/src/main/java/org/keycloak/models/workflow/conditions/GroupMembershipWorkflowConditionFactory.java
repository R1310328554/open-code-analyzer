package org.keycloak.models.workflow.conditions;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowConditionProviderFactory;

/**
 * 组成员关系工作流条件工厂，provider ID 为 {@value #ID}（{@code is-member-of}）。
 */
public class GroupMembershipWorkflowConditionFactory implements WorkflowConditionProviderFactory<GroupMembershipWorkflowConditionProvider> {

    /** 条件 provider 标识：用户是否为指定组成员。 */
    public static final String ID = "is-member-of";

    @Override
    public GroupMembershipWorkflowConditionProvider create(KeycloakSession session, String configParameter) {
        return new GroupMembershipWorkflowConditionProvider(session, configParameter);
    }

    @Override
    public String getId() {
        return ID;
    }
}
