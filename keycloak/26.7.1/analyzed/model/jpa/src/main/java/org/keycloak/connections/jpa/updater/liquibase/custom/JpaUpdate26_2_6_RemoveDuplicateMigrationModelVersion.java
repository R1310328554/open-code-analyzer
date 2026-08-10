package org.keycloak.connections.jpa.updater.liquibase.custom;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import liquibase.exception.CustomChangeException;
import liquibase.statement.core.DeleteStatement;
import liquibase.structure.core.Column;

/**
 * 清理 {@code MIGRATION_MODEL} 表中 {@code VERSION} 重复的记录。
 * <p>同一版本号出现多行时，保留 {@code UPDATE_TIME} 较新（或 ID 较大）的一条，删除其余。</p>
 * See: <a href="https://github.com/keycloak/keycloak/issues/39866">keycloak#39866</a>
 */
public class JpaUpdate26_2_6_RemoveDuplicateMigrationModelVersion extends CustomKeycloakTask {

    private final static String MIGRATION_MODEL_TABLE = "MIGRATION_MODEL";

    @Override
    protected String getTaskId() {
        return "Delete duplicated records for DB version in MIGRATION_MODEL table";
    }

    /** 找出应删除的重复 VERSION 行 ID，并按 20 条一批生成 DELETE。 */
    @Override
    protected void generateStatementsImpl() throws CustomChangeException {
        Set<String> idsToDelete = new HashSet<>();

        final String tableName = getTableName(MIGRATION_MODEL_TABLE);
        final String colId = database.correctObjectName("ID", Column.class);
        final String colVersion = database.correctObjectName("VERSION", Column.class);
        final String colUpdateTime = database.correctObjectName("UPDATE_TIME", Column.class);

        //noinspection SqlSourceToSinkFlow
        try (PreparedStatement ps = connection.prepareStatement(getOlderDuplicatedRecords(tableName, colId, colVersion, colUpdateTime))) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                idsToDelete.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            throw new CustomChangeException(getTaskId() + ": Failed to detect duplicate MIGRATION_MODEL rows", e);
        }

        AtomicInteger i = new AtomicInteger();
        idsToDelete.stream()
                .collect(Collectors.groupingByConcurrent(id -> i.getAndIncrement() / 20, Collectors.toList())) // 每批最多 20 条
                .values().stream()
                .map(ids -> new DeleteStatement(null, null, MIGRATION_MODEL_TABLE)
                        .setWhere(":name IN (" + ids.stream().map(id -> "?").collect(Collectors.joining(",")) + ")")
                        .addWhereColumnName(colId)
                        .addWhereParameters(ids.toArray())
                )
                .forEach(statements::add);
    }

    /**
     * 查询 VERSION 重复组中应删除的较旧记录 ID。
     * <p>
     * 若存在 VERSION 重复，选择规则：
     * <p>
     * - UPDATE_TIME 不同：保留较新的记录
     * <p>
     * - UPDATE_TIME 相同：按 ID 较大者保留（等价于随机择一）
     */
    private String getOlderDuplicatedRecords(String tableName, String colId, String colVersion, String colUpdateTime) {
        return """
                SELECT m1.%s
                FROM %s m1
                WHERE EXISTS (
                    SELECT m2.%s
                    FROM %s m2
                    WHERE m2.%s = m1.%s
                    AND (
                        m2.%s > m1.%s
                        OR (m2.%s = m1.%s AND m2.%s > m1.%s)
                    )
                )
                """.formatted(
                colId,                  // SELECT m1.%s         => SELECT m1.ID
                tableName,              // FROM %s m1           => FROM MIGRATION_MODEL m1
                colId,                  // SELECT m2.%s         => SELECT m2.ID
                tableName,              // FROM %s              => FROM MIGRATION_MODEL m2
                colVersion, colVersion, // WHERE m2.%s = m1.%s  => WHERE m2.VERSION = m1.VERSION
                colUpdateTime, colUpdateTime, // m2.%s > m1.%s  => m2.UPDATE_TIME > m1.UPDATE_TIME
                // OR (m2.%s = m1.%s AND m2.%s > m1.%s)         => OR (m2.UPDATE_TIME = m1.UPDATE_TIME AND m2.ID > m1.ID)
                colUpdateTime, colUpdateTime, colId, colId);
    }

}
