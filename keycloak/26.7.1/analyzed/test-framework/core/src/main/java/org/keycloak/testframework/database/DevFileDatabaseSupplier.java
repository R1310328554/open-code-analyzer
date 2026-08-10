package org.keycloak.testframework.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.keycloak.testframework.util.TmpDir;

import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * 基于 H2 文件模式的开发/测试数据库 {@link AbstractDatabaseSupplier}。
 * <p>
 * 别名 {@code dev-file}，数据持久化到临时目录下的 H2 文件数据库。
 */
public class DevFileDatabaseSupplier extends AbstractDatabaseSupplier {

    /** H2 文件数据库所在目录。 */
    private static final File DB_DIR = Path.of(TmpDir.resolveTmpDir().getAbsolutePath(), "kc-test-framework", "h2").toFile();

    /** 是否在测试间复用数据库目录（默认不复用）。 */
    @ConfigProperty(name = "reuse", defaultValue = "false")
    boolean reuse;

    /** {@inheritDoc} 返回 {@code dev-file}。 */
    @Override
    public String getAlias() {
        return "dev-file";
    }

    /** {@inheritDoc} 返回 H2 文件模式 {@link TestDatabase} 实现。 */
    @Override
    TestDatabase getTestDatabase() {
        return new DevFileTestDatabase(reuse);
    }

    /** H2 文件模式测试数据库实现。 */
    private static class DevFileTestDatabase implements TestDatabase {

        private final boolean reuse;

        /** @param reuse 是否复用已有数据库目录 */
        public DevFileTestDatabase(boolean reuse) {
            this.reuse = reuse;
        }

        /** 启动前清理旧数据；不支持 init 脚本。 */
        @Override
        public void start(DatabaseConfiguration config) {
            deleteDatabase();
            if (config.getInitScript() != null) {
                throw new IllegalArgumentException("init script not supported, configure h2 properties via --db-url-properties");
            }
        }

        /** 停止时按配置清理数据库目录。 */
        @Override
        public void stop() {
            deleteDatabase();
        }

        /** 在非复用模式下删除 H2 数据库目录。 */
        private void deleteDatabase() {
            if (!reuse && DB_DIR.exists()) {
                try {
                    FileUtils.deleteDirectory(DB_DIR);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete directory: " + DB_DIR.getAbsolutePath(), e);
                }
            }
        }

        /** 返回 Keycloak {@code dev-file} 数据库连接配置。 */
        @Override
        public Map<String, String> serverConfig() {
            return Map.of(
                    "db", "dev-file",
                    "db-url", "jdbc:h2:file:" + DB_DIR + "/keycloak.db;DB_CLOSE_ON_EXIT=true;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=0"
                );
        }
    }

}
