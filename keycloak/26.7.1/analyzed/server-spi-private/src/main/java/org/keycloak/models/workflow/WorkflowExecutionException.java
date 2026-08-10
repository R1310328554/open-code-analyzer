package org.keycloak.models.workflow;

import org.keycloak.models.ModelException;

/**
 * 工作流执行失败时抛出的模型异常。
 * <p>继承 {@link org.keycloak.models.ModelException}，表示运行时执行错误而非配置校验错误。</p>
 */
public class WorkflowExecutionException extends ModelException {

    /** @param message 错误描述 */
    public WorkflowExecutionException(String message) {
        super(message);
    }
}
