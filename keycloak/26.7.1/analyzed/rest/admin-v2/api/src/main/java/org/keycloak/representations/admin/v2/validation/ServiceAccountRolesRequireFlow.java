package org.keycloak.representations.admin.v2.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 类级校验：仅当启用 {@code SERVICE_ACCOUNT} 登录流时，
 * 才允许设置 {@code serviceAccountRoles}。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ServiceAccountRolesRequireFlow {
    String message() default "serviceAccountRoles can only be set when SERVICE_ACCOUNT flow is enabled";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] affectedFieldNames() default {"serviceAccountRoles"};
}
