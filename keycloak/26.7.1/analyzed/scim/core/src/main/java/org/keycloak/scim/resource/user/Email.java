package org.keycloak.scim.resource.user;

import org.keycloak.scim.resource.common.MultiValuedAttribute;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SCIM 用户电子邮件多值属性（RFC 7643 第 4.1.2 节）。
 * <p>默认类型为 {@code work}，且设为主邮箱。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Email extends MultiValuedAttribute {

    /** 创建默认工作邮箱（{@code type=work}，{@code primary=true}）。 */
    public Email() {
        setType("work");
        setPrimary(true);
    }

    /**
     * 以给定地址创建主工作邮箱。
     *
     * @param email 邮箱地址
     */
    public Email(String email) {
        setValue(email);
        setPrimary(true);
        setType("work");
    }

    /**
     * 创建指定值、类型与主邮箱标志的邮箱属性。
     *
     * @param value 邮箱地址
     * @param type 邮箱类型（如 work、home）
     * @param primary 是否为主邮箱
     */
    public Email(String value, String type, Boolean primary) {
        setValue(value);
        setType(type);
        setPrimary(primary);
    }
}
