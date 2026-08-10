package org.keycloak.testframework.realm;

/**
 * 托管测试用户的声明式配置接口。
 * <p>
 * 实现类通过 {@link #configure(UserBuilder)} 向构建器追加或覆盖用户属性。
 */
public interface UserConfig {

    /**
     * 将本配置应用到 {@link UserBuilder}。
     *
     * @param user 用户构建器
     * @return 配置后的构建器
     */
    UserBuilder configure(UserBuilder user);

}
