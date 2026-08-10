package org.keycloak.testframework.remote.providers.runonserver;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 从远程测试类服务加载测试侧类的 {@link URLClassLoader}。
 * <p>
 * 在嵌入式模式下避免误用系统类加载器，确保服务器端反序列化时使用 Quarkus 侧可见的类定义。
 */
public class TestClassLoader extends URLClassLoader {

    /** 远程测试类 HTTP 服务的基础 URL。 */
    private static final String testClassUrl = "http://localhost:8500/test-classes/";

    /**
     * 指向本地测试类服务的类加载器。
     *
     * @throws MalformedURLException 测试类 URL 无效时抛出
     */
    public TestClassLoader() throws MalformedURLException {
        super(new URL[] { new URL(testClassUrl)}, TestClassLoader.class.getClassLoader());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadClass = super.loadClass(name, resolve);

        // 嵌入式模式下系统类加载器也能看到测试类，但会绕过 Quarkus 类加载器；
        // 若检测到由系统类加载器加载，则强制从远程测试类提供者重新加载。
        if (loadClass != null && loadClass.getClassLoader() != null && loadClass.getClassLoader().equals(ClassLoader.getSystemClassLoader())) {
            return findClass(name);
        } else {
            return loadClass;
        }
    }
}
