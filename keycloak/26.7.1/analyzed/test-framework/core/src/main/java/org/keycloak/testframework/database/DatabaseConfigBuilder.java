package org.keycloak.testframework.database;

/**
 * 测试数据库配置的流式构建器。
 * <p>
 * 通过 {@link #create()} 创建实例，链式设置选项后调用 {@link #build()} 得到不可变配置快照。
 */
public class DatabaseConfigBuilder {

    /** 底层配置对象。 */
    DatabaseConfiguration rep;

    private DatabaseConfigBuilder(DatabaseConfiguration rep) {
        this.rep = rep;
    }

    /** 创建默认的 {@link DatabaseConfigBuilder} 实例。 */
    public static DatabaseConfigBuilder create() {
        DatabaseConfiguration rep = new DatabaseConfiguration();
        return new DatabaseConfigBuilder(rep);
    }

    /**
     * 配置数据库启动时执行的初始化脚本。
     *
     * @param initScript 类路径上的初始化脚本路径
     * @return 当前构建器
     */
    public DatabaseConfigBuilder initScript(String initScript) {
        rep.setInitScript(initScript);
        return this;
    }

    /**
     * 设置数据库名称，默认为 <code>keycloak</code>。
     *
     * @param database 要使用的数据库名
     * @return 当前构建器
     */
    public DatabaseConfigBuilder database(String database) {
        rep.setDatabase(database);
        return this;
    }

    /**
     * 是否禁止复用同一数据库实例。
     *
     * @param preventReuse 为 <code>true</code> 时每次测试使用独立数据库
     * @return 当前构建器
     */
    public DatabaseConfigBuilder preventReuse(boolean preventReuse) {
        rep.setPreventReuse(preventReuse);
        return this;
    }

    /** 构建并返回 {@link DatabaseConfiguration} 配置快照。 */
    public DatabaseConfiguration build() {
        return rep;
    }
}
