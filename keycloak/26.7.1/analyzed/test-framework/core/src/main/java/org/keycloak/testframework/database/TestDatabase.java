package org.keycloak.testframework.database;

import java.util.Map;

/**
 * 测试框架托管的数据库生命周期抽象。
 * <p>
 * 实现类负责启动/停止数据库（或等价准备），并向 Keycloak 服务器提供 {@link #serverConfig()} 选项。
 */
public interface TestDatabase {

    /**
     * 按给定配置启动或准备测试数据库。
     *
     * @param databaseConfiguration 数据库配置
     */
    void start(DatabaseConfiguration databaseConfiguration);

    /** 停止测试数据库并释放资源。 */
    void stop();

    /**
     * 返回应注入 Keycloak 服务器的数据库相关 CLI/配置项。
     *
     * @return 键值对形式的服务器配置
     */
    Map<String, String> serverConfig();
}
