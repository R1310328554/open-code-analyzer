package org.keycloak.quarkus.deployment;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.connections.jpa.updater.liquibase.lock.DummyLockService;
import org.keycloak.quarkus.runtime.KeycloakRecorder;

import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import liquibase.database.Database;
import liquibase.lockservice.LockService;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sqlgenerator.SqlGenerator;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import static org.keycloak.quarkus.deployment.KeycloakProcessor.getDefaultDataSource;

/**
 * Liquibase 服务实现类索引与运行时配置的 Quarkus 构建处理器。
 */
class LiquibaseProcessor {

    /** 扫描 Jandex 索引并配置 Liquibase 服务实现列表。 */
    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    void configure(KeycloakRecorder recorder, List<JdbcDataSourceBuildItem> jdbcDataSources, CombinedIndexBuildItem indexBuildItem) {
        DotName liquibaseServiceName = DotName.createSimple(LiquibaseService.class.getName());
        Map<String, List<String>> services = new HashMap<>();
        IndexView index = indexBuildItem.getIndex();
        JdbcDataSourceBuildItem dataSourceBuildItem = getDefaultDataSource(jdbcDataSources);
        String dbKind = dataSourceBuildItem.getDbKind();

        for (Class<?> c : Arrays.asList(liquibase.diff.compare.DatabaseObjectComparator.class,
                liquibase.parser.NamespaceDetails.class,
                liquibase.precondition.Precondition.class,
                Database.class,
                liquibase.change.Change.class,
                liquibase.snapshot.SnapshotGenerator.class,
                liquibase.changelog.ChangeLogHistoryService.class,
                liquibase.datatype.LiquibaseDataType.class,
                liquibase.executor.Executor.class,
                SqlGenerator.class)) {
            List<String> impls = new ArrayList<>();
            services.put(c.getName(), impls);
            Set<ClassInfo> classes = new HashSet<>();
            if (c.isInterface()) {
                classes.addAll(index.getAllKnownImplementors(DotName.createSimple(c.getName())));
            } else {
                classes.addAll(index.getAllKnownSubclasses(DotName.createSimple(c.getName())));
            }
            filterImplementations(c, dbKind, classes);
            for (ClassInfo found : classes) {
                if (Modifier.isAbstract(found.flags()) ||
                        Modifier.isInterface(found.flags()) ||
                        !found.hasNoArgsConstructor() ||
                        !Modifier.isPublic(found.flags())) {
                    continue;
                }
                AnnotationInstance annotationInstance = found.declaredAnnotation(liquibaseServiceName);
                if (annotationInstance == null || !annotationInstance.value("skip").asBoolean()) {
                    impls.add(found.name().toString());
                }
            }
        }

        services.put(LockService.class.getName(), Collections.singletonList(DummyLockService.class.getName()));
        services.put(ChangeLogParser.class.getName(), Collections.singletonList(XMLChangeLogSAXParser.class.getName()));

        recorder.configureLiquibase(services);
    }

    /** 按当前数据库类型过滤 Liquibase Database 实现类。 */
    private void filterImplementations(Class<?> types, String dbKind, Set<ClassInfo> classes) {
        if (Database.class.equals(types)) {
            // 移除当前数据库类型不支持的 Liquibase Database 实现
            classes.removeIf(classInfo -> !org.keycloak.config.database.Database.isLiquibaseDatabaseSupported(classInfo.name().toString(), dbKind));
        }
    }
}
