package org.keycloak.representations.admin.v2.validators;

import java.util.Set;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.keycloak.representations.admin.v2.BaseClientRepresentation;
import org.keycloak.representations.admin.v2.validation.ValidRedirectUris;

/**
 * {@link org.keycloak.representations.admin.v2.validation.ValidRedirectUris} 约束校验器：按 Keycloak 重定向 URI 规则校验客户端 redirectUris。
 * <p>
 * 上下文感知：已设置根 URL 时允许相对路径；未设置时仅接受绝对 URI。
 */
public class ValidRedirectUrisValidator implements ConstraintValidator<ValidRedirectUris, BaseClientRepresentation> {

    private static final Pattern SCHEME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://");

    @Override
    public boolean isValid(BaseClientRepresentation representation, ConstraintValidatorContext context) {
        Set<String> redirectUris = representation.getRedirectUris();
        if (redirectUris == null || redirectUris.isEmpty()) {
            return true;
        }

        boolean hasRootUrl = representation.getAppUrl() != null && !representation.getAppUrl().isBlank();
        boolean allValid = true;

        for (String uri : redirectUris) {
            String error = validateRedirectUri(uri, hasRootUrl);
            if (error != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(error)
                        .addPropertyNode("redirectUris")
                        .addConstraintViolation();
                allValid = false;
            }
        }

        return allValid;
    }

    /**
     * 校验单条重定向 URI。
     *
     * @param uri the redirect URI to validate
     * @param hasRootUrl whether the client has a root URL configured
     * @return error message if invalid, null if valid
     */
    public static String validateRedirectUri(String uri, boolean hasRootUrl) {
        if (uri == null || uri.isBlank()) {
            return "Redirect URI cannot be empty";
        }

        String trimmedUri = uri.trim();

        // 特殊值始终有效
        if ("*".equals(trimmedUri) || "+".equals(trimmedUri) || "-".equals(trimmedUri)) {
            return null;
        }

        boolean hasScheme = SCHEME_PATTERN.matcher(trimmedUri).find();

        // 未设置根 URL 时仅允许绝对 URI
        if (!hasRootUrl && !hasScheme) {
            return "Redirect URI must be an absolute URI (include scheme like https://) when Root URL is not set";
        }

        // 校验通配符规则
        if (trimmedUri.contains("*")) {
            return validateWildcard(trimmedUri);
        }

        return null;
    }

    private static String validateWildcard(String uri) {
        // 通配符须位于 URI 末尾
        if (!uri.endsWith("*")) {
            return "Wildcard (*) must be at the end of the URI";
        }

        // 仅允许一个通配符
        long wildcardCount = uri.chars().filter(ch -> ch == '*').count();
        if (wildcardCount > 1) {
            return "Only one wildcard (*) is allowed at the end of the URI";
        }

        // 通配符前须有 "/"（如 "/*"、"/path/*"）
        int wildcardIndex = uri.lastIndexOf('*');
        if (wildcardIndex > 0 && uri.charAt(wildcardIndex - 1) != '/') {
            return "Wildcard (*) must be preceded by a slash (/)";
        }

        // 含通配符时不允许查询参数
        if (uri.contains("?")) {
            return "Wildcard URIs cannot contain query parameters";
        }

        // 含通配符时不允许片段（#）
        if (uri.contains("#")) {
            return "Wildcard URIs cannot contain fragments";
        }

        return null;
    }

    /**
     * 判断单条重定向 URI 是否有效。
     *
     * @param uri the redirect URI to check
     * @param hasRootUrl whether the client has a root URL configured
     * @return true if valid, false otherwise
     */
    public static boolean isValidRedirectUri(String uri, boolean hasRootUrl) {
        return validateRedirectUri(uri, hasRootUrl) == null;
    }
}
