package org.keycloak.models.cache.infinispan.entities;

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.RealmModel;

/**
 * 客户端列表查询结果的 Infinispan 缓存实体。
 * <p>
 * 实现 {@link ClientQuery}，缓存某 realm 下客户端 ID 集合，
 * 用于按客户端集合进行缓存命中与失效。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientListQuery extends AbstractRevisioned implements ClientQuery {
    /** 查询命中的客户端 ID 集合。 */
    private final Set<String> clients;
    /** 所属 realm 的唯一标识。 */
    private final String realm;

    /** 以客户端 ID 集合构造列表查询缓存条目。 */
    public ClientListQuery(long revisioned, String id, RealmModel realm, Set<String> clients) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.clients = clients;
    }

    /** 以单个客户端 ID 构造列表查询缓存条目。 */
    public ClientListQuery(long revisioned, String id, RealmModel realm, String client) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.clients = new HashSet<>();
        this.clients.add(client);
    }

    /** 返回查询命中的客户端 ID 集合。 */
    @Override
    public Set<String> getClients() {
        return clients;
    }

    /** 返回所属 realm 的唯一标识。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return "ClientListQuery{" +
                "id='" + getId() + "'" +
                "realm='" + realm + '\'' +
                '}';
    }
}
