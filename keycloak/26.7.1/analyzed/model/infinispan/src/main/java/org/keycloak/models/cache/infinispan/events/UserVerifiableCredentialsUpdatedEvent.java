
package org.keycloak.models.cache.infinispan.events;

import java.util.Set;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.UserCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 用户可验证凭证（Verifiable Credential）缓存条目的失效事件。
 * <p>
 * 当用户的可验证凭证发生增删改时发布，通知 {@link UserCacheManager}
 * 失效该用户相关的 VC 缓存条目。
 */
@ProtoTypeId(Marshalling.USER_VERIFIABLE_CREDENTIALS_UPDATED_EVENT)
public class UserVerifiableCredentialsUpdatedEvent extends InvalidationEvent implements UserCacheInvalidationEvent {

    /** 构造可验证凭证更新事件。 */
    private UserVerifiableCredentialsUpdatedEvent(String id) {
        super(id);
    }

    /** 创建指定用户 ID 的可验证凭证更新事件。 */
    @ProtoFactory
    public static UserVerifiableCredentialsUpdatedEvent create(String id) {
        return new UserVerifiableCredentialsUpdatedEvent(id);
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return String.format("UserVerifiableCredentialsUpdatedEvent [ userId=%s ]", getId());
    }

    /** 将可验证凭证更新引发的失效键加入集合。 */
    @Override
    public void addInvalidations(UserCacheManager userCache, Set<String> invalidations) {
        userCache.verifiableCredentialsInvalidation(getId(), invalidations);
    }

}
