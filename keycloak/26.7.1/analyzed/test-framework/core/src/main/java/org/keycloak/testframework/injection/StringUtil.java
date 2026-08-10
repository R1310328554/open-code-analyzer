package org.keycloak.testframework.injection;

/** 测试框架注入模块的字符串辅助工具。 */
public class StringUtil {

    /**
     * 将空字符串规范为 {@code null}，便于 ref 比较与存储。
     *
     * @param s 输入字符串
     * @return 空字符串时返回 {@code null}，否则原值
     */
    public static String convertEmptyToNull(String s) {
        return s != null && s.isEmpty() ? null : s;
    }

}
