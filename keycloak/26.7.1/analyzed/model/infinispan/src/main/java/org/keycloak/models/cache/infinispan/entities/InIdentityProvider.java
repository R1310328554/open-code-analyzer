package org.keycloak.models.cache.infinispan.entities;


/**
 * 身份提供者（Identity Provider）相关缓存条目的标记接口。
 * <p>
 * 继承 {@link Revisioned}，提供按提供者 ID 判断是否包含于缓存集合的能力。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface InIdentityProvider extends Revisioned {
    /** 判断缓存集合是否包含指定身份提供者 ID。 */
    boolean contains(String providerId);
}
