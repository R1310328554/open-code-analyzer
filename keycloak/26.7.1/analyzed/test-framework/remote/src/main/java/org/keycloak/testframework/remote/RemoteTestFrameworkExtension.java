package org.keycloak.testframework.remote;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.testframework.TestFrameworkExecutor;
import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.injection.Registry;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.remote.annotations.TestOnServer;
import org.keycloak.testframework.remote.runonserver.RunOnServerClient;
import org.keycloak.testframework.remote.runonserver.RunOnServerSupplier;
import org.keycloak.testframework.remote.runonserver.RunTestOnServer;
import org.keycloak.testframework.remote.runonserver.TestClassServerSupplier;
import org.keycloak.testframework.remote.timeoffset.TimeOffsetSupplier;

/**
 * 远程测试框架扩展：注册 Run-on-Server、时间偏移与远程提供者等 {@link Supplier}，
 * 并执行带 {@link TestOnServer} 注解的测试方法。
 */
public class RemoteTestFrameworkExtension implements TestFrameworkExtension, TestFrameworkExecutor {
    /** {@inheritDoc} 注册时间偏移、Run-on-Server、远程提供者与测试类服务等供应器。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(
                new TimeOffsetSupplier(),
                new RunOnServerSupplier(),
                new RemoteProvidersSupplier(),
                new TestClassServerSupplier()
        );
    }

    /** {@inheritDoc} 始终启用 {@link RemoteProviders} 类型注入。 */
    @Override
    public List<Class<?>> alwaysEnabledValueTypes() {
        return List.of(RemoteProviders.class);
    }

    /** {@inheritDoc} {@link TestOnServer} 方法额外需要 {@link RunOnServerClient}。 */
    @Override
    public List<Class<?>> getMethodValueTypes(Method method) {
        return isTestOnServer(method) ? List.of(RunOnServerClient.class) : Collections.emptyList();
    }

    /** {@inheritDoc} {@link TestOnServer} 方法可注入 {@link KeycloakSession} 参数。 */
    @Override
    public boolean supportsParameter(Method method, Class<?> parameterType) {
        return isTestOnServer(method) && parameterType.equals(KeycloakSession.class);
    }

    /** {@inheritDoc} 仅处理标注 {@link TestOnServer} 的测试方法。 */
    @Override
    public boolean shouldExecute(Method testMethod) {
        return isTestOnServer(testMethod);
    }

    /** {@inheritDoc} 通过 {@link RunOnServerClient} 在服务器上运行指定测试方法。 */
    @Override
    public void execute(Registry registry, Class<?> testClass, Method testMethod) {
        RunOnServerClient value = (RunOnServerClient) registry.getDeployedInstances().stream().filter(i -> i.getRequestedValueType() != null && i.getRequestedValueType().equals(RunOnServerClient.class)).findFirst().get().getValue();

        RunTestOnServer runTestOnServer = new RunTestOnServer(testClass.getName(), testMethod.getName());
        value.run(runTestOnServer);
    }

    /** 判断方法是否标注 {@link TestOnServer}。 */
    private boolean isTestOnServer(Method method) {
        return method.isAnnotationPresent(TestOnServer.class);
    }

}
