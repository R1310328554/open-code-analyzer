package org.keycloak.representations.admin.v2.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.keycloak.representations.admin.v2.OIDCClientRepresentation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 校验约束：当 {@link OIDCClientRepresentation.Auth#getMethod()} 为（JWT）客户端密钥类型时，
 * 要求 {@link OIDCClientRepresentation.Auth#getSecret()} 非空。
 */
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = {})
public @interface ClientSecretNotBlank {

    String message() default "Client secret must not be blank";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

     String[] affectedFieldNames();
}
