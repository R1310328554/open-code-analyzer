package org.keycloak.testframework;

import java.lang.reflect.Method;

/**
 * 测试执行上下文辅助类，供条件日志或调试逻辑判断当前是否在指定测试类/方法中运行。
 * 由 {@link KeycloakIntegrationTestExtension} 在每条用例前后更新状态。
 */
public class DebugHelper {

    private static boolean IN_TEST = false;
    private static String CURRENT_TEST_CLASS;
    private static String CURRENT_TEST_METHOD;

    /** 记录当前执行的测试类与方法。 */
    static void testStarted(Class<?> clazz, Method method) {
        IN_TEST = true;
        CURRENT_TEST_CLASS = clazz.getName();
        CURRENT_TEST_METHOD = method.getName();
    }

    /** 清除当前测试上下文。 */
    static void testFinished() {
        IN_TEST = false;
        CURRENT_TEST_CLASS = null;
        CURRENT_TEST_METHOD = null;
    }

    /** @return 是否处于某条 JUnit 测试方法执行期间 */
    public static boolean isInTest() {
        return IN_TEST;
    }

    /**
     * 判断当前是否正在执行匹配的测试。
     * @param test 格式为 {@code 类名#方法名}，类名可为全限定名或简单类名
     * @return 类名与方法名均匹配时返回 true
     */
    public static boolean isInTest(String test) {
        if (!IN_TEST) {
            return false;
        }

        String[] split = test.split("#");

        String expectedClassName = split[0].isEmpty() ? null : split[0];
        String expectedMethod = split.length > 1 ? split[1] : null;

        if (expectedClassName != null) {
            if (expectedClassName.indexOf('.') != -1) {
                if (!expectedClassName.equals(CURRENT_TEST_CLASS)) {
                    return false;
                }
            } else {
                String currentTestSimpleName = CURRENT_TEST_CLASS.substring(CURRENT_TEST_CLASS.lastIndexOf('.') + 1);
                if (!expectedClassName.equals(currentTestSimpleName)) {
                    return false;
                }
            }
        }

        if (expectedMethod != null) {
            if (!expectedMethod.equals(CURRENT_TEST_METHOD)) {
                return false;
            }
        }

        return true;
    }

}
