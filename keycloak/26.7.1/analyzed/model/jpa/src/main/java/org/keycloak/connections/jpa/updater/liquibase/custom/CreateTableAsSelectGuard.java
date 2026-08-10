package org.keycloak.connections.jpa.updater.liquibase.custom;

import java.util.regex.Pattern;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.RawSqlStatement;

/**
 * 拦截 MySQL 上的 {@code CREATE TABLE ... AS SELECT} 原始 SQL。
 * <p>该语法与 Group Replication 不兼容（ERROR 3098）；仅需防护 {@link CustomKeycloakTask} 子类发出的 RawSqlStatement，XML changelog 无法表达此模式。</p>
 */
public class CreateTableAsSelectGuard extends AbstractSqlGenerator<RawSqlStatement> {

    /** 匹配 CTAS 语句的正则（不区分大小写）。 */
    private static final Pattern CTAS_PATTERN = Pattern.compile("(?i)CREATE\\s+TABLE\\s+\\S+\\s+AS\\s+SELECT");

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 100;
    }

    @Override
    public boolean supports(RawSqlStatement statement, Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    public ValidationErrors validate(RawSqlStatement statement, Database database, SqlGeneratorChain<RawSqlStatement> chain) {
        return new ValidationErrors();
    }

    /** 检测到 CTAS 时抛出运行时异常，提示改用 CREATE TABLE + INSERT INTO ... SELECT。 */
    @Override
    public Sql[] generateSql(RawSqlStatement statement, Database database, SqlGeneratorChain<RawSqlStatement> chain) {
        String sql = statement.getSql().trim();
        if (CTAS_PATTERN.matcher(sql).find()) {
            throw new RuntimeException("CREATE TABLE ... AS SELECT is incompatible with MySQL Group Replication "
                    + "(ERROR 3098). Use CREATE TABLE with inline PRIMARY KEY followed by INSERT INTO ... SELECT. "
                    + "Found: " + sql);
        }
        return chain.generateSql(statement, database);
    }
}
