package org.keycloak.protocol.docker.installation;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.ClientInstallationProvider;
import org.keycloak.protocol.docker.DockerAuthV2Protocol;
import org.keycloak.protocol.docker.installation.compose.DockerComposeZipContent;

import org.jboss.logging.Logger;

/**
 * Docker 客户端安装提供方：生成含 docker-compose.yaml、TLS 证书与 README 的 ZIP 包。
 * <p>用于在本地快速搭建带 Keycloak 认证的私有 Registry 开发环境。</p>
 */
public class DockerComposeYamlInstallationProvider implements ClientInstallationProvider {
    private static Logger log = Logger.getLogger(DockerComposeYamlInstallationProvider.class);

    /** ZIP 包内根目录前缀。 */
    public static final String ROOT_DIR = "keycloak-docker-compose-yaml/";

    @Override
    public ClientInstallationProvider create(final KeycloakSession session) {
        return this;
    }

    @Override
    public void init(final Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(final KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return "docker-v2-compose-yaml";
    }

    @Override
    /** 使用 Realm RSA 证书与客户端信息生成 docker-compose 安装 ZIP。 */
    public Response generateInstallation(final KeycloakSession session, final RealmModel realm, final ClientModel client, final URI serverBaseUri) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutput = new ZipOutputStream(byteStream);

        try {
            return generateInstallation(zipOutput, byteStream, session.keys().getActiveRsaKey(realm).getCertificate(), session.getContext().getUri().getBaseUri().toURL(), realm.getName(), client.getClientId());
        } catch (final IOException e) {
            try {
                zipOutput.close();
            } catch (final IOException ex) {
                // 已在异常处理中，忽略关闭失败
            }
            try {
                byteStream.close();
            } catch (final IOException ex) {
                // do nothing, already in an exception
            }
            throw new RuntimeException("Error occurred during attempt to generate docker-compose yaml installation files", e);
        }
    }

    public Response generateInstallation(final ZipOutputStream zipOutput, final ByteArrayOutputStream byteStream, final Certificate realmCert, final URL realmBaseURl,
                                         final String realmName, final String clientName) throws IOException {
        final DockerComposeZipContent zipContent = new DockerComposeZipContent(realmCert, realmBaseURl, realmName, clientName);

        zipOutput.putNextEntry(new ZipEntry(ROOT_DIR));

        // 写入 docker-compose.yaml
        zipOutput.putNextEntry(new ZipEntry(ROOT_DIR + "docker-compose.yaml"));
        zipOutput.write(zipContent.getYamlFile().generateDockerComposeFileBytes());
        zipOutput.closeEntry();

        // 写入 Registry 数据目录占位与 .gitignore
        zipOutput.putNextEntry(new ZipEntry(ROOT_DIR + zipContent.getDataDirectoryName() + "/"));
        zipOutput.putNextEntry(new ZipEntry(ROOT_DIR + zipContent.getDataDirectoryName() + "/.gitignore"));
        zipOutput.write("*".getBytes());
        zipOutput.closeEntry();

        // 写入 localhost 证书/私钥及 IdP 信任链
        final String certsDirectory = ROOT_DIR + zipContent.getCertsDirectory().getDirectoryName() + "/";
        zipOutput.putNextEntry(new ZipEntry(certsDirectory));
        zipOutput.putNextEntry(new ZipEntry(certsDirectory + zipContent.getCertsDirectory().getLocalhostCertFile().getKey()));
        zipOutput.write(zipContent.getCertsDirectory().getLocalhostCertFile().getValue());
        zipOutput.closeEntry();
        zipOutput.putNextEntry(new ZipEntry(certsDirectory + zipContent.getCertsDirectory().getLocalhostKeyFile().getKey()));
        zipOutput.write(zipContent.getCertsDirectory().getLocalhostKeyFile().getValue());
        zipOutput.closeEntry();
        zipOutput.putNextEntry(new ZipEntry(certsDirectory + zipContent.getCertsDirectory().getIdpTrustChainFile().getKey()));
        zipOutput.write(zipContent.getCertsDirectory().getIdpTrustChainFile().getValue());
        zipOutput.closeEntry();

        // 打包 README 说明文档
        zipOutput.putNextEntry(new ZipEntry(ROOT_DIR + "README.md"));
        try (BufferedReader br = new BufferedReader(new InputStreamReader(DockerComposeYamlInstallationProvider.class.getResourceAsStream("/DockerComposeYamlReadme.md")))) {
            final String readmeContent = br.lines().collect(Collectors.joining("\n"));
            zipOutput.write(readmeContent.getBytes());
            zipOutput.closeEntry();
        }

        zipOutput.close();
        byteStream.close();

        return Response.ok(byteStream.toByteArray(), getMediaType()).build();
    }

    @Override
    public String getProtocol() {
        return DockerAuthV2Protocol.LOGIN_PROTOCOL;
    }

    @Override
    /** @return 控制台展示名称 */
    public String getDisplayType() {
        return "Docker Compose YAML";
    }

    @Override
    /** @return 帮助说明：用于本地开发 Registry */
    public String getHelpText() {
        return "Produces a zip file that can be used to stand up a development registry on localhost";
    }

    @Override
    /** @return 下载文件名 */
    public String getFilename() {
        return "keycloak-docker-compose-yaml.zip";
    }

    @Override
    public String getMediaType() {
        return "application/zip";
    }

    @Override
    /** 仅以文件下载方式提供，不在页面内联展示。 */
    public boolean isDownloadOnly() {
        return true;
    }
}
