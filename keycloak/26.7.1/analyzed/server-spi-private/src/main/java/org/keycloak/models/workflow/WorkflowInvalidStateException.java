package org.keycloak.models.workflow;

import org.keycloak.models.ModelValidationException;

/**
 * 工作流组件或提供者配置无效时抛出的校验异常。
 * <p>继承 {@link org.keycloak.models.ModelValidationException}，通常在 {@link WorkflowConditionProvider#validate()} 等自检阶段抛出。</p>
 */
public class WorkflowInvalidStateException extends ModelValidationException {

    /** @param message 无效状态描述 */
    public WorkflowInvalidStateException(String message) {
        super(message);
    }
}
