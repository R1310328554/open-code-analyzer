package org.keycloak.representations.admin.v2.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 按 Keycloak 重定向 URI 规则校验的类级约束注解。
 * <p>
 * 校验规则：
 * <ul>
 *   <li>通配符 (*) 仅允许出现在路径末尾</li>
 *   <li>通配符前必须有斜杠 (/)</li>
 *   <li>通配符后不能跟查询参数或片段</li>
 *   <li>仅允许一个通配符</li>
 *   <li>未设置根 URL 时，重定向 URI 必须为绝对地址（含 scheme）</li>
 *   <li>已设置根 URL 时，允许相对路径</li>
 *   <li>特殊值 "*"（全匹配）、"+" 与 "-"（登出后）始终有效</li>
 * </ul>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidRedirectUris {
    String message() default "Invalid redirect URI";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] affectedFieldNames() default { "redirectUris" };
}
