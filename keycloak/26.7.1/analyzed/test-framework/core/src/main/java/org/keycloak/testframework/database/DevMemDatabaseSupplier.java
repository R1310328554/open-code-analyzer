package org.keycloak.testframework.database;

import java.util.Map;

/**
 * 基于 H2 内存模式的开发/测试数据库 {@link AbstractDatabaseSupplier}。
 * <p>
 * 别名 {@code dev-mem}，进程内内存数据库，适合快速隔离测试。
 */
public class DevMemDatabaseSupplier extends AbstractDatabaseSupplier {

    /** {@inheritDoc} 返回 {@code dev-mem}。 */
    @Override
    public String getAlias() {
        return "dev-mem";
    }

    /** {@inheritDoc} 返回 H2 内存模式 {@link TestDatabase} 实现。 */
    @Override
    TestDatabase getTestDatabase() {
        return new DevMemTestDatabase();
    }

    /** H2 内存模式测试数据库实现。 */
    private static class DevMemTestDatabase implements TestDatabase {

        /** 校验配置；不支持 init 脚本。 */
        @Override
        public void start(DatabaseConfiguration config) {
            if (config.getInitScript() != null)
                throw new IllegalArgumentException("init script not supported, configure h2 properties via --db-url-properties");
        }

        /** 内存数据库无需显式停止操作。 */
        @Override
        public void stop() {
        }

        /** 返回 Keycloak {@code dev-mem} 数据库配置。 */
        @Override
        public Map<String, String> serverConfig() {
            return Map.of("db", "dev-mem");
        }
    }

}
