package org.keycloak.testframework.injection.predicates;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.keycloak.testframework.TestFrameworkExecutor;

/**
 * 针对 {@link org.keycloak.testframework.TestFrameworkExecutor} 的谓词工厂。
 * <p>
 * 封装执行器是否应运行某测试方法的判断逻辑。
 */
public interface TestFrameworkExecutorPredicates {

    /**
     * 返回会调用 {@link TestFrameworkExecutor#shouldExecute(Method)} 的谓词。
     *
     * @param method 待执行的测试方法
     * @return 执行器筛选谓词
     */
    static Predicate<TestFrameworkExecutor> shouldExecute(Method method) {
        return r -> r.shouldExecute(method);
    }

}
