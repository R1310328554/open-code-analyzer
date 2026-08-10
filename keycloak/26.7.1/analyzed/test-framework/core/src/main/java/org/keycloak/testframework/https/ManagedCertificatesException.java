package org.keycloak.testframework.https;

/**
 * 托管测试证书生成、加载或 SSL 上下文构建失败时抛出的运行时异常。
 */
public class ManagedCertificatesException extends RuntimeException {
    /**
     * @param cause 原始失败原因
     */
    public ManagedCertificatesException(Throwable cause) {
        super(cause);
    }
}
