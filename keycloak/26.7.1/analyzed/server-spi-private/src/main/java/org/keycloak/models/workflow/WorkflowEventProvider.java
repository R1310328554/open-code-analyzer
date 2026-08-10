package org.keycloak.models.workflow;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderEvent;

/**
 * 工作流事件提供者：将 Keycloak 事件转换为 {@link WorkflowEvent} 并参与条件评估。
 * </p>
 * 实现类需判断用户事件、管理事件或 {@link org.keycloak.provider.ProviderEvent} 是否受支持，并完成转换与 {@link WorkflowExecutionContext} 下的匹配评估。
 */
public interface WorkflowEventProvider extends Provider {

    /**
     * 返回本事件提供者所处理的 {@link ResourceType}。
     *
     * @return the supported ResourceType for this event provider implementation
     */
    ResourceType getSupportedResourceType();

    /**
     * 将用户 {@link Event} 转换为 {@link WorkflowEvent}。
     *
     * @param event the user event to convert
     * @return a WorkflowEvent representing the given event, or {@code null} if the event is not supported
     */
    WorkflowEvent create(Event event);

    /**
     * 将 {@link AdminEvent} 转换为 {@link WorkflowEvent}。
     *
     * @param adminEvent the admin event to convert
     * @return a WorkflowEvent representing the given admin event, or {@code null} if the event is not supported
     */
    WorkflowEvent create(AdminEvent adminEvent);

    /**
     * 将 {@link ProviderEvent} 转换为 {@link WorkflowEvent}。
     *
     * @param providerEvent the provider event to convert
     * @return a WorkflowEvent representing the given provider event, or {@code null} if the event is not supported
     */
    WorkflowEvent create(ProviderEvent providerEvent);

    /**
     * 判断本提供者是否支持给定用户 {@link Event}。
     *
     * @param event the user event to check
     * @return {@code true} if the event is supported, {@code false} otherwise
     */
    boolean supports(Event event);

    /**
     * 判断本提供者是否支持给定 {@link AdminEvent}。
     *
     * @param adminEvent the admin event to check
     * @return {@code true} if the event is supported, {@code false} otherwise
     */
    boolean supports(AdminEvent adminEvent);

    /**
     * 判断本提供者是否支持给定 {@link ProviderEvent}。
     *
     * @param providerEvent the provider event to check
     * @return {@code true} if the event is supported, {@code false} otherwise
     */
    boolean supports(ProviderEvent providerEvent);

    /**
     * 评估执行上下文中的工作流事件是否匹配本提供者的判定条件。
     * </p>
     * 实现应检查 {@code context} 中的 {@link WorkflowEvent}，满足特定条件时返回 {@code true}。
     *
     * @param context the execution context for the workflow evaluation
     * @return {@code true} if the event matches the criteria, {@code false} otherwise
     */
    boolean evaluate(WorkflowExecutionContext context);

    @Override
    default void close() {
        // no-op
    }

}
