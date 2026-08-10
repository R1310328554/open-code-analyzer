package org.keycloak.testsuite.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 测试用 {@link URLClassLoader} 单例，从本地 HTTP 端点加载类。
 */
public class TestClassLoader extends URLClassLoader {

    /** 单例实例。 */
    private static TestClassLoader instance;

    /** 获取或创建指向 {@code http://localhost:8500/} 的类加载器实例。 */
    public static TestClassLoader getInstance() {
        if (instance == null) {
            ClassLoader parent = TestClassLoader.class.getClassLoader();
            try {
                instance = new TestClassLoader(parent);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    /**
     * 私有构造：父加载器 + 本地测试 URL。
     *
     * @param parent 父类加载器
     * @throws MalformedURLException 若 URL 格式无效
     */
    private TestClassLoader(ClassLoader parent) throws MalformedURLException {
        super(new URL[] { new URL("http://localhost:8500/") }, parent);
    }

}
