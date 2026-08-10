package org.keycloak.models.jpa;

/**
 * JPA 模型适配器标记接口：暴露底层 JPA 实体以便 Provider 层直接访问持久化对象。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface JpaModel<T> {
    /** 返回此适配器包装的 JPA 实体。 */
    T getEntity();
}
