package org.keycloak.models.cache.infinispan.stream;

import java.util.Map;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.entities.InClient;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 缓存流过滤谓词：匹配 {@link InClient} 标记且 clientId 相等的条目。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@ProtoTypeId(Marshalling.IN_CLIENT_PREDICATE)
public class InClientPredicate implements Predicate<Map.Entry<String, Revisioned>> {
    /** 目标客户端 UUID。 */
    private String clientId;

    /** 创建可链式配置的谓词实例。 */
    public static InClientPredicate create() {
        return new InClientPredicate();
    }

    /** 限定要匹配的客户端 ID。 */
    public InClientPredicate client(String id) {
        clientId = id;
        return this;
    }

    @ProtoField(1)
    String getClientId() {
        return clientId;
    }

    void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** 判断缓存值是否为指定客户端的 InClient 索引条目。 */
    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        return entry.getValue() instanceof InClient inClient && clientId.equals(inClient.getClientId());
    }

}
