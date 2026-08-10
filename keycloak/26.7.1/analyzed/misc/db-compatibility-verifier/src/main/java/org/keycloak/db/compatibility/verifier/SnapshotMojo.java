package org.keycloak.db.compatibility.verifier;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven 目标 {@code snapshot}：生成 db 兼容性校验用的 supported/unsupported JSON 基线快照。
 * <p>
 * 扫描类路径上全部 Liquibase {@link ChangeSet} 与配置包内的 {@link Migration} 实现，
 * 写入 supported 文件；unsupported 文件初始化为空集合。
 */
@Mojo(name = "snapshot")
public class SnapshotMojo extends AbstractMojo {

    /** 迁移类扫描包名，对应属性 {@code db.verify.migration.package}。 */
    @Parameter(property = "db.verify.migration.package")
    String migrationsPackage;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping execution");
            return;
        }

        try {
            File root = project.getBasedir();
            File sFile = new File(root, supportedFile);
            File uFile = new File(root, unsupportedFile);

            ClassLoader classLoader = classLoader();
            createSnapshot(classLoader, sFile, uFile, migrationsPackage);
        } catch (Exception e) {
            throw new MojoExecutionException("Error creating ChangeSet snapshot", e);
        }
    }

    /**
     * 收集当前模块中的变更集与迁移类，并写入 JSON 快照文件。
     *
     * @param classLoader        项目类加载器
     * @param sFile              supported 列表路径
     * @param uFile              unsupported 列表路径
     * @param migrationsPackage  迁移类包名
     */
    void createSnapshot(ClassLoader classLoader, File sFile, File uFile, String migrationsPackage) throws IOException {
        // 记录 jpa-changelog*.xml 中定义的全部 ChangeSet
        ChangeLogXMLParser xmlParser = new ChangeLogXMLParser(classLoader);
        Set<ChangeSet> changeSets = xmlParser.discoverAllChangeSets();

        // 记录 migrationsPackage 下全部 Migration 实现类
        Set<Migration> migrations = new KeycloakMigrationParser(classLoader, migrationsPackage).discoverAllMigrations();

        // 写入 supported 文件
        JsonParent jsonFile = new JsonParent(changeSets, migrations);
        objectMapper.writeValue(sFile, jsonFile);

        // unsupported 文件初始化为空 JSON 结构
        objectMapper.writeValue(uFile, new JsonParent(Set.of(), Set.of()));
    }
}
