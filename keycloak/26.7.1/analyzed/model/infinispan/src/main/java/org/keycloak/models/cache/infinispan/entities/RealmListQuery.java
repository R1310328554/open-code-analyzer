package org.keycloak.models.cache.infinispan.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * Realm 列表查询结果的 Infinispan 缓存实体。
 * <p>
 * 实现 {@link RealmQuery}，缓存查询命中的 realm ID 集合。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RealmListQuery extends AbstractRevisioned implements RealmQuery {
    /** 查询命中的 realm ID 集合。 */
    private final Set<String> realms;

    /** 以单个 realm ID 构造列表查询缓存条目。 */
    public RealmListQuery(long revision, String id, String realm) {
        super(revision, id);
        realms = new HashSet<>();
        realms.add(realm);
    }
    /** 以 realm ID 集合构造列表查询缓存条目。 */
    public RealmListQuery(long revision, String id, Set<String> realms) {
        super(revision, id);
        this.realms = realms;
    }

    /** 返回查询命中的 realm ID 集合。 */
    @Override
    public Set<String> getRealms() {
        return realms;
    }
}
