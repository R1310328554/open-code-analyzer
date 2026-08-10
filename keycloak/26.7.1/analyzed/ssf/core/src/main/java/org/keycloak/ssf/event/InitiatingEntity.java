package org.keycloak.ssf.event;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 事件发起实体枚举，标识触发 SSF 安全事件的主体类型。
 * <p>序列化为 JSON 时使用 {@link #getCode()} 返回的小写字符串。</p>
 */
public enum InitiatingEntity {
    /** 管理员发起。 */ ADMIN("admin"),
    /** 用户发起。 */ USER("user"),
    /** 策略规则发起。 */ POLICY("policy"),
    /** 系统自动发起。 */ SYSTEM("system"),
    ;

    private final String code;

    InitiatingEntity(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
