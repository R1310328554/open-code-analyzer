package org.keycloak.ssf.transmitter.stream;

import org.keycloak.ssf.SsfException;

/**
 * 创建流时检测到重复流配置时抛出的异常。
 * 通常映射为 HTTP 409 Conflict。
 */
public class DuplicateStreamConfigException extends SsfException {

    /** 无消息构造。 */
    public DuplicateStreamConfigException() {
    }

    /**
     * @param message 错误描述
     */
    public DuplicateStreamConfigException(String message) {
        super(message);
    }

    /**
     * @param message 错误描述
     * @param cause 根因
     */
    public DuplicateStreamConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
