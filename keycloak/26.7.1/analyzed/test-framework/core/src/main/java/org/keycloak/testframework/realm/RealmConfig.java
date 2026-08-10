package org.keycloak.testframework.realm;

/**
 * 托管测试 Realm 的声明式配置接口。
 * <p>
 * 实现类通过 {@link #configure(RealmBuilder)} 向构建器追加或覆盖 Realm 属性。
 */
public interface RealmConfig {

    /**
     * 将本配置应用到 {@link RealmBuilder}。
     *
     * @param realm Realm 构建器
     * @return 配置后的构建器
     */
    RealmBuilder configure(RealmBuilder realm);

}
