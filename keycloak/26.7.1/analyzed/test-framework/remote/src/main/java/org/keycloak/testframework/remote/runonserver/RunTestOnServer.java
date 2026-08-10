package org.keycloak.testframework.remote.runonserver;

import java.io.IOException;
import java.lang.reflect.Method;

import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.testframework.remote.providers.runonserver.RunOnServer;

/**
 * 在 Keycloak 服务端反射调用指定测试类的方法。
 * <p>
 * 测试类须有无参构造器，目标方法签名为 {@code void method(KeycloakSession session)}。
 */
public class RunTestOnServer implements RunOnServer {

    private final String testClass;
    private final String testMethod;

    /**
     * 指定要在服务端执行的测试类与方法名。
     *
     * @param testClass 测试类的全限定名
     * @param testMethod 接受 {@link KeycloakSession} 参数的方法名
     */
    public RunTestOnServer(String testClass, String testMethod) {
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    /** {@inheritDoc} 加载测试类并调用目标方法。 */
    @Override
    public void run(KeycloakSession session) throws IOException, VerificationException {
        try {
            Class<?> clazz = this.getClass().getClassLoader().loadClass(testClass);
            Object test = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getDeclaredMethod(testMethod, KeycloakSession.class);
            method.invoke(test, session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
