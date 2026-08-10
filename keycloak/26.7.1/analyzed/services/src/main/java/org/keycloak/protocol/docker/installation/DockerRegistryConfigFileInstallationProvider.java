package org.keycloak.protocol.docker.installation;

import java.net.URI;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.ClientInstallationProvider;
import org.keycloak.protocol.docker.DockerAuthV2Protocol;

/**
 * Docker Registry 配置文件片段安装提供方：输出 YAML 格式的 token 认证配置。
 * <p>包含 realm、service、issuer 三项，可直接合并到 Registry 配置。</p>
 */
public class DockerRegistryConfigFileInstallationProvider implements ClientInstallationProvider {

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
        return "docker-v2-registry-config-file";
    }

    @Override
    /** 生成 Registry {@code auth.token} 配置片段（realm/service/issuer）。 */
    public Response generateInstallation(final KeycloakSession session, final RealmModel realm, final ClientModel client, final URI serverBaseUri) {
        final StringBuilder responseString = new StringBuilder("auth:\n")
                .append("  token:\n")
                .append("    realm: ").append(serverBaseUri).append("/realms/").append(realm.getName()).append("/protocol/").append(DockerAuthV2Protocol.LOGIN_PROTOCOL).append("/auth\n")
                .append("    service: ").append(client.getClientId()).append("\n")
                .append("    issuer: ").append(serverBaseUri).append("/realms/").append(realm.getName()).append("\n");
        return Response.ok(responseString.toString(), MediaType.TEXT_PLAIN_TYPE).build();
    }

    @Override
    public String getProtocol() {
        return DockerAuthV2Protocol.LOGIN_PROTOCOL;
    }

    @Override
    /** @return 展示名称“Registry Config File” */
    public String getDisplayType() {
        return "Registry Config File";
    }

    @Override
    /** @return 提供 Registry 配置片段的帮助文本 */
    public String getHelpText() {
        return "Provides a registry configuration file snippet for use with this client";
    }

    @Override
    /** @return 建议文件名 {@code config.yml} */
    public String getFilename() {
        return "config.yml";
    }

    @Override
    public String getMediaType() {
        return MediaType.TEXT_PLAIN;
    }

    @Override
    public boolean isDownloadOnly() {
        return false;
    }
}
