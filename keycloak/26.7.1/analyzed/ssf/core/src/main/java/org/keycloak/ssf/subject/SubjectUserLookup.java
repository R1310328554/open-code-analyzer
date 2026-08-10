package org.keycloak.ssf.subject;

import jakarta.ws.rs.core.UriInfo;

import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderQuery;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.Urls;
import org.keycloak.urls.UrlType;

import org.jboss.logging.Logger;

/**
 * 将 {@link SubjectId} 解析为 {@link UserModel} 的查找工具。
 * <p>支持 {@link EmailSubjectId}、{@link OpaqueSubjectId} 与 {@link IssuerSubjectId}；
 * 对 {@link IssuerSubjectId}，若 iss 匹配当前 realm 发行方则按本地用户 ID 查找，
 * 否则按外部 IdP 的 federated identity 链接查找。</p>
 */
public class SubjectUserLookup {

    protected static final Logger log = Logger.getLogger(SubjectUserLookup.class);

    /**
     * 根据主体类型在 realm 中查找对应用户。
     *
     * @param session 当前 Keycloak 会话
     * @param realm 目标 realm
     * @param subjectId 待查找的主体标识符
     * @return 匹配的用户，未找到或不支持的类型时返回 {@code null}
     */
    public static UserModel lookupUser(KeycloakSession session, RealmModel realm, SubjectId subjectId) {

        if (subjectId instanceof EmailSubjectId) {
            return getUserByEmail(session, realm, ((EmailSubjectId) subjectId).getEmail());
        }

        if (subjectId instanceof OpaqueSubjectId) {
            return getUserById(session, realm, ((OpaqueSubjectId) subjectId).getId());
        }

        if (subjectId instanceof IssuerSubjectId) {
            var issuerSubjectId = (IssuerSubjectId) subjectId;
            return getUserByIssuerSub(session, realm, issuerSubjectId.getIss(), issuerSubjectId.getSub());
        }

        log.warnf("Lookup failed for unknown subject id type. subjectId=%s", subjectId);
        return null;
    }

    private static UserModel getUserByIssuerSub(KeycloakSession session, RealmModel realm, String iss, String sub) {

        // iss = current realm issuer
        UriInfo frontendUriInfo = session.getContext().getUri(UrlType.FRONTEND);
        String realmIssuer = Urls.realmIssuer(frontendUriInfo.getBaseUri(), session.getContext().getRealm().getName());
        if (realmIssuer.equals(iss)) {
            // Find realm user
            return getUserById(session, realm, sub);
        }

        if (session.identityProviders().count() == 0) {
            log.warnf("No identity providers configured for realm. realm=%s", realm.getName());
            return null;
        }

        // Find identity provider whose issuer matches the iss claim
        IdentityProviderModel idp = session.identityProviders().getAllStream(IdentityProviderQuery.userAuthentication())
                .filter(i -> iss.equals(i.getConfig().get(IdentityProviderModel.ISSUER)))
                .findFirst()
                .orElse(null);

        if (idp == null) {
            log.warnf("No identity provider found for issuer. iss=%s", iss);
            return null;
        }

        // Lookup user by federated identity link: the sub claim is the user ID at the external IdP
        FederatedIdentityModel federatedIdentity = new FederatedIdentityModel(idp.getAlias(), sub, null);
        UserModel user = session.users().getUserByFederatedIdentity(realm, federatedIdentity);
        if (user == null) {
            log.debugf("No user found for federated identity. idpAlias=%s sub=%s", idp.getAlias(), sub);
        }
        return user;
    }

    private static UserModel getUserById(KeycloakSession session, RealmModel realm, String userId) {
        return session.users().getUserById(realm, userId);
    }

    private static UserModel getUserByEmail(KeycloakSession session, RealmModel realm, String email) {
        return session.users().getUserByEmail(realm, email);
    }
}
