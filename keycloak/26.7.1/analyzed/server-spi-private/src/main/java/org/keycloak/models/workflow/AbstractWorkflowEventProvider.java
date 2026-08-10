package org.keycloak.models.workflow;

import java.util.Objects;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderEvent;

/**
 * {@link WorkflowEventProvider} 的抽象基类，为多数方法提供默认实现。
 * </p>
 * 子类仅需实现 {@link #getSupportedResourceType()} 及相应的 {@code supports} 方法；若支持 {@link ProviderEvent}，还需实现 {@link #resolveResourceId} 以解析资源 ID。
 */
public abstract class AbstractWorkflowEventProvider implements WorkflowEventProvider {

    protected final String providerId;
    protected final String configParameter;
    protected final KeycloakSession session;

    /** @param session Keycloak 会话；@param configParameter 配置参数名；@param providerId 提供者 ID */
    public AbstractWorkflowEventProvider(final KeycloakSession session, final String configParameter, final String providerId) {
        this.providerId = providerId;
        this.configParameter = configParameter;
        this.session = session;
    }

    /** 将用户 {@link Event} 转换为 {@link WorkflowEvent}（不支持时返回 null）。 */
    @Override
    public WorkflowEvent create(Event event) {
        if (supports(event)) {
            ResourceType resourceType = getSupportedResourceType();
            String resourceIdFromEvent = resourceType.resolveResourceId(session, event);
            return resourceIdFromEvent != null ? new WorkflowEvent(resourceType, resourceIdFromEvent, event, providerId) : null;
        }
        return null;
    }

    /** 将 {@link AdminEvent} 转换为工作流事件。 */
    @Override
    public WorkflowEvent create(AdminEvent adminEvent) {
        if (supports(adminEvent)) {
            return new WorkflowEvent(getSupportedResourceType(), adminEvent.getResourceId(), adminEvent, providerId);
        }
        return null;
    }

    /** 将 {@link ProviderEvent} 转换为工作流事件（需子类实现资源 ID 解析）。 */
    @Override
    public WorkflowEvent create(ProviderEvent providerEvent) {
        if (supports(providerEvent)) {
            String resourceId = resolveResourceId(providerEvent);
            return resourceId != null ? new WorkflowEvent(getSupportedResourceType(), resourceId, providerEvent, providerId) : null;
        }
        return null;
    }

    /** 事件是否由本提供者产生（比较 providerId）。 */
    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        WorkflowEvent event = context.getEvent();
        return event != null && Objects.equals(this.providerId, event.getEventProviderId());
    }

    @Override
    public boolean supports(Event event) {
        return false;
    }

    @Override
    public boolean supports(AdminEvent adminEvent) {
        return false;
    }

    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return false;
    }

    /**
     * 从 {@link ProviderEvent} 解析资源 ID；默认返回 null，由子类覆盖。
     *
     * @param providerEvent the provider event
     * @return the resolved resource ID, or {@code null} if it cannot be resolved
     */
    protected String resolveResourceId(ProviderEvent providerEvent) {
        return null;
    }
}
