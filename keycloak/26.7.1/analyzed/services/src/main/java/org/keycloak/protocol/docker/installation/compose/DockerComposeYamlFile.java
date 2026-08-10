package org.keycloak.protocol.docker.installation.compose;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URL;

/**
 * docker-compose.yaml 内容生成器：配置 Registry v2 镜像、TLS、Keycloak 令牌认证及卷挂载。
 * <p>默认绑定 {@code 127.0.0.1:5000}，使用文件系统存储与令牌认证环境变量。</p>
 */
public class DockerComposeYamlFile {

    private final String registryDataDirName;
    private final String localCertDirName;
    private final String containerCertPath;
    private final String localhostCrtFileName;
    private final String localhostKeyFileName;
    private final String authServerTrustChainFileName;
    private final URL authServerUrl;
    private final String realmName;
    private final String serviceId;

    /**
     * @param registryDataDirName 容器与本地共用的 Registry 数据目录名
     * @param localCertDirName 本地证书相对目录名
     * @param containerCertPath 证书目录在容器内的挂载路径
     * @param localhostCrtFileName Registry TLS 证书文件名
     * @param localhostKeyFileName Registry TLS 私钥文件名
     * @param authServerTrustChainFileName IdP 信任链文件名，用于校验认证令牌
     * @param authServerUrl Keycloak 根 URL（开发环境常见 {@code http://localhost:8080/auth}）
     * @param realmName Docker 客户端所属 Realm 名称
     * @param serviceId Docker service 参数，对应 Keycloak 客户端 ID
     */
    public DockerComposeYamlFile(final String registryDataDirName, final String localCertDirName, final String containerCertPath, final String localhostCrtFileName, final String localhostKeyFileName, final String authServerTrustChainFileName, final URL authServerUrl, final String realmName, final String serviceId) {
        this.registryDataDirName = registryDataDirName;
        this.localCertDirName = localCertDirName;
        this.containerCertPath = containerCertPath;
        this.localhostCrtFileName = localhostCrtFileName;
        this.localhostKeyFileName = localhostKeyFileName;
        this.authServerTrustChainFileName = authServerTrustChainFileName;
        this.authServerUrl = authServerUrl;
        this.realmName = realmName;
        this.serviceId = serviceId;
    }

    /** 生成 docker-compose.yaml 文件 UTF-8 字节内容。 */
    public byte[] generateDockerComposeFileBytes() {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final PrintWriter writer = new PrintWriter(output);

        writer.print("registry:\n");
        writer.print("  image: registry:2\n");
        writer.print("  ports:\n");
        writer.print("    - 127.0.0.1:5000:5000\n");
        writer.print("  environment:\n");
        writer.print("    REGISTRY_STORAGE_FILESYSTEM_ROOTDIRECTORY: /" + registryDataDirName + "\n");
        writer.print("    REGISTRY_HTTP_TLS_CERTIFICATE: " + containerCertPath + "/" + localhostCrtFileName + "\n");
        writer.print("    REGISTRY_HTTP_TLS_KEY: " + containerCertPath + "/" + localhostKeyFileName + "\n");
        writer.print("    REGISTRY_AUTH_TOKEN_REALM: " + authServerUrl + "realms/" + realmName + "/protocol/docker-v2/auth\n");
        writer.print("    REGISTRY_AUTH_TOKEN_SERVICE: " + serviceId + "\n");
        writer.print("    REGISTRY_AUTH_TOKEN_ISSUER: " + authServerUrl + "realms/" + realmName + "\n");
        writer.print("    REGISTRY_AUTH_TOKEN_ROOTCERTBUNDLE: " + containerCertPath + "/" + authServerTrustChainFileName + "\n");
        writer.print("  volumes:\n");
        writer.print("    - ./" + registryDataDirName + ":/" + registryDataDirName + ":z\n");
        writer.print("    - ./" + localCertDirName + ":" + containerCertPath + ":z");

        writer.flush();
        writer.close();

        return output.toByteArray();
    }
}
