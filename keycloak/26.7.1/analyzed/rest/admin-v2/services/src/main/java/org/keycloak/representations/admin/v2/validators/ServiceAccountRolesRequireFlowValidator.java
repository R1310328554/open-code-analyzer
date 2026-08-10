package org.keycloak.representations.admin.v2.validators;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.keycloak.representations.admin.v2.OIDCClientRepresentation;
import org.keycloak.representations.admin.v2.OIDCClientRepresentation.Flow;
import org.keycloak.representations.admin.v2.validation.ServiceAccountRolesRequireFlow;

/**
 * {@link org.keycloak.representations.admin.v2.validation.ServiceAccountRolesRequireFlow} 约束校验器：仅当启用 {@code SERVICE_ACCOUNT} 登录流时，才允许设置 serviceAccountRoles。
 */
public class ServiceAccountRolesRequireFlowValidator implements ConstraintValidator<ServiceAccountRolesRequireFlow, OIDCClientRepresentation> {

    @Override
    public boolean isValid(OIDCClientRepresentation representation, ConstraintValidatorContext context) {
        Set<String> serviceAccountRoles = representation.getServiceAccountRoles();
        if (serviceAccountRoles == null || serviceAccountRoles.isEmpty()) {
            return true;
        }
        Set<Flow> loginFlows = representation.getLoginFlows();
        if (loginFlows != null && loginFlows.contains(Flow.SERVICE_ACCOUNT)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "serviceAccountRoles can only be set when SERVICE_ACCOUNT flow is enabled")
                .addPropertyNode("serviceAccountRoles")
                .addConstraintViolation();
        return false;
    }
}
