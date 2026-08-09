package com.taobao.arthas.core.util;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

/**
 * ClassLoader 查询与 classpath URL 解析工具。
 * <p>
 * 基于 {@link Instrumentation#getAllLoadedClasses()} 枚举 JVM 中已加载类对应的
 * ClassLoader，支持按 hash、类名、{@code toString()} 等条件筛选；并兼容 JDK 8
 * {@link URLClassLoader} 与 JDK 9+ 内部 {@code ucp} 结构读取 classpath。
 *
 * @author hengyunabc 2019-02-05
 */
public class ClassLoaderUtils {
    private static Logger logger = LoggerFactory.getLogger(ClassLoaderUtils.class);

    /**
     * 收集 JVM 当前已加载类涉及的全部非 null ClassLoader（去重）。
     *
     * @param inst Java Agent 注入的 Instrumentation
     * @return 去重后的 ClassLoader 集合
     */
    public static Set<ClassLoader> getAllClassLoader(Instrumentation inst) {
        Set<ClassLoader> classLoaderSet = new HashSet<ClassLoader>();

        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            ClassLoader classLoader = clazz.getClassLoader();
            if (classLoader != null) {
                classLoaderSet.add(classLoader);
            }
        }
        return classLoaderSet;
    }

    /**
     * 按十六进制 hash 查找唯一 ClassLoader（与 {@link #classLoaderHash} 输出一致）。
     *
     * @param inst Java Agent Instrumentation
     * @param hashCode ClassLoader 的 hash 十六进制字符串
     * @return 匹配的 ClassLoader，未找到或参数为空时返回 null
     */
    public static ClassLoader getClassLoader(Instrumentation inst, String hashCode) {
        if (hashCode == null || hashCode.isEmpty()) {
            return null;
        }

        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            ClassLoader classLoader = clazz.getClassLoader();
            if (classLoader != null) {
                if (Integer.toHexString(classLoader.hashCode()).equals(hashCode)) {
                    return classLoader;
                }
            }
        }
        return null;
    }

    /**
     * 通过 ClassLoader 实现类的全限定名查找匹配的 ClassLoader 列表。
     *
     * @param inst Instrumentation
     * @param classLoaderClassName ClassLoader 实现类名，如 {@code sun.misc.Launcher$AppClassLoader}
     * @return 匹配的 ClassLoader 列表；参数为空时返回 null
     */
    public static List<ClassLoader> getClassLoaderByClassName(Instrumentation inst, String classLoaderClassName) {
        if (classLoaderClassName == null || classLoaderClassName.isEmpty()) {
            return null;
        }
        Set<ClassLoader> classLoaderSet = getAllClassLoader(inst);
        List<ClassLoader> matchClassLoaders = new ArrayList<ClassLoader>();
        for (ClassLoader classLoader : classLoaderSet) {
            if (classLoader.getClass().getName().equals(classLoaderClassName)) {
                matchClassLoaders.add(classLoader);
            }
        }
        return matchClassLoaders;
    }

    /**
     * 计算 ClassLoader 在 Arthas 命令输出中使用的十六进制 hash。
     * <p>
     * 优先使用 {@link ClassLoader#hashCode()}；若结果非正数则回退到
     * {@link System#identityHashCode(Object)} 并取绝对值，保证展示稳定。
     *
     * @param classLoader 目标 ClassLoader，null 表示 Bootstrap
     * @return 十六进制 hash 字符串
     */
    public static String classLoaderHash(ClassLoader classLoader) {
        int hashCode = 0;
        if (classLoader == null) {
            hashCode = System.identityHashCode(classLoader);
        } else {
            hashCode = classLoader.hashCode();
        }
        if (hashCode <= 0) {
            hashCode = System.identityHashCode(classLoader);
            if (hashCode < 0) {
                hashCode = hashCode & Integer.MAX_VALUE;
            }
        }
        return Integer.toHexString(hashCode);
    }

    /**
     * 按 ClassLoader 实现类名和/或 {@link ClassLoader#toString()} 查找 ClassLoader 列表。
     * <p>
     * 仅传类名或仅传 toString 时做 OR 匹配；两者均传时取交集（同时满足两种条件）。
     *
     * @param inst Instrumentation
     * @param classLoaderClassName ClassLoader 实现类全限定名，可为空
     * @param classLoaderToString {@code ClassLoader.toString()} 的完整字符串，可为空
     * @return 匹配的 ClassLoader 列表（可能为空列表）
     */
    public static List<ClassLoader> getClassLoader(Instrumentation inst, String classLoaderClassName, String classLoaderToString) {
        List<ClassLoader> matchClassLoaders = new ArrayList<ClassLoader>();
        if (StringUtils.isEmpty(classLoaderClassName) && StringUtils.isEmpty(classLoaderToString)) {
            return matchClassLoaders;
        }
        Set<ClassLoader> classLoaderSet = getAllClassLoader(inst);
        List<ClassLoader> matchedByClassLoaderToStr = new ArrayList<ClassLoader>();
        for (ClassLoader classLoader : classLoaderSet) {
            // 仅按 ClassLoader 实现类名匹配
            if (!StringUtils.isEmpty(classLoaderClassName) && StringUtils.isEmpty(classLoaderToString)) {
                if (classLoader.getClass().getName().equals(classLoaderClassName)) {
                    matchClassLoaders.add(classLoader);
                }
            }
            // 仅按 toString 匹配
            else if (!StringUtils.isEmpty(classLoaderToString) && StringUtils.isEmpty(classLoaderClassName)) {
                if (classLoader.toString().equals(classLoaderToString)) {
                    matchClassLoaders.add(classLoader);
                }
            }
            // 类名与 toString 均指定：先分别收集，最后取交集
            else {
                if (classLoader.getClass().getName().equals(classLoaderClassName)) {
                    matchClassLoaders.add(classLoader);
                }
                if (classLoader.toString().equals(classLoaderToString)) {
                    matchedByClassLoaderToStr.add(classLoader);
                }
            }
        }
        // 两种条件都指定时，保留同时满足的 ClassLoader
        if (!StringUtils.isEmpty(classLoaderClassName) && !StringUtils.isEmpty(classLoaderToString)) {
            matchClassLoaders.retainAll(matchedByClassLoaderToStr);
        }
        return matchClassLoaders;
    }

    /**
     * 读取 ClassLoader 关联的 classpath URL 数组。
     * <p>
     * JDK 8 走 {@link URLClassLoader#getURLs()}；JDK 9+ 通过 {@code sun.misc.Unsafe}
     * 反射读取内部 {@code ucp.path} 字段（兼容 AppClassLoader 与 BuiltinClassLoader 层级差异）。
     *
     * @param classLoader 目标 ClassLoader
     * @return URL 数组；无法解析或不支持时返回 null
     */
    @SuppressWarnings({ "unchecked", "restriction" })
    public static URL[] getUrls(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            try {
                return ((URLClassLoader) classLoader).getURLs();
            } catch (Throwable e) {
                logger.error("classLoader: {} getUrls error", classLoader, e);
            }
        }

        // JDK 9+ 内部 ClassLoader 结构
        if (classLoader.getClass().getName().startsWith("jdk.internal.loader.ClassLoaders$")) {
            try {
                Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);

                Class<?> ucpOwner = classLoader.getClass();
                Field ucpField = null;

                // jdk 9~15: jdk.internal.loader.ClassLoaders$AppClassLoader.ucp
                // jdk 16~17: jdk.internal.loader.BuiltinClassLoader.ucp
                while (ucpField == null && !ucpOwner.getName().equals("java.lang.Object")) {
                    try {
                        ucpField = ucpOwner.getDeclaredField("ucp");
                    } catch (NoSuchFieldException ex) {
                        ucpOwner = ucpOwner.getSuperclass();
                    }
                }
                if (ucpField == null) {
                    return null;
                }

                long ucpFieldOffset = unsafe.objectFieldOffset(ucpField);
                Object ucpObject = unsafe.getObject(classLoader, ucpFieldOffset);
                if (ucpObject == null) {
                    return null;
                }

                // jdk.internal.loader.URLClassPath.path
                Field pathField = ucpField.getType().getDeclaredField("path");
                if (pathField == null) {
                    return null;
                }
                long pathFieldOffset = unsafe.objectFieldOffset(pathField);
                ArrayList<URL> path = (ArrayList<URL>) unsafe.getObject(ucpObject, pathFieldOffset);

                return path.toArray(new URL[path.size()]);
            } catch (Throwable e) {
                // 反射失败时静默返回 null
                return null;
            }
        }
        return null;
    }
}
