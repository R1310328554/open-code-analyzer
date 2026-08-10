package org.keycloak.util;

/**
 * 布尔值辅助工具，统一处理可能为 null 的三值布尔逻辑。
 */
public class Booleans {

    /**
     * 判断布尔值是否为 true；null 视为 false。
     *
     * @param b 待检查的布尔值
     * @return 非 null 且为 true 时返回 true
     */
    public static Boolean isTrue(Boolean b) {
        return b != null && b;
    }

    /**
     * 判断布尔值是否为 false；null 视为 false。
     *
     * @param b 待检查的布尔值
     * @return 为 null 或为 false 时返回 true
     */
    public static Boolean isFalse(Boolean b) {
        return b == null || !b;
    }

    /**
     * 比较两个可能为 null 的布尔值；null 均视为 false。
     *
     * @param a 第一个布尔值
     * @param b 第二个布尔值
     * @return 两者归一化后相等则返回 true
     */
    public static Boolean equals(Boolean a, Boolean b) {
        a = a != null && a;
        b = b != null && b;
        return a.equals(b);
    }

}
