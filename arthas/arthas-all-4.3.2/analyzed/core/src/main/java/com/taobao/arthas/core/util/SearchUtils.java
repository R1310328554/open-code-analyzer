package com.taobao.arthas.core.util;

import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.arthas.core.util.matcher.RegexMatcher;
import com.taobao.arthas.core.util.matcher.WildcardMatcher;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 基于 {@link Instrumentation} 的已加载类搜索工具。
 * <p>
 * 支持通配符/正则类名匹配、子类扩展、ClassLoader hash 过滤及内部类检索，
 * 供 sc、sm、watch 等命令在 JVM 内快速定位目标类。
 * Created by vlinux on 15/5/17.
 * @author diecui1202 on 2017/09/07.
 */
public class SearchUtils {

    /**
     * 根据类名匹配，搜索已经被JVM加载的类
     *
     * @param inst             inst
     * @param classNameMatcher 类名匹配
     * @param limit            最大匹配限制
     * @return 匹配的类集合
     */
    public static Set<Class<?>> searchClass(Instrumentation inst, Matcher<String> classNameMatcher, int limit) {
        if (classNameMatcher == null) {
            return Collections.emptySet();
        }
        final Set<Class<?>> matches = new HashSet<Class<?>>();
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz == null) {
                continue;   
            }
            if (classNameMatcher.matching(clazz.getName())) {
                matches.add(clazz);
            }
            if (matches.size() >= limit) {
                break;
            }
        }
        return matches;
    }

    /** 无数量上限地搜索与匹配器相符的已加载类。 */
    public static Set<Class<?>> searchClass(Instrumentation inst, Matcher<String> classNameMatcher) {
        return searchClass(inst, classNameMatcher, Integer.MAX_VALUE);
    }

    /**
     * 按类名模式搜索；若未禁用子类扩展则一并纳入匹配类的子类型。
     *
     * @param inst Instrumentation
     * @param classPattern 类名通配符或正则
     * @param isRegEx true 使用正则，false 使用通配符
     */
    public static Set<Class<?>> searchClass(Instrumentation inst, String classPattern, boolean isRegEx) {
        Matcher<String> classNameMatcher = classNameMatcher(classPattern, isRegEx);
        return GlobalOptions.isDisableSubClass ? searchClass(inst, classNameMatcher) :
                searchSubClass(inst, searchClass(inst, classNameMatcher));
    }

    /** 在类名匹配基础上，再按 ClassLoader 十六进制 hash 过滤结果。 */
    public static Set<Class<?>> searchClass(Instrumentation inst, String classPattern, boolean isRegEx, String code) {
        Set<Class<?>> matchedClasses = searchClass(inst, classPattern, isRegEx);
        return filter(matchedClasses, code);
    }

    /** 仅匹配类名本身，不扩展子类。 */
    public static Set<Class<?>> searchClassOnly(Instrumentation inst, String classPattern, boolean isRegEx) {
        Matcher<String> classNameMatcher = classNameMatcher(classPattern, isRegEx);
        return searchClass(inst, classNameMatcher);
    }

    /** 通配符匹配类名，并限制最大返回数量。 */
    public static Set<Class<?>> searchClassOnly(Instrumentation inst, String classPattern, int limit) {
        Matcher<String> classNameMatcher = classNameMatcher(classPattern, false);
        return searchClass(inst, classNameMatcher, limit);
    }

    /** 类名匹配 + ClassLoader hash 过滤，不扩展子类。 */
    public static Set<Class<?>> searchClassOnly(Instrumentation inst, String classPattern, boolean isRegEx, String code) {
        Set<Class<?>> matchedClasses = searchClassOnly(inst, classPattern, isRegEx);
        return filter(matchedClasses, code);
    }

    /** 按 ClassLoader {@code hashCode()} 十六进制串过滤类集合。 */
    private static Set<Class<?>> filter(Set<Class<?>> matchedClasses, String code) {
        if (code == null) {
            return matchedClasses;
        }

        Set<Class<?>> result = new HashSet<Class<?>>();
        if (matchedClasses != null) {
            for (Class<?> c : matchedClasses) {
                if (c.getClassLoader() != null && Integer.toHexString(c.getClassLoader().hashCode()).equals(code)) {
                    result.add(c);
                }
            }
        }
        return result;
    }

    /**
     * 根据模式串构造类名 {@link Matcher}。
     * <p>空模式时：正则默认为 {@code .*}，通配符默认为 {@code *}；
     * 非 Lambda 类名将 {@code /} 规范为 {@code .}。
     */
    public static Matcher<String> classNameMatcher(String classPattern, boolean isRegEx) {
        if (StringUtils.isEmpty(classPattern)) {
            classPattern = isRegEx ? ".*" : "*";
        }
        if (!classPattern.contains("$$Lambda")) {
            classPattern = StringUtils.replace(classPattern, "/", ".");
        }
        return isRegEx ? new RegexMatcher(classPattern) : new WildcardMatcher(classPattern);
    }

    /**
     * 搜索目标类的子类
     *
     * @param inst     inst
     * @param classSet 当前类集合
     * @return 匹配的子类集合
     */
    public static Set<Class<?>> searchSubClass(Instrumentation inst, Set<Class<?>> classSet) {
        final Set<Class<?>> matches = new HashSet<Class<?>>();
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz == null) {
                continue;   
            }
            for (Class<?> superClass : classSet) {
                if (superClass.isAssignableFrom(clazz)) {
                    matches.add(clazz);
                    break;
                }
            }
        }
        return matches;
    }

    /**
     * 搜索目标类的内部类
     *
     * @param inst inst
     * @param c    当前类
     * @return 匹配的类的集合
     */
    public static Set<Class<?>> searchInnerClass(Instrumentation inst, Class<?> c) {
        final Set<Class<?>> matches = new HashSet<Class<?>>();
        for (Class<?> clazz : inst.getInitiatedClasses(c.getClassLoader())) {
            if (c.getClassLoader() != null && clazz.getClassLoader() != null && c.getClassLoader().equals(clazz.getClassLoader())) {
                if (clazz.getName().startsWith(c.getName())) {
                    matches.add(clazz);
                }
            }
        }
        return matches;
    }
}
