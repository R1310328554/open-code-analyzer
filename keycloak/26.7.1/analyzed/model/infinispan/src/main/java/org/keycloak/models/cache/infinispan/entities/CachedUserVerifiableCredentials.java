
package org.keycloak.models.cache.infinispan.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserVerifiableCredentialModel;

/**
 * 用户可验证凭证（Verifiable Credential）列表的 Infinispan 缓存实体。
 * <p>
 * 实现 {@link InRealm}，将某用户下全部 VC 快照为 {@link CachedUserVerifiableCredential} 列表，
 * 供集群缓存层按 realm 与 revision 进行失效判定。
 */
public class CachedUserVerifiableCredentials extends AbstractRevisioned implements InRealm {
    /** 该用户下已缓存的可验证凭证列表。 */
    private final List<CachedUserVerifiableCredential> credentials;
    /** 所属 realm 的唯一标识。 */
    private final String realmId;

    /** 从 realm 与凭证模型列表构造 VC 列表缓存实体。 */
    public CachedUserVerifiableCredentials(long revision, String id, RealmModel realm, List<UserVerifiableCredentialModel> credentials) {
        super(revision, id);
        this.realmId = realm.getId();
        this.credentials = credentials != null
            ? credentials.stream()
                .map(CachedUserVerifiableCredential::new)
                .collect(Collectors.toCollection(ArrayList::new))
            : new ArrayList<>();
    }

    /** 返回该用户下缓存的可验证凭证列表。 */
    public List<CachedUserVerifiableCredential> getCredentials() {
        return credentials;
    }

    /** 返回所属 realm 的唯一标识。 */
    @Override
    public String getRealm() {
        return realmId;
    }
}
