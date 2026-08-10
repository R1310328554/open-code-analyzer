package org.keycloak.ssf;

import org.keycloak.models.ModelException;

/**
 * SSF（Shared Signals Framework，共享信号框架）相关操作抛出的模型层异常。
 * <p>继承自 {@link ModelException}，用于 SSF 事件处理、验证等场景。</p>
 */
public class SsfException extends ModelException {

    public SsfException() {
    }

    public SsfException(String message) {
        super(message);
    }

    public SsfException(String message, Throwable cause) {
        super(message, cause);
    }
}
