package org.keycloak.testframework;

/**
 * 测试类配置无效或托管资源不可恢复地初始化失败时抛出（例如服务器无法启动）。
 * 抛出后同一测试类内后续测试方法将被跳过。
 */
public class FatalTestClassException extends RuntimeException {

    /** @param message 描述致命错误的说明 */
    public FatalTestClassException(String message) {
        super(message);
    }

}
