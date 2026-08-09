package com.taobao.arthas.common;

import java.util.Properties;

/**
 * 运行时 Java 版本探测：基于 {@code java.specification.version} 提供比较与布尔判断。
 *
 * @author hengyunabc 2018-11-21
 */
public class JavaVersionUtils {
    private static final String VERSION_PROP_NAME = "java.specification.version";
    /** 当前 JVM 规范版本字符串，如 1.8、11、17 */
    private static final String JAVA_VERSION_STR = System.getProperty(VERSION_PROP_NAME);
    private static final float JAVA_VERSION = Float.parseFloat(JAVA_VERSION_STR);

    private JavaVersionUtils() {
    }

    /** 返回启动时缓存的版本字符串 */
    public static String javaVersionStr() {
        return JAVA_VERSION_STR;
    }

    public static String javaVersionStr(Properties props) {
        return (null != props) ? props.getProperty(VERSION_PROP_NAME): null;
    }

    /** 返回解析后的浮点版本号，用于大小比较 */
    public static float javaVersion() {
        return JAVA_VERSION;
    }

    public static boolean isJava6() {
        return "1.6".equals(JAVA_VERSION_STR);
    }

    public static boolean isJava7() {
        return "1.7".equals(JAVA_VERSION_STR);
    }

    /** 是否为 Java 8（1.8） */
    public static boolean isJava8() {
        return "1.8".equals(JAVA_VERSION_STR);
    }

    public static boolean isJava9() {
        return "9".equals(JAVA_VERSION_STR);
    }

    /** 版本是否低于 9 */
    public static boolean isLessThanJava9() {
        return JAVA_VERSION < 9.0f;
    }

    public static boolean isGreaterThanJava7() {
        return JAVA_VERSION > 1.7f;
    }

    public static boolean isGreaterThanJava8() {
        return JAVA_VERSION > 1.8f;
    }

    /** 版本是否高于 11 */
    public static boolean isGreaterThanJava11() {
        return JAVA_VERSION > 11.0f;
    }
}
