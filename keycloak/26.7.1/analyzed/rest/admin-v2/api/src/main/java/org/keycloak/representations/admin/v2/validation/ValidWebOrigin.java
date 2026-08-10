package org.keycloak.representations.admin.v2.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 校验 Web Origin 的约束注解：须为 {@code scheme://host[:port]} 格式，或 {@code +} 从重定向 URI 推导，或 {@code *} 允许全部来源。
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidWebOrigin {
    /** 校验失败时的默认消息模板。 */
    String message() default "must be a valid web origin (scheme://host[:port]), or '+' to derive from redirect URIs, or '*' to allow all";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
