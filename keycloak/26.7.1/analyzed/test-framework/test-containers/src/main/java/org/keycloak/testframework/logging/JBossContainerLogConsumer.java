package org.keycloak.testframework.logging;

import java.util.function.Consumer;

import org.jboss.logging.Logger;
import org.testcontainers.containers.output.OutputFrame;

/**
 * 将 Testcontainers 容器标准输出/错误流转发至 JBoss Logging。
 * <p>
 * STDOUT 记为 DEBUG，STDERR 记为 WARN，便于在测试日志中查看容器输出。
 */
public class JBossContainerLogConsumer implements Consumer<OutputFrame> {

    private final Logger logger;

    /**
     * 指定接收容器日志的目标日志器。
     *
     * @param logger JBoss Logging 日志器
     */
    public JBossContainerLogConsumer(Logger logger) {
        this.logger = logger;
    }

    /** {@inheritDoc} 按输出类型写入 DEBUG 或 WARN 级别日志。 */
    @Override
    public void accept(OutputFrame outputFrame) {
        OutputFrame.OutputType type = outputFrame.getType();
        switch (type) {
            case STDOUT:
                logger.debug(outputFrame.getUtf8StringWithoutLineEnding());
                break;
            case STDERR:
                logger.warn(outputFrame.getUtf8StringWithoutLineEnding());
                break;
        }
    }

}
