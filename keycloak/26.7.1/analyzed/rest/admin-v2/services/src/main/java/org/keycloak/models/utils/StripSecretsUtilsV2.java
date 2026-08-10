package org.keycloak.models.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.admin.v2.OIDCClientRepresentation;
import org.keycloak.representations.admin.v2.SAMLClientRepresentation;
import org.keycloak.utils.StringUtil;

/**
 * Admin API v2 表示对象的密钥脱敏工具（OIDC secret、SAML 签名证书等）。
 */
public class StripSecretsUtilsV2 extends StripSecretsUtils {
    private static final Map<Class<?>, BiConsumer<KeycloakSession, Object>> REPRESENTATION_FORMATTER = new HashMap<>();

    static {
        REPRESENTATION_FORMATTER.put(OIDCClientRepresentation.class, (session, o) -> StripSecretsUtilsV2.stripOidcClient((OIDCClientRepresentation) o));
        REPRESENTATION_FORMATTER.put(SAMLClientRepresentation.class, (session, o) -> StripSecretsUtilsV2.stripSamlClient((SAMLClientRepresentation) o));
    }

    /** 按表示类型调用注册的脱敏处理器。 */
    public static <T> T stripSecrets(KeycloakSession session, T representation) {
        return stripSecrets(session, representation, REPRESENTATION_FORMATTER);
    }

    /** 掩码 OIDC 客户端 auth.secret（非 vault 值）。 */
    protected static OIDCClientRepresentation stripOidcClient(OIDCClientRepresentation rep) {
        Optional.ofNullable(rep.getAuth())
                .map(OIDCClientRepresentation.Auth::getSecret)
                .filter(StringUtil::isNotBlank)
                .ifPresent(secret -> rep.getAuth().setSecret(maskNonVaultValue(secret)));
        return rep;
    }

    /** 掩码 SAML 客户端 signingCertificate（非 vault 值）。 */
    protected static SAMLClientRepresentation stripSamlClient(SAMLClientRepresentation rep) {
        Optional.ofNullable(rep.getSigningCertificate())
                .filter(StringUtil::isNotBlank)
                .ifPresent(cert -> rep.setSigningCertificate(maskNonVaultValue(cert)));

        return rep;
    }

}
