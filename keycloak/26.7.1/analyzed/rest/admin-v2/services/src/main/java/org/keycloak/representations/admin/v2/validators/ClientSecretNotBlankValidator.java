package org.keycloak.representations.admin.v2.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.keycloak.authentication.authenticators.client.ClientIdAndSecretAuthenticator;
import org.keycloak.authentication.authenticators.client.JWTClientSecretAuthenticator;
import org.keycloak.representations.admin.v2.OIDCClientRepresentation;
import org.keycloak.representations.admin.v2.validation.ClientSecretNotBlank;
import org.keycloak.utils.StringUtil;

/**
 * 当 {@link OIDCClientRepresentation.Auth#getMethod()} 为客户端密钥认证方式时，校验 {@link OIDCClientRepresentation.Auth#getSecret()} 非空。
 */
public class ClientSecretNotBlankValidator implements ConstraintValidator<ClientSecretNotBlank, OIDCClientRepresentation.Auth> {

    @Override
    public boolean isValid(OIDCClientRepresentation.Auth auth, ConstraintValidatorContext context) {
        if (auth != null && isClientSecret(auth.getMethod())) {
            if (StringUtil.isBlank(auth.getSecret())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("must not be blank when authentication method requires a secret")
                        .addPropertyNode("secret")
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }

    /** 判断认证方法是否为需要 client secret 的类型。 */
    public static boolean isClientSecret(String method) {
        return ClientIdAndSecretAuthenticator.PROVIDER_ID.equals(method)
                || JWTClientSecretAuthenticator.PROVIDER_ID.equals(method);
    }
}
