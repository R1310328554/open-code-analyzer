package org.keycloak.models.cache.infinispan.stream;

import java.util.Map;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.entities.InIdentityProvider;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 缓存流过滤谓词：匹配 {@link InIdentityProvider} 且包含指定 IdP 别名的条目。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@ProtoTypeId(Marshalling.IN_IDENTITY_PROVIDER_PREDICATE)
public class InIdentityProviderPredicate implements Predicate<Map.Entry<String, Revisioned>> {
    /** 目标身份提供者内部 ID 或别名。 */
    private String id;

    /** 创建可链式配置的谓词实例。 */
    public static InIdentityProviderPredicate create() {
        return new InIdentityProviderPredicate();
    }

    /** 设置要匹配的身份提供者标识。 */
    public InIdentityProviderPredicate provider(String id) {
        this.id = id;
        return this;
    }

    @ProtoField(1)
    String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    /** 判断 InIdentityProvider 索引是否包含给定 IdP。 */
    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        return entry.getValue() instanceof InIdentityProvider provider && provider.contains(id);
    }

}
