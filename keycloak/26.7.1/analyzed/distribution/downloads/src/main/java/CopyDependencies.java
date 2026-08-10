import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Keycloak 发行版构建辅助类：按资源清单从 Maven 本地仓库或项目路径复制依赖归档。
 * <p>
 * Created by st on 06.02.17.
 */
public class CopyDependencies {

    /** 入口：args[0]=Maven 仓库根，args[1]=目标根，args[2]=版本号。 */
    public static void main(String[] args) throws IOException {
        String version = args[2];
        Path targetRoot = new File(args[1]).toPath().resolve(version);
        Path projectDir = targetRoot.getParent().getParent().getParent().getParent();
        Path mavenRepository = new File(args[0]).toPath().resolve("org").resolve("keycloak");

        CopyDependencies dependencies = new CopyDependencies(version, projectDir, targetRoot, mavenRepository);
        dependencies.copyFiles();
    }

    private final String version;
    private final Path targetDir;
    private final Path projectDir;
    private final Path mavenRepository;

    /**
     * @param version Keycloak 版本号
     * @param projectDir 项目根目录
     * @param targetDir 复制目标目录
     * @param mavenRepository Maven org/keycloak 仓库路径
     */
    public CopyDependencies(String version, Path projectDir, Path targetDir, Path mavenRepository) {
        this.version = version;
        this.targetDir = targetDir;
        this.projectDir = projectDir;
        this.mavenRepository = mavenRepository;
    }

    /** 读取 classpath 资源 {@code files} 清单并逐行复制 mvn/npm 依赖。 */
    public void copyFiles() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(CopyDependencies.class.getResourceAsStream("files")));
        targetDir.toFile().mkdirs();

        for (String l = br.readLine(); l != null; l = br.readLine()) {
            if (l.trim().length() > 0) {
                l = replaceVariables(l);

                String[] t = l.trim().split(":");

                String type = t[0];
                String artifactName = t[1];
                String destinationName = t.length == 2 ? artifactName : t[2];

                switch (type) {
                    case "mvn":
                        copyMaven(artifactName, destinationName);
                        break;
                    case "npm":
                        copyNpm(artifactName, destinationName);
                        break;
                }
            }
        }

        br.close();
    }

    /** 从 Maven 仓库复制 .tar.gz/.tgz/.zip 构件并重命名。 */
    private void copyMaven(String artifactName, String destinationName) throws IOException {
        File artifactDir = mavenRepository.resolve(artifactName).resolve(version).toFile();
        if (!artifactDir.isDirectory()) {
            throw new RuntimeException(artifactName + " (" + artifactDir + ") not found");
        }

        File[] files = artifactDir.listFiles((file, name) -> name.contains(".tar.gz") || name.contains(".tgz") || name.contains(".zip"));

        for (File f : files) {
            Files.copy(f.toPath(), targetDir.resolve(f.getName().replace(artifactName, destinationName)), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** 从项目目录复制 npm 构建产物。 */
    private void copyNpm(String artifactName, String destinationName) throws IOException {
        Path artifactPath = projectDir.resolve(artifactName);
        if (!artifactPath.toFile().isFile()) {
            throw new RuntimeException(artifactName + " (" + artifactPath + ") not found");
        }

        Files.copy(projectDir.resolve(artifactName), targetDir.resolve(destinationName));
    }

    /** 将清单行中的 {@code $$VERSION$$} 替换为当前版本。 */
    private String replaceVariables(String input) {
        return input.replaceAll("\\$\\$VERSION\\$\\$", version);
    }

}
