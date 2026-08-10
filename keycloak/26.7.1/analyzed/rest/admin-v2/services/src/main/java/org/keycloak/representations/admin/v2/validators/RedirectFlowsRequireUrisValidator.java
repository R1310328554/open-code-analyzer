package org.keycloak.representations.admin.v2.validators;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.keycloak.representations.admin.v2.OIDCClientRepresentation;
import org.keycloak.representations.admin.v2.OIDCClientRepresentation.Flow;
import org.keycloak.representations.admin.v2.validation.RedirectFlowsRequireUris;

/**
 * {@link org.keycloak.representations.admin.v2.validation.RedirectFlowsRequireUris} 约束校验器：启用 {@code STANDARD} 或 {@code IMPLICIT} 登录流时，至少配置一条重定向 URI。
 */
public class RedirectFlowsRequireUrisValidator implements ConstraintValidator<RedirectFlowsRequireUris, OIDCClientRepresentation> {

    private static final Set<Flow> REDIRECT_FLOWS = Set.of(Flow.STANDARD, Flow.IMPLICIT);

    @Override
    public boolean isValid(OIDCClientRepresentation representation, ConstraintValidatorContext context) {
        Set<Flow> loginFlows = representation.getLoginFlows();
        if (loginFlows == null || loginFlows.isEmpty()) {
            return true;
        }
        Set<String> redirectUris = representation.getRedirectUris();
        if (redirectUris != null && !redirectUris.isEmpty()) {
            return true;
        }
        for (Flow flow : REDIRECT_FLOWS) {
            if (loginFlows.contains(flow)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                flow + " requires at least one redirect URI")
                        .addPropertyNode("redirectUris")
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
