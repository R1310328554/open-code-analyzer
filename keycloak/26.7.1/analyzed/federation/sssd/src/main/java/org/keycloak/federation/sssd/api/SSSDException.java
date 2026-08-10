package org.keycloak.federation.sssd.api;

/**
 * SSSD 联邦集成运行时异常，封装 D-Bus 通信或属性解析失败等错误。
 *
 * @author <a href="mailto:bruno@abstractj.org">Bruno Oliveira</a>
 */
public class SSSDException extends RuntimeException {
    /** 无消息构造 */
    public SSSDException() {
    }

    /**
     * @param message 错误描述
     */
    public SSSDException(String message) {
        super(message);
    }

    /**
     * @param message 错误描述
     * @param cause 原始异常
     */
    public SSSDException(String message, Throwable cause) {
        super(message, cause);
    }
}
