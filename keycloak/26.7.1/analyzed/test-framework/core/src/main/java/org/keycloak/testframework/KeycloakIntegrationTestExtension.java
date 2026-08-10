package org.keycloak.testframework;

import java.lang.reflect.Method;
import java.util.Optional;

import org.keycloak.testframework.injection.Registry;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Keycloak 集成测试的 JUnit 5 扩展：协调 {@link Registry} 生命周期、日志与 {@link DebugHelper}，
 * 并支持自定义 {@link TestFrameworkExecutor} 拦截测试方法。
 */
public class KeycloakIntegrationTestExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback, TestWatcher, InvocationInterceptor, ParameterResolver {

    /** 测试类开始前初始化日志分隔与 GitHub Actions 报告。 */
    @Override
    public void beforeAll(ExtensionContext context) {
        getLogHandler(context).beforeAll(context);
    }

    /** 单条用例前：日志、Registry 注入与 DebugHelper 标记。 */
    @Override
    public void beforeEach(ExtensionContext context) {
        getLogHandler(context).beforeEachStarting(context);
        getRegistry(context).beforeEach(context.getRequiredTestInstance(), context.getRequiredTestMethod());
        getLogHandler(context).beforeEachCompleted(context);
        DebugHelper.testStarted(context.getRequiredTestClass(), context.getRequiredTestMethod());
    }

    /** 单条用例后：清理 Registry 与日志过滤器。 */
    @Override
    public void afterEach(ExtensionContext context) {
        DebugHelper.testFinished();
        getLogHandler(context).afterEachStarting(context);
        getRegistry(context).afterEach();
        getLogHandler(context).afterEachCompleted(context);
    }

    /** 测试类结束后关闭 Registry 并汇总类级状态。 */
    @Override
    public void afterAll(ExtensionContext context) {
        getLogHandler(context).afterAll(context);
        getRegistry(context).afterAll();
    }

    /** 用例失败时转发日志并上报。 */
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        getLogHandler(context).testFailed(context);
    }

    /** 用例被禁用时清理日志过滤器。 */
    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        getLogHandler(context).testDisabled(context);
    }

    /** 用例成功时丢弃缓冲日志。 */
    @Override
    public void testSuccessful(ExtensionContext context) {
        getLogHandler(context).testSuccessful(context);
    }

    /** 用例中止时转发已缓冲日志。 */
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        getLogHandler(context).testAborted(context);
    }

    /** 委托 Registry 拦截测试方法（供自定义 Executor 使用）。 */
    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        getRegistry(extensionContext).intercept(invocation, invocationContext);
    }

    /** 获取或创建全局 {@link Registry} 并绑定当前 ExtensionContext。 */
    public static Registry getRegistry(ExtensionContext context) {
        ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
        Registry registry = (Registry) store.getOrComputeIfAbsent(Registry.class, r -> new Registry());
        registry.setCurrentContext(context);
        return registry;
    }

    /** 获取或创建全局 {@link LogHandler}。 */
    public static LogHandler getLogHandler(ExtensionContext context) {
        ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
        LogHandler logHandler = (LogHandler) store.computeIfAbsent(LogHandler.class, l -> new LogHandler());
        return logHandler;
    }

    /** 是否由 Registry 解析该参数（自定义 Executor 场景）。 */
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        return getRegistry(context).supportsParameter(parameterContext, context);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        // 当前仅供自定义 TestFrameworkExecutor 使用，参数注入由其负责，此处返回 null
        return null;
    }
}
