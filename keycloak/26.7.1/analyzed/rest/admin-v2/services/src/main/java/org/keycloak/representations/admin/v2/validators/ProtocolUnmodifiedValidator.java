package org.keycloak.representations.admin.v2.validators;

import java.util.Optional;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.keycloak.models.ClientModel;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;
import org.keycloak.representations.admin.v2.validation.ProtocolUnmodified;
import org.keycloak.validation.jakarta.ValidationContext;

/**
 * {@link org.keycloak.representations.admin.v2.validation.ProtocolUnmodified} 约束校验器：更新客户端时 protocol 须与持久化值一致，禁止修改。
 */
public class ProtocolUnmodifiedValidator implements ConstraintValidator<ProtocolUnmodified, BaseClientRepresentation> {

    @Override
    public boolean isValid(BaseClientRepresentation representation, ConstraintValidatorContext context) {
        ValidationContext validationContext = ValidationContext.unwrap(context);

        String persistedProtocol = Optional.ofNullable(validationContext.realm().getClientByClientId(representation.getClientId()))
                .map(ClientModel::getProtocol)
                .orElse(null);

        if (persistedProtocol == null || persistedProtocol.equals(representation.getProtocol())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("protocol")
                .addConstraintViolation();
        return false;
    }
}
