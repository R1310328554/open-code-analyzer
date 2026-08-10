package org.keycloak.common.profile;

/**
 * Profile 配置无效或冲突时抛出的运行时异常。
 */
public class ProfileException extends RuntimeException {

    public ProfileException(String message) {
        super(message);
    }

    public ProfileException(String message, Throwable cause) {
        super(message, cause);
    }
}
