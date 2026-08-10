package org.keycloak.testframework.util;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.keycloak.it.utils.Maven;
import org.keycloak.testframework.server.KeycloakDependency;

import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;
import io.quarkus.bootstrap.resolver.maven.workspace.LocalProject;
import org.eclipse.aether.artifact.Artifact;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * 在 Quarkus Maven 工作区中解析本地模块、补全依赖坐标并打包 provider JAR。
 */
public final class MavenProjectUtil {

    private static LocalProject rootModuleProject;

    /** 返回当前构建上下文的 Maven 根模块（向上遍历至无父 POM）。 */
    private static LocalProject getRootModule() {
        if (rootModuleProject != null) {
            return rootModuleProject;
        }

        BootstrapMavenContext ctx = Maven.bootstrapCurrentMavenContext();
        LocalProject m = ctx.getCurrentProject();
        while (m.getLocalParent() != null) {
            m = m.getLocalParent();
        }
        rootModuleProject = m;
        return rootModuleProject;
    }

    /**
     * 在工作区中按坐标查找本地 Maven 模块。
     *
     * @param groupId Maven groupId
     * @param artifactId Maven artifactId
     * @return 匹配的 {@link LocalProject}
     * @throws RuntimeException 模块不存在于当前工作区时
     */
    public static LocalProject findLocalModule(String groupId, String artifactId) {
        LocalProject rootModule = getRootModule();
        LocalProject dependencyModule = rootModule.getWorkspace().getProject(groupId, artifactId);
        if (dependencyModule == null) {
            throw new RuntimeException("Failed to resolve artifact in this project: [" + groupId + ":" + artifactId + "]");
        }
        return dependencyModule;
    }

    /** @return 当前执行测试的 Maven 模块 */
    public static LocalProject getCurrentModule() {
        BootstrapMavenContext ctx = Maven.bootstrapCurrentMavenContext();
        return ctx.getCurrentProject();
    }

    /**
     * 用工作区或仓库信息补全 {@link KeycloakDependency} 的 groupId、artifactId 与 version。
     *
     * @param dependency 原始依赖声明
     * @return 坐标已解析的依赖副本
     */
    public static KeycloakDependency updateDependencyDetails(KeycloakDependency dependency) {
        KeycloakDependency.Builder updatedDependency = new KeycloakDependency.Builder()
                .hotDeployable(dependency.isHotDeployable())
                .dependencyCurrentProject(dependency.dependencyCurrentProject());

        if (dependency.dependencyCurrentProject()) {
            LocalProject localProject = getCurrentModule();

            updatedDependency
                    .setGroupId(localProject.getGroupId())
                    .setArtifactId(localProject.getArtifactId())
                    .setVersion(localProject.getVersion());
        } else {
            String version = Optional.ofNullable(Maven.getArtifact(dependency.getGroupId(), dependency.getArtifactId()))
                    .map(Artifact::getVersion)
                    .orElse("");

            updatedDependency
                    .setGroupId(dependency.getGroupId())
                    .setArtifactId(dependency.getArtifactId())
                    .setVersion(version);
        }

        return updatedDependency.build();
    }

    /**
     * 从编译输出目录打包并导出 provider JAR。
     *
     * @param jarName JAR 文件名
     * @param classesPath 编译输出目录路径（{@code target/classes}）
     * @param targetPath JAR 导出目标路径
     */
    public static void buildJar(String jarName, Path classesPath, Path targetPath) {
        JavaArchive providerJar = ShrinkWrap.create(JavaArchive.class, jarName);

        try (Stream<Path> sourcePathStream = Files.walk(classesPath)) {
            sourcePathStream.filter(Files::isRegularFile)
                    .forEach(p -> {
                        String relativeFilePath = classesPath.relativize(p).toString();

                        if (relativeFilePath.endsWith(".class")) {
                            String fullyQualifiedClassName = relativeFilePath.replace(File.separatorChar, '.').substring(0, relativeFilePath.lastIndexOf('.'));
                            providerJar.addClass(fullyQualifiedClassName);
                        } else {
                            File resourceFile = p.toFile();
                            providerJar.addAsResource(resourceFile, relativeFilePath);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        providerJar.as(ZipExporter.class).exportTo(targetPath.toFile(), true);
    }
}
