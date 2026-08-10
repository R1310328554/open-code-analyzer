package org.keycloak.testframework;

import java.util.logging.Filter;
import java.util.logging.Handler;

import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.github.GitHubActionReport;

import io.quarkus.runtime.logging.LoggingSetupRecorder;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.jboss.logging.Logger;
import org.jboss.logmanager.LogManager;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * 集成测试日志与 GitHub Actions 报告协调器：初始化 Quarkus/JBoss 日志、
 * 在每条用例前后输出结构化状态，并按配置启用 {@link LogFilter}。
 */
public class LogHandler implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger("testinfo");
    private final boolean logFilterEnabled;
    private final GitHubActionReport gitHubActionReport = new GitHubActionReport();

    /** 读取配置、设置 LogManager 系统属性并初始化 Quarkus 日志。 */
    public LogHandler() {
        logFilterEnabled = Config.get("kc.test.log.filter", false, Boolean.class);

        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        System.setProperty("junit.quarkus.enable-basic-logging", "false");
        System.setProperty("log4j2.disable.jmx", "true");

        initializeQuarkusLogging();
    }

    /** 释放 Quarkus 测试 Config，改用测试框架 {@link Config} 并处理启动失败日志。 */
    private static void initializeQuarkusLogging() {
        // 不使用 Quarkus TestConfigProviderResolver 创建的 Config。
        // 也可用 Customizer 保留 Quarkus Config，但本框架非 Quarkus 测试，依赖其 Config 既非必需也可能不稳定。
        SmallRyeConfigProviderResolver configProviderResolver = (SmallRyeConfigProviderResolver) ConfigProviderResolver.instance();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        configProviderResolver.releaseConfig(cl);
        ConfigProviderResolver.instance().registerConfig(Config.getConfig(), cl);
        LoggingSetupRecorder.handleFailedStart();
    }

    /** 测试类开始：分隔线、RUNNING 状态与 CI 类级开始事件。 */
    public void beforeAll(ExtensionContext context) {
        logDivider(Logger.Level.INFO);
        logTestClassStatus(context, Status.RUNNING, Logger.Level.INFO);
        gitHubActionReport.onClassStart();
    }

    /** 单条用例初始化阶段 DEBUG 日志。 */
    public void beforeEachStarting(ExtensionContext context) {
        logTestMethodStatus(context, Status.INIT, Logger.Level.DEBUG);
    }

    /** 用例 RUNNING 状态、安装 LogFilter、CI 方法开始。 */
    public void beforeEachCompleted(ExtensionContext context) {
        logTestMethodStatus(context, Status.RUNNING, Logger.Level.DEBUG);
        initLogFilter();
        gitHubActionReport.onMethodStart();
    }

    /** 测试类结束：根据执行异常判定 SUCCESS/FAILED 并上报 CI。 */
    public void afterAll(ExtensionContext context) {
        Status status = context.getExecutionException().isPresent() ? Status.FAILED : Status.SUCCESS;
        if (status == Status.FAILED) {
            gitHubActionReport.onClassError(context);
        } else {
            gitHubActionReport.onClassSuccess(context);
        }
        logTestClassStatus(context, status, Logger.Level.DEBUG);
    }

    /** 用例 CLEANUP 阶段 DEBUG 日志。 */
    public void afterEachStarting(ExtensionContext context) {
        logTestMethodStatus(context, Status.CLEANUP, Logger.Level.DEBUG);
    }

    /** 用例清理完成（当前无额外操作）。 */
    public void afterEachCompleted(ExtensionContext context) {
    }

    /** 成功：丢弃缓冲日志并记录 SUCCESS。 */
    public void testSuccessful(ExtensionContext context) {
        gitHubActionReport.onMethodSuccess(context);
        clearLogFilter(false);
        logTestMethodStatus(context, Status.SUCCESS, Logger.Level.DEBUG);
    }

    /** 失败：转发缓冲日志并 ERROR 级别记录。 */
    public void testFailed(ExtensionContext context) {
        gitHubActionReport.onMethodFailed(context);
        clearLogFilter(true);
        logTestMethodStatus(context, Status.FAILED, Logger.Level.ERROR);
    }

    /** 中止：转发缓冲日志。 */
    public void testAborted(ExtensionContext context) {
        clearLogFilter(true);
        logTestMethodStatus(context, Status.ABORTED, Logger.Level.ERROR);
    }

    /** 禁用：丢弃缓冲日志。 */
    public void testDisabled(ExtensionContext context) {
        clearLogFilter(false);
        logTestMethodStatus(context, Status.DISABLED, Logger.Level.DEBUG);
    }

    /** 打印 GitHub Actions 测试摘要。 */
    public void close() {
        gitHubActionReport.printSummary();
    }

    private void logDivider(Logger.Level level) {
        LOGGER.log(level, "----------------------------------------------------------------");
    }

    private void logTestClassStatus(ExtensionContext context, Status status, Logger.Level level) {
        LOGGER.logv(level, "{0} - {1}", status.getLogString(), context.getRequiredTestClass().getName());
    }

    private void logTestMethodStatus(ExtensionContext context, Status status, Logger.Level level) {
        LOGGER.logv(level, "{0} - {1} / {2}", status.getLogString(), context.getRequiredTestClass().getName(), context.getRequiredTestMethod().getName());
    }

    /** 若启用 kc.test.log.filter，为根 Logger 的 Handler 安装 LogFilter。 */
    private void initLogFilter() {
        if (!logFilterEnabled) {
            return;
        }

        for (Handler handler : LogManager.getLogManager().getLogger("").getHandlers()) {
            handler.setFilter(new LogFilter());
        }
    }

    /** 移除 Handler 上的 LogFilter 并按需转发缓冲日志。 */
    private void clearLogFilter(boolean forwardLogs) {
        if (!logFilterEnabled) {
            return;
        }

        for (Handler handler : LogManager.getLogManager().getLogger("").getHandlers()) {
            Filter filter = handler.getFilter();
            handler.setFilter(null);
            if (filter instanceof LogFilter) {
                ((LogFilter) filter).clear(forwardLogs);
            }
        }
    }

    /** 测试执行阶段，用于对齐日志列宽。 */
    private enum Status {
        INIT,
        CLEANUP,
        RUNNING,
        FINISHED,
        SUCCESS,
        ABORTED,
        DISABLED,
        FAILED;

        private final String logString;

        Status() {
            this.logString = String.format("%1$10s", this);
        }

        private String getLogString() {
            return logString;
        }
    }

}
