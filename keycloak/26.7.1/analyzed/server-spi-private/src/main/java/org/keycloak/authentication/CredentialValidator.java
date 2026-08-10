package org.keycloak.authentication;

import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 凭证校验辅助接口，封装 {@link CredentialProvider} 访问与用户已存凭证查询。
 * @param <T> 具体 {@link CredentialProvider} 实现类型
 */
public interface CredentialValidator<T extends CredentialProvider> {
    /** 从会话获取凭证提供者实例。 */
    T getCredentialProvider(KeycloakSession session);
    /** 返回用户已存储的、与本提供者类型匹配的凭证列表。 */
    default List<CredentialModel> getCredentials(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.credentialManager().getStoredCredentialsByTypeStream(getCredentialProvider(session).getType())
                .collect(Collectors.toList());
    }
    /** 返回凭证类型字符串（委托 {@link CredentialProvider#getType}）。 */
    default String getType(KeycloakSession session) {
        return getCredentialProvider(session).getType();
    }
}
