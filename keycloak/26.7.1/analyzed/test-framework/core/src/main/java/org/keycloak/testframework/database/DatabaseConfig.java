package org.keycloak.testframework.database;

/**
 * 托管测试数据库的声明式配置接口。
 * <p>
 * 实现类通过 {@link #configure(DatabaseConfigBuilder)} 向构建器追加或覆盖数据库选项。
 */
public interface DatabaseConfig {

    /**
     * 将本配置应用到 {@link DatabaseConfigBuilder}。
     *
     * @param database 数据库配置构建器
     * @return 配置后的构建器
     */
    DatabaseConfigBuilder configure(DatabaseConfigBuilder database);

}
