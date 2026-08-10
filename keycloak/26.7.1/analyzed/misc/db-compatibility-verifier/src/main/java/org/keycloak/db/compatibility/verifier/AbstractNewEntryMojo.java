package org.keycloak.db.compatibility.verifier;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 向 supported/unsupported JSON 追加新变更集或 Migration 条目的 Mojo 基类。
 * 支持单条 {@link ChangeSet}、单条 {@link Migration} 或批量导入全部已知变更集。
 */
abstract class AbstractNewEntryMojo extends AbstractMojo {
    /** 为 true 时导入 classpath 上全部已知 ChangeSet（并排除 alternate 文件中已列出的项） */
    @Parameter(property = "db.verify.changeset.all", defaultValue = "false")
    boolean addAll;

    @Parameter(property = "db.verify.changeset.id")
    String id;

    @Parameter(property = "db.verify.changeset.author")
    String author;

    @Parameter(property = "db.verify.changeset.filename")
    String filename;

    /** 要追加的 Keycloak Migration 实现类全限定名 */
    @Parameter(property = "db.verify.migration.class")
    String migration;

    /**
     * 根据 {@link #addAll}、{@link #migration} 或单条 ChangeSet 参数，向目标 JSON 写入新条目。
     *
     * @param dest 要更新的主 JSON 文件
     * @param alternate 用于去重校验的对照 JSON（如 unsupported 列表）
     */
    protected void execute(File dest, File alternate) throws Exception {
        ClassLoader classLoader = classLoader();
        if (addAll) {
            addAllChangeSets(classLoader, dest, alternate);
        } else if (migration != null && !migration.isEmpty()) {
            addMigration(classLoader, new Migration(migration), dest, alternate);
        } else {
            checkValidChangeSetId(id, author, filename);
            ChangeSet changeSet = new ChangeSet(id, author, filename);
            addChangeSet(classLoader, changeSet, dest, alternate);
        }
    }

    /** 断言引用文件存在，否则抛出 {@link MojoExecutionException} */
    protected void checkFileExist(String ref, File file) throws MojoExecutionException {
        if (!file.exists()) {
            throw new MojoExecutionException("%s file does not exist".formatted(ref));
        }
    }

    /** 校验单条 ChangeSet 的 id、author、filename 均已配置且非空白 */
    protected void checkValidChangeSetId(String id, String author, String filename) throws MojoExecutionException {
        if (id == null || id.isBlank()) {
            throw new MojoExecutionException("ChangeSet id not set");
        }
        if (author == null || author.isBlank()) {
            throw new MojoExecutionException("ChangeSet author not set");
        }
        if (filename == null || filename.isBlank()) {
            throw new MojoExecutionException("ChangeSet filename not set");
        }
    }

    /** 发现全部已知 ChangeSet，减去 alternate 中的排除项后写回 dest */
    void addAllChangeSets(ClassLoader classLoader, File dest, File exclusions) throws IOException {
        // 从 Liquibase changelog XML 发现全部已知 ChangeSet
        ChangeLogXMLParser xmlParser = new ChangeLogXMLParser(classLoader);
        Set<ChangeSet> knownChangeSets = xmlParser.discoverAllChangeSets();

        // 读取需排除的条目并从已知集合中移除
        JsonParent excludedParent = objectMapper.readValue(exclusions, new TypeReference<>() {});
        Collection<ChangeSet> excludedChanges = excludedParent.changeSets();
        knownChangeSets.removeAll(excludedChanges);

        // 用更新后的 changeSets 覆盖 dest，保留原有 migrations 列表
        JsonParent parent = objectMapper.readValue(dest, new TypeReference<>() {});
        objectMapper.writeValue(dest, new JsonParent(knownChangeSets, parent.migrations()));
    }

    /** 校验 ChangeSet 存在于 classpath，且未在 alternate/dest 中重复后追加到 dest */
    void addChangeSet(ClassLoader classLoader, ChangeSet changeSet, File dest, File alternate) throws IOException, MojoExecutionException {
        // 从 Liquibase changelog 发现全部已知 ChangeSet
        ChangeLogXMLParser xmlParser = new ChangeLogXMLParser(classLoader);
        Set<ChangeSet> knownChangeSets = xmlParser.discoverAllChangeSets();

        // 不允许添加 classpath 上不存在的未知变更集
        if (!knownChangeSets.contains(changeSet)) {
            throw new MojoExecutionException("Unknown ChangeSet: " + changeSet);
        }

        JsonParent parent = objectMapper.readValue(alternate, new TypeReference<>() {});
        Set<ChangeSet> alternateChangeSets = new HashSet<>(parent.changeSets());
        if (alternateChangeSets.contains(changeSet)) {
            throw new MojoExecutionException("ChangeSet already defined in the %s file".formatted(alternate.getName()));
        }

        parent = objectMapper.readValue(dest, new TypeReference<>() {});
        Collection<ChangeSet> destChanges = parent.changeSets();
        if (!destChanges.contains(changeSet)) {
            // 尚未存在于 dest 时追加到 JSON 数组并写回文件
            destChanges.add(changeSet);
            objectMapper.writeValue(dest, parent);
        }
    }

    /** 校验 Migration 类存在于 classpath，且未重复后追加到 dest 的 migrations 列表 */
    void addMigration(ClassLoader classLoader, Migration migration, File dest, File alternate) throws IOException, MojoExecutionException {
        // 在 Migration 类所在包下发现全部已知 Migration
        String clazz = migration.clazz();
        int idx = clazz.lastIndexOf(".");
        String pkg = idx == -1 ? "" : clazz.substring(0, idx);

        KeycloakMigrationParser migrationParser = new KeycloakMigrationParser(classLoader, pkg);
        Set<Migration> knownMigrations = migrationParser.discoverAllMigrations();

        // 不允许添加未知的 Migration 类
        if (!knownMigrations.contains(migration)) {
            throw new MojoExecutionException("Unknown Migration: " + migration);
        }

        JsonParent parent = objectMapper.readValue(alternate, new TypeReference<>() {});
        Set<Migration> alternateMigrations = new HashSet<>(parent.migrations());
        if (alternateMigrations.contains(migration)) {
            throw new MojoExecutionException("Migration already defined in the %s file".formatted(alternate.getName()));
        }

        parent = objectMapper.readValue(dest, new TypeReference<>() {});
        Collection<Migration> destChanges = parent.migrations();
        if (!destChanges.contains(migration)) {
            // 尚未存在于 dest 时追加并写回 JSON
            destChanges.add(migration);
            objectMapper.writeValue(dest, parent);
        }
    }
}
