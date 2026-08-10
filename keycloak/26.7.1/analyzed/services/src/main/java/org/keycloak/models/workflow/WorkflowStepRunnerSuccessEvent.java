package org.keycloak.models.workflow;

import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderEvent;

/**
 * 工作流定时步骤执行成功时发布的 {@link ProviderEvent}。
 * <p>携带触发执行的 {@link KeycloakSession}，供其他 Provider 监听后续处理。</p>
 *
 * @param session 执行定时任务的 Keycloak 会话
 */
public record WorkflowStepRunnerSuccessEvent(KeycloakSession session) implements ProviderEvent {
}
