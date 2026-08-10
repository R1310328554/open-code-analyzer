package org.keycloak.protocol.oidc.utils;

/**
 * OIDC {@code subject_types_supported} 主题标识类型。
 * <p>{@link #PUBLIC} 表示公开 subject；{@link #PAIRWISE} 表示按客户端隔离的对偶标识。</p>
 */
public enum SubjectType {
    /** 公开 subject（与用户名/sub 一致） */
    PUBLIC,
    /** 对偶 subject（按 sector identifier 派生） */
    PAIRWISE;

    /** 解析配置字符串，null 时默认 {@link #PUBLIC} @param subjectTypeStr 类型名 @return 枚举值 */
    public static SubjectType parse(String subjectTypeStr) {
        if (subjectTypeStr == null) {
            return PUBLIC;
        }
        return Enum.valueOf(SubjectType.class, subjectTypeStr.toUpperCase());
    }
}
