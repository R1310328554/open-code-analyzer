package org.keycloak.models.workflow.events;

import org.keycloak.models.FederatedIdentityModel.FederatedIdentityCreatedEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.AbstractWorkflowEventProvider;
import org.keycloak.models.workflow.ResourceType;
import org.keycloak.models.workflow.WorkflowExecutionContext;
import org.keycloak.provider.ProviderEvent;

/**
 * 用户联邦身份添加工作流事件 provider：匹配 {@link FederatedIdentityCreatedEvent}。
 * <p>
 * 配置参数可选指定 IdP alias，例如 {@code user-federated-identity-added(myidp)}。
 */
public class UserFedIdentityAddedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserFedIdentityAddedWorkflowEventProvider(final KeycloakSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return providerEvent instanceof FederatedIdentityCreatedEvent;
    }

    @Override
    protected String resolveResourceId(ProviderEvent providerEvent) {
        if (providerEvent instanceof FederatedIdentityCreatedEvent fie) {
            return fie.getUser().getId();
        }
        return null;
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        if (!super.evaluate(context)) {
            return false;
        }
        if (super.configParameter != null) {
            // 配置参数为 IdP alias 时，须与联邦身份事件的 IdP 一致
            ProviderEvent fedIdentityEvent = (ProviderEvent) context.getEvent().getEvent();
            if (fedIdentityEvent instanceof FederatedIdentityCreatedEvent fie) {
                return configParameter.equals(fie.getFederatedIdentity().getIdentityProvider());
            } else {
                return false;
            }
        } else {
            // 无额外参数时直接通过
            return true;
        }
    }
}
