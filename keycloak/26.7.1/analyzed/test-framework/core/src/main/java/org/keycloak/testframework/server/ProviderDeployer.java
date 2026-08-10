package org.keycloak.testframework.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.it.utils.Maven;
import org.keycloak.testframework.util.FileUtils;
import org.keycloak.testframework.util.MavenProjectUtil;

import io.quarkus.bootstrap.resolver.maven.workspace.LocalProject;
import org.jboss.logging.Logger;

/**
 * 将测试声明的 Maven provider 依赖同步到 Keycloak {@code providers} 目录。
 * <p>
 * 支持热部署（从编译输出打包 JAR）与远程 artifact 复制，并清理未请求的遗留 JAR。
 */
final class ProviderDeployer {

    private final Logger log;
    private final File providersDir;
    private final boolean hotDeployEnabled;
    private final Set<KeycloakDependency> requestedDependencies;

    /**
     * @param log 日志记录器
     * @param keycloakHomeDir Keycloak 安装根目录
     * @param requestedDependencies 测试请求的 provider 依赖
     * @param hotDeployEnabled 是否允许热部署本地模块
     */
    ProviderDeployer(Logger log, File keycloakHomeDir, Set<KeycloakDependency> requestedDependencies, boolean hotDeployEnabled) {
        this.log = log;
        this.providersDir = new File(keycloakHomeDir, "providers");
        this.requestedDependencies = requestedDependencies;
        this.hotDeployEnabled = hotDeployEnabled;
    }

    /**
     * 同步 providers 目录：删除多余 JAR、复制或打包请求的依赖。
     *
     * @return 是否有任何 provider 文件被新增或更新
     * @throws IOException 读写 provider 文件失败时
     */
    boolean updateDependencies() throws IOException {
        boolean anyDependenciesModified = deleteNotRequestedDependencies();

        for (KeycloakDependency d : requestedDependencies) {
            boolean shouldPackageClasses = hotDeployEnabled && d.isHotDeployable();

            String jarName = getDependencyJarName(d);

            Path dependencyPath = getDependencyPath(d);
            Path targetPath = providersDir.toPath().resolve(jarName);

            File targetFile = targetPath.toFile();

            long dependencyLastModified = getMostRecentModification(dependencyPath);
            File targetLastModifiedFile = new File(targetFile.getAbsolutePath() + ".lastModified");
            long targetLastModified = targetLastModifiedFile.isFile() ? FileUtils.readLongFromFile(targetLastModifiedFile) : -1;

            if (dependencyLastModified != targetLastModified || !targetFile.isFile()) {
                log.trace("Adding or overwriting existing provider: " + targetPath.toFile().getAbsolutePath());

                if (shouldPackageClasses || d.dependencyCurrentProject()) {
                    MavenProjectUtil.buildJar(jarName, dependencyPath, targetPath);
                } else {
                    Files.copy(dependencyPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                Files.writeString(targetLastModifiedFile.toPath(), Long.toString(dependencyLastModified));
                anyDependenciesModified = true;
            }
        }
        return anyDependenciesModified;
    }

    /** 根据 Maven 坐标生成 providers 目录下的 JAR 文件名。 */
    private String getDependencyJarName(KeycloakDependency dependency) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();

        if (dependency.dependencyCurrentProject()) {
            LocalProject project = MavenProjectUtil.getCurrentModule();

            groupId = project.getGroupId();
            artifactId = project.getArtifactId();
        }

        return groupId + "__" + artifactId + ".jar";
    }

    /** 删除 providers 目录中不在请求列表内的 JAR 及其 {@code .lastModified}  sidecar。 */
    private boolean deleteNotRequestedDependencies() {
        Set<String> requestedJarNames = requestedDependencies.stream()
                .map(this::getDependencyJarName)
                .collect(Collectors.toSet());

        List<File> toDelete = listExistingDependencies().stream()
                .filter(f -> !requestedJarNames.contains(f.getName()))
                .toList();

        for (File f : toDelete) {
            String path = f.getAbsolutePath();
            log.trace("Deleted non-requested provider: " + path);
            FileUtils.delete(f);
            FileUtils.delete(new File(path + ".lastModified"));
        }

        return !toDelete.isEmpty();
    }

    /** 列出 providers 目录下现有的 {@code .jar} 文件。 */
    private List<File> listExistingDependencies() {
        if (providersDir.isDirectory()) {
            File[] files = providersDir.listFiles(n -> n.getName().endsWith(".jar"));
            if (files != null) {
                return Arrays.stream(files).toList();
            }
        }
        return List.of();
    }

    /** 解析依赖源路径：当前模块 classes、本地模块输出或 Maven 仓库 artifact。 */
    private Path getDependencyPath(KeycloakDependency d) {
        if (d.dependencyCurrentProject()) {
            return MavenProjectUtil.getCurrentModule().getClassesDir();
        }

        if (d.isHotDeployable() && hotDeployEnabled) {
            return MavenProjectUtil.findLocalModule(d.getGroupId(), d.getArtifactId()).getClassesDir();
        }

        return Maven.resolveArtifact(d.getGroupId(), d.getArtifactId());
    }

    /** 返回文件或目录树中最新的最后修改时间戳，用于增量部署判断。 */
    private long getMostRecentModification(Path path) throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            return 0;
        }

        if (file.isFile()) {
            return file.lastModified();
        }

        try (Stream<Path> stream = Files.walk(path)) {
            return stream
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> p.toFile().lastModified())
                    .max()
                    .orElse(0);
        }
    }

}
