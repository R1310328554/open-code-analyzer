package org.keycloak.models.workflow.conditions;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowConditionProviderFactory;

/**
 * 用户属性工作流条件工厂，provider ID 为 {@value #ID}（{@code has-user-attribute}）。
 */
public class UserAttributeWorkflowConditionFactory implements WorkflowConditionProviderFactory<UserAttributeWorkflowConditionProvider> {

    /** 条件 provider 标识：用户是否拥有指定键值对的属性。 */
    public static final String ID = "has-user-attribute";

    @Override
    public UserAttributeWorkflowConditionProvider create(KeycloakSession session, String keyValuePair) {
        return new UserAttributeWorkflowConditionProvider(session, keyValuePair);
    }

    @Override
    public String getId() {
        return ID;
    }

}
