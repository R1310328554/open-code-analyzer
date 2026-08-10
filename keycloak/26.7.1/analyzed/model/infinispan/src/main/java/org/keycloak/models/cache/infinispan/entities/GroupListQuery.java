package org.keycloak.models.cache.infinispan.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.models.RealmModel;

/**
 * 用户组列表查询结果的 Infinispan 缓存实体。
 * <p>
 * 实现 {@link GroupQuery}，按搜索键缓存组 ID 集合，
 * 支持合并多次查询结果以复用同一缓存条目。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class GroupListQuery extends AbstractRevisioned implements GroupQuery {
    /** 所属 realm 的唯一标识。 */
    private final String realm;
    /** 搜索键到组 ID 集合的映射。 */
    private final Map<String, Set<String>> searchKeys;

    /** 以单个搜索键与结果集合构造组列表查询缓存条目。 */
    public GroupListQuery(long revisioned, String id, RealmModel realm, String searchKey, Set<String> result) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.searchKeys = new HashMap<>();
        this.searchKeys.put(searchKey, result);
    }

    /** 合并先前查询结果并追加新搜索键结果。 */
    public GroupListQuery(long revisioned, String id, RealmModel realm, String searchKey, Set<String> result, GroupListQuery previous) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.searchKeys = new HashMap<>();
        this.searchKeys.putAll(previous.searchKeys);
        this.searchKeys.put(searchKey, result);
    }

    /** 以组 ID 集合构造组列表查询缓存条目。 */
    public GroupListQuery(long revisioned, String id, RealmModel realm, Set<String> ids) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.searchKeys = new HashMap<>();
        this.searchKeys.put(id, ids);
    }

    /** 返回所有搜索键下组 ID 的并集。 */
    @Override
    public Set<String> getGroups() {
        Collection<Set<String>> values = searchKeys.values();

        if (values.isEmpty()) {
            return Set.of();
        }

        return values.stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    /** 返回指定搜索键对应的组 ID 集合。 */
    public Set<String> getGroups(String searchKey) {
        return searchKeys.get(searchKey);
    }

    /** 返回所属 realm 的唯一标识。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return "GroupListQuery{" +
                "id='" + getId() + "'" +
                "realm='" + realm + '\'' +
                '}';
    }
}
