package org.keycloak.models.cache.infinispan.authorization.stream;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.authorization.entities.InResourceServer;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 按资源服务器 ID 过滤授权缓存条目的流式谓词。
 * <p>
 * 实现 {@link Predicate}，在批量失效时匹配实现了 {@link InResourceServer} 的缓存实体。
 * 通过 ProtoStream 序列化以便在集群节点间传递查询条件。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@ProtoTypeId(Marshalling.IN_RESOURCE_SERVER_PREDICATE)
public class InResourceServerPredicate implements Predicate<Map.Entry<String, Revisioned>> {
    /** 待匹配的目标资源服务器 ID。 */
    private final String serverId;

    /** 私有构造，通过 {@link #create} 获取实例。 */
    private InResourceServerPredicate(String serverId) {
        this.serverId = Objects.requireNonNull(serverId);
    }

    /** ProtoStream 工厂方法，创建按资源服务器 ID 过滤的谓词。 */
    @ProtoFactory
    public static InResourceServerPredicate create(String serverId) {
        return new InResourceServerPredicate(serverId);
    }

    /** 返回待匹配的资源服务器 ID（ProtoStream 序列化字段）。 */
    @ProtoField(1)
    String getServerId() {
        return serverId;
    }

    /** 判断缓存条目是否属于指定资源服务器。 */
    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        return entry.getValue() instanceof InResourceServer inResourceServer && serverId.equals(inResourceServer.getResourceServerId());
    }

}
