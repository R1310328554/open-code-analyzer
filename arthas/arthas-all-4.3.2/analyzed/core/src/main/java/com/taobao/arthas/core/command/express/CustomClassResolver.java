package com.taobao.arthas.core.command.express;

import ognl.ClassResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用当前线程上下文 ClassLoader 的 OGNL 类解析器单例。
 * <p>
 * 优先 {@link Thread#getContextClassLoader()}，否则 {@link Class#forName(String)}。
 *
 * @author diecui1202 on 2017/9/29.
 * @see ognl.DefaultClassResolver
 */
public class CustomClassResolver implements ClassResolver {

    /** 全局共享解析器实例 */
    public static final CustomClassResolver customClassResolver = new CustomClassResolver();

    /** 已解析类名缓存 */
    private Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>(101);

    private CustomClassResolver() {

    }

    @Override
    public Class classForName(String className, Map context) throws ClassNotFoundException {
        Class<?> result = null;

        if ((result = classes.get(className)) == null) {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                if (classLoader != null) {
                    result = classLoader.loadClass(className);
                } else {
                    result = Class.forName(className);
                }
            } catch (ClassNotFoundException ex) {
                // 短类名补全 java.lang 前缀
                if (className.indexOf('.') == -1) {
                    result = Class.forName("java.lang." + className);
                    classes.put("java.lang." + className, result);
                }
            }
            classes.put(className, result);
        }
        return result;
    }
}
