package org.keycloak.representations.admin.v2.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 字段级校验：SAML XML 签名规范化方法须为合法 URI
 *（参见 {@code javax.xml.crypto.dsig.CanonicalizationMethod} 常量）。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidCanonicalizationMethod {
    String message() default "must be a valid XML canonicalization method URI (see javax.xml.crypto.dsig.CanonicalizationMethod constants)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
