package org.keycloak.protocol.docker.installation.compose;

import java.net.URL;
import java.security.cert.Certificate;

/**
 * Docker Compose 安装 ZIP 内容聚合：封装 YAML 文件、数据目录名与证书目录。
 * <p>由 {@link DockerComposeYamlInstallationProvider} 生成完整 docker-compose 安装包结构。</p>
 */
public class DockerComposeZipContent {

    /** docker-compose.yaml 文件描述。 */
    private final DockerComposeYamlFile yamlFile;
    /** 数据目录名称（通常为 {@code data}）。 */
    private final String dataDirectoryName;
    /** 证书目录描述（Registry 与 IdP 信任链）。 */
    private final DockerComposeCertsDirectory certsDirectory;

    /**
     * 根据 Realm 证书与基础 URL 构建 ZIP 内容结构。
     * @param realmCert Realm 签名证书
     * @param realmBaseUrl Realm 对外基础 URL
     * @param realmName Realm 名称
     * @param clientId Docker Registry 客户端 ID
     */
    public DockerComposeZipContent(final Certificate realmCert, final URL realmBaseUrl, final String realmName, final String clientId) {
        final String dataDirectoryName = "data";
        final String certsDirectoryName = "certs";
        final String registryCertFilename = "localhost.crt";
        final String registryKeyFilename = "localhost.key";
        final String idpCertTrustChainFilename = "localhost_trust_chain.pem";

        this.yamlFile = new DockerComposeYamlFile(dataDirectoryName, certsDirectoryName, "/opt/" + certsDirectoryName, registryCertFilename, registryKeyFilename, idpCertTrustChainFilename, realmBaseUrl, realmName, clientId);
        this.dataDirectoryName = dataDirectoryName;
        this.certsDirectory = new DockerComposeCertsDirectory(certsDirectoryName, realmCert, registryCertFilename, registryKeyFilename, idpCertTrustChainFilename, realmName);
    }

    /** @return docker-compose YAML 文件描述 */
    public DockerComposeYamlFile getYamlFile() {
        return yamlFile;
    }

    /** @return 数据目录名称 */
    public String getDataDirectoryName() {
        return dataDirectoryName;
    }

    /** @return 证书目录描述 */
    public DockerComposeCertsDirectory getCertsDirectory() {
        return certsDirectory;
    }
}
