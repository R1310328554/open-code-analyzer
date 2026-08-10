package org.keycloak.models.workflow.events;

import org.keycloak.models.GroupModel.GroupMemberLeaveEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.workflow.AbstractWorkflowEventProvider;
import org.keycloak.models.workflow.ResourceType;
import org.keycloak.models.workflow.WorkflowExecutionContext;
import org.keycloak.provider.ProviderEvent;

import static org.keycloak.models.utils.KeycloakModelUtils.GROUP_PATH_SEPARATOR;

/**
 * 用户离开组成员关系时触发的工作流事件 provider。
 * <p>
 * 监听 {@link GroupMemberLeaveEvent}；可选配置参数限定特定组路径（如 {@code user-group-membership-removed(/mygroup)}）。
 */
public class UserGroupMembershipRemovedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserGroupMembershipRemovedWorkflowEventProvider(final KeycloakSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    /** 本事件作用于用户资源。 */
    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    /** 仅支持用户离开组事件。 */
    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return providerEvent instanceof GroupMemberLeaveEvent;
    }

    /** 以离开组的用户 ID 作为 workflow 资源标识。 */
    @Override
    protected String resolveResourceId(ProviderEvent providerEvent) {
        if (providerEvent instanceof GroupMemberLeaveEvent gme) {
            return gme.getUser().getId();
        }
        return null;
    }

    /**
     * 评估事件是否匹配：先执行基类校验，再按配置参数比对组路径。
     */
    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        if (!super.evaluate(context)) {
            return false;
        }
        if (super.configParameter != null) {
            String groupName = configParameter;
            // 组名通过事件 provider 参数传入，例如 user-group-membership-removed(mygroup)
            if (!groupName.startsWith(GROUP_PATH_SEPARATOR))
                groupName = GROUP_PATH_SEPARATOR + groupName;
            ProviderEvent groupEvent = (ProviderEvent) context.getEvent().getEvent();
            if (groupEvent instanceof GroupMemberLeaveEvent leaveEvent) {
                return groupName.equals(KeycloakModelUtils.buildGroupPath(leaveEvent.getGroup()));
            } else {
                return false;
            }
        } else {
            // 未配置组路径时，任意离开组事件均匹配
            return true;
        }
    }
}
