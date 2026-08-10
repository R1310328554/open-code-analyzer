package org.keycloak.models.workflow;

/**
 * 工作流事件：绑定资源类型、资源 ID、原始事件及事件提供者 ID。
 * <p>由 {@link WorkflowEventProvider} 从 Keycloak 事件转换而来，驱动工作流触发与条件评估。</p>
 */
public class WorkflowEvent {

    private final ResourceType type;
    private final String resourceId;
    private final Object event;
    private final String eventProviderId;

    /**
     * @param type 资源类型
     * @param resourceId 资源 ID
     * @param event 原始事件对象（可为 {@code null}）
     * @param eventProviderId 事件提供者 ID
     */
    public WorkflowEvent(ResourceType type, String resourceId, Object event, String eventProviderId) {
        this.type = type;
        this.resourceId = resourceId;
        this.event = event;
        this.eventProviderId = eventProviderId;
    }

    /** @return 事件关联的资源类型 */
    public ResourceType getResourceType() {
        return type;
    }

    /** @return 事件关联的资源 ID */
    public String getResourceId() {
        return resourceId;
    }

    /** @return 产生本工作流事件的事件提供者 ID */
    public String getEventProviderId() {
        return eventProviderId;
    }

    /** @return 原始 Keycloak 事件对象 */
    public Object getEvent() {
        return event;
    }
}
