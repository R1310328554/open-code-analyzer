package org.keycloak.testframework.infinispan;

/**
 * Infinispan 缓存部署模式枚举。
 * <p>
 * 用于测试框架选择嵌入式本地缓存或集群/外部 Infinispan 配置。
 */
public enum CacheType {

    /** 仅用于嵌入式部署的本地 Infinispan 缓存。 */
    LOCAL,

    /** 可用于嵌入式或外部部署的集群 Infinispan 缓存。 */
    ISPN;
}
