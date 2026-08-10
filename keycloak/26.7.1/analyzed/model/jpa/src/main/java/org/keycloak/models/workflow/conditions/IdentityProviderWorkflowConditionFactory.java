package org.keycloak.models.workflow.conditions;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowConditionProviderFactory;

/**
 * 身份提供者关联工作流条件工厂，provider ID 为 {@value #ID}（{@code has-identity-provider-link}）。
 */
public class IdentityProviderWorkflowConditionFactory implements WorkflowConditionProviderFactory<IdentityProviderWorkflowConditionProvider> {

    /** 条件 provider 标识：用户是否已关联指定 IdP。 */
    public static final String ID = "has-identity-provider-link";

    @Override
    public IdentityProviderWorkflowConditionProvider create(KeycloakSession session, String configParameter) {
        return new IdentityProviderWorkflowConditionProvider(session, configParameter);
    }

    @Override
    public String getId() {
        return ID;
    }

}
