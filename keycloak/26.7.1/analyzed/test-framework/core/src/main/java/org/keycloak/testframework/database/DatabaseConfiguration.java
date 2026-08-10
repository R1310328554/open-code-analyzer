package org.keycloak.testframework.database;

/**
 * 测试数据库运行时配置的数据载体。
 * <p>
 * 由 {@link DatabaseConfigBuilder} 组装，供 {@link TestDatabase#start(DatabaseConfiguration)} 使用。
 */
public final class DatabaseConfiguration {
    /** 启动时执行的初始化脚本路径。 */
    private String initScript;
    /** 数据库名称。 */
    private String database;
    /** 是否禁止复用数据库。 */
    private boolean preventReuse;

    /** 返回初始化脚本路径。 */
    public String getInitScript() {
        return initScript;
    }

    /** 设置初始化脚本路径。 */
    public void setInitScript(String initScript) {
        this.initScript = initScript;
    }

    /** 返回数据库名称。 */
    public String getDatabase() {
        return database;
    }

    /** 设置数据库名称。 */
    public void setDatabase(String database) {
        this.database = database;
    }

    /** 返回是否禁止复用数据库。 */
    public boolean isPreventReuse() {
        return preventReuse;
    }

    /** 设置是否禁止复用数据库。 */
    public void setPreventReuse(boolean preventReuse) {
        this.preventReuse = preventReuse;
    }
}
