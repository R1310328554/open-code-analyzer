package org.keycloak.models.cache.infinispan.entities;

/**
 * 标记缓存实体隶属于某客户端的接口。
 * <p>
 * 继承 {@link InRealm}，在 realm 归属基础上额外暴露客户端 ID，
 * 供客户端作用域等子域缓存条目使用。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface InClient extends InRealm {
    /** 返回所属客户端的唯一标识。 */
    String getClientId();
}
