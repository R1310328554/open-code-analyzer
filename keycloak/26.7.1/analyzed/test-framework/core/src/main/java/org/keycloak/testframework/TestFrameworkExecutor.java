package org.keycloak.testframework;

import java.lang.reflect.Method;
import java.util.List;

import org.keycloak.testframework.injection.Registry;

/**
 * 自定义测试执行策略：决定如何解析参数、是否执行某测试方法，以及如何驱动 {@link Registry} 运行用例。
 */
public interface TestFrameworkExecutor {

    /** @param method 测试方法 @return 该方法需注入的值类型列表 */
    List<Class<?>> getMethodValueTypes(Method method);

    /** 是否支持为该方法解析给定参数类型。 */
    boolean supportsParameter(Method method, Class<?> parameterType);

    /** 是否应执行该测试方法（可用于过滤/重试策略）。 */
    boolean shouldExecute(Method testMethod);

    /** 使用 Registry 中的依赖执行测试方法。 */
    void execute(Registry registry, Class<?> testClass, Method testMethod);

}
