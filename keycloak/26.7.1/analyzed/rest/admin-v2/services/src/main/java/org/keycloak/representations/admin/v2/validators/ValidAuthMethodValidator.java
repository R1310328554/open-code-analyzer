package org.keycloak.representations.admin.v2.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.keycloak.authentication.ClientAuthenticator;
import org.keycloak.representations.admin.v2.validation.ValidAuthMethod;
import org.keycloak.validation.jakarta.ValidationContext;

/**
 * {@link org.keycloak.representations.admin.v2.validation.ValidAuthMethod} 约束校验器：认证方法须为已注册的 {@link org.keycloak.authentication.ClientAuthenticator} provider ID。
 */
public class ValidAuthMethodValidator implements ConstraintValidator<ValidAuthMethod, String> {

    @Override
    public boolean isValid(String authMethod, ConstraintValidatorContext context) {
        if (authMethod == null || authMethod.isBlank()) {
            return true;
        }
        ValidationContext validationContext = ValidationContext.unwrap(context);
        return validationContext.session().getKeycloakSessionFactory()
                .getProviderFactory(ClientAuthenticator.class, authMethod) != null;
    }
}
