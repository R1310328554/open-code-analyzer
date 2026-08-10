package org.keycloak.models.cache.infinispan.stream;

import java.util.Map;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.entities.InRealm;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 缓存流过滤谓词：匹配 {@link InRealm} 标记且 realm ID 相等的条目。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@ProtoTypeId(Marshalling.IN_REALM_PREDICATE)
public class InRealmPredicate implements Predicate<Map.Entry<String, Revisioned>> {
    /** 目标 realm 标识。 */
    private String realm;

    /** 创建可链式配置的谓词实例。 */
    public static InRealmPredicate create() {
        return new InRealmPredicate();
    }

    /** 限定要匹配的 realm ID。 */
    public InRealmPredicate realm(String id) {
        realm = id;
        return this;
    }

    @ProtoField(1)
    String getRealm() {
        return realm;
    }

    void setRealm(String realm) {
        this.realm = realm;
    }

    /** 判断缓存值是否为指定 realm 的 InRealm 索引条目。 */
    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        return entry.getValue() instanceof InRealm inRealm && realm.equals(inRealm.getRealm());

    }

}
