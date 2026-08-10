package org.keycloak.representations.admin.v2.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 类级校验：启用 {@code STANDARD} 或 {@code IMPLICIT} 登录流时，
 * 须至少配置一条重定向 URI。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface RedirectFlowsRequireUris {
    String message() default "STANDARD and IMPLICIT flows require at least one redirect URI";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    // TODO: OASModelFilter 支持继承属性的类级描述后，将 affectedFieldNames 加入 "redirectUris"
    String[] affectedFieldNames() default {"loginFlows"};
}
