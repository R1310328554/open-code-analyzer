package org.keycloak.models.cache.infinispan.authorization.stream;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.authorization.entities.InScope;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 按作用域 ID 过滤授权缓存条目的流式谓词。
 * <p>
 * 实现 {@link Predicate}，在批量失效时匹配实现了 {@link InScope} 的缓存实体。
 * 通过 ProtoStream 序列化以便在集群节点间传递查询条件。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@ProtoTypeId(Marshalling.IN_SCOPE_PREDICATE)
public class InScopePredicate implements Predicate<Map.Entry<String, Revisioned>> {
    /** 待匹配的目标作用域 ID。 */
    private final String scopeId;

    /** 私有构造，通过 {@link #create} 获取实例。 */
    private InScopePredicate(String scopeId) {
        this.scopeId = Objects.requireNonNull(scopeId);
    }

    /** ProtoStream 工厂方法，创建按作用域 ID 过滤的谓词。 */
    @ProtoFactory
    public static InScopePredicate create(String scopeId) {
        return new InScopePredicate(scopeId);
    }

    /** 返回待匹配的作用域 ID（ProtoStream 序列化字段）。 */
    @ProtoField(1)
    String getScopeId() {
        return scopeId;
    }

    /** 判断缓存条目是否关联到指定作用域。 */
    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        return entry.getValue() instanceof InScope inScope && scopeId.equals(inScope.getScopeId());
    }

}
