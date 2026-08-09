package com.taobao.arthas.core.command.express;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ognl.ClassResolver;

/**
 * 基于指定 {@link ClassLoader} 的 OGNL 类解析器，带本地类名缓存。
 * <p>
 * 无包名时会尝试 {@code java.lang.*} 前缀，行为参考 {@link ognl.DefaultClassResolver}。
 *
 * @author hengyunabc 2018-10-18
 * @see ognl.DefaultClassResolver
 */
public class ClassLoaderClassResolver implements ClassResolver {

    /** 用于加载目标类的 ClassLoader */
    private ClassLoader classLoader;

    /** 类名到 Class 的并发缓存，减少重复 loadClass */
    private Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>(101);

    public ClassLoaderClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Class classForName(String className, Map context) throws ClassNotFoundException {
        Class<?> result = null;

        if ((result = classes.get(className)) == null) {
            try {
                result = classLoader.loadClass(className);
            } catch (ClassNotFoundException ex) {
                // 短类名（无点）时尝试 java.lang 包
                if (className.indexOf('.') == -1) {
                    result = Class.forName("java.lang." + className);
                    classes.put("java.lang." + className, result);
                }
            }
            if (result == null) {
                return null;
            }
            classes.put(className, result);
        }
        return result;
    }
}
