package org.keycloak.models.cache.infinispan.stream;

import java.util.Map;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.entities.GroupListQuery;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 缓存流过滤谓词：匹配指定 realm 下的 {@link GroupListQuery} 条目。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@ProtoTypeId(Marshalling.GROUP_LIST_PREDICATE)
public class GroupListPredicate implements Predicate<Map.Entry<String, Revisioned>> {
    /** 目标 realm 标识。 */
    private String realm;

    /** 创建可链式配置的谓词实例。 */
    public static GroupListPredicate create() {
        return new GroupListPredicate();
    }

    /** 限定要匹配的 realm。 */
    public GroupListPredicate realm(String realm) {
        this.realm = realm;
        return this;
    }

    @ProtoField(1)
    String getRealm() {
        return realm;
    }

    void setRealm(String realm) {
        this.realm = realm;
    }

    /** 判断缓存值是否为指定 realm 的组列表查询实体。 */
    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        return entry.getValue() instanceof GroupListQuery groupList && groupList.getRealm().equals(realm);
    }

}
