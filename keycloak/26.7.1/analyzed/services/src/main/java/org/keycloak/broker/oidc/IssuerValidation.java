package org.keycloak.broker.oidc;

import java.util.Map;
import java.util.Objects;

import org.keycloak.cache.AlternativeLookupProvider;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.util.Strings;
import org.keycloak.utils.KeycloakSessionUtil;

import static org.keycloak.common.util.UriUtils.checkUrl;
import static org.keycloak.models.IdentityProviderModel.ISSUER;

/**
 * Issuer 校验契约：确保 OIDC issuer URL 合法且在 realm 内唯一。
 * <p>JWT Authorization Grant 与 Federated Client Authentication 要求 issuer 不重复。</p>
 */
public interface IssuerValidation {

    /** @return 身份代理配置映射 */
    Map<String, String> getConfig();

    /** @return 当前 IdP 内部 ID（更新时排除自身） */
    String getInternalId();

    /** @return IdP 是否已启用 */
    boolean isEnabled();

    /** 校验 issuer 非空、SSL 合规且未被其他 IdP 占用。 */
    default void validateIssuer(RealmModel realm, IdentityProviderType type) {

        String issuer = getConfig().get(ISSUER);
        if (Strings.isEmpty(issuer)) {
            throw new IllegalArgumentException("Issuer is required");
        }

        checkUrl(realm.getSslRequired(), issuer, "Issuer");

        if (isEnabled()) {
            KeycloakSession session = KeycloakSessionUtil.getKeycloakSession();
            AlternativeLookupProvider lookupProvider = session.getProvider(AlternativeLookupProvider.class);

            if (lookupProvider != null) {
                IdentityProviderModel existingIdp = lookupProvider.lookupIdentityProviderFromIssuer(session, type, getConfig().get(ISSUER));
                if (existingIdp != null && (getInternalId() == null || !Objects.equals(existingIdp.getInternalId(), getInternalId()))) {
                    throw new IllegalArgumentException("Issuer URL already used for IDP '" + existingIdp.getAlias() + "', Issuer must be unique if the idp supports JWT Authorization Grant or Federated Client Authentication");
                }
            }
        }
    }
}
