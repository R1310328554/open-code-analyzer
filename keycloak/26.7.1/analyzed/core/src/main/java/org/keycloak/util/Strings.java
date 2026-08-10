package org.keycloak.util;

/**
 * 字符串辅助工具类。
 */
public class Strings {

    /** 工具类，禁止实例化。 */
    private Strings() {
    }

    /**
     * 判断字符串是否为 null、空串或仅含空白字符。
     *
     * @param str 待检查字符串
     * @return 满足上述条件时返回 true
     */
    public static Boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

}
