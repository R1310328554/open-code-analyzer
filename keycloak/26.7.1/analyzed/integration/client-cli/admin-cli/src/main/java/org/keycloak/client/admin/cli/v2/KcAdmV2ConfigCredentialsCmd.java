package org.keycloak.client.admin.cli.v2;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import org.keycloak.client.admin.cli.commands.ConfigCredentialsCmd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import static org.keycloak.client.admin.cli.KcAdmMain.CMD;
import static org.keycloak.client.admin.cli.KcAdmMain.V2_FLAG;

/**
 * v2 模式下的 {@code config credentials} 子命令。
 * <p>
 * 继承 v1 凭据配置逻辑，并在登录成功后尝试从管理端口自动拉取 OpenAPI 描述符并缓存。
 */
@Command(name = "credentials", description = "--server SERVER_URL --realm REALM [ARGUMENTS]")
class KcAdmV2ConfigCredentialsCmd extends ConfigCredentialsCmd {

    private static final int DEFAULT_MANAGEMENT_PORT = 9000;
    private static final String OPENAPI_PATH = "/openapi";

    /** 覆盖默认管理端口 OpenAPI 端点 URL。 */
    @Option(names = "--openapi-url", description = "URL of the OpenAPI endpoint for auto-fetching the server descriptor " +
            "(default: <server-protocol>://<server-host>:" + DEFAULT_MANAGEMENT_PORT + OPENAPI_PATH + ")")
    String openApiUrl;

    @Spec
    CommandSpec spec;

    /** v2 描述符本地缓存目录。 */
    private final Path cacheDir;

    KcAdmV2ConfigCredentialsCmd(Path cacheDir) {
        this.cacheDir = cacheDir;
    }

    @Override
    protected String getCommand() {
        return CMD + " " + V2_FLAG;
    }

    @Override
    protected void printExtraOptions(PrintWriter out) {
        out.println("    --openapi-url URL       URL of the OpenAPI endpoint for auto-fetching the server descriptor");
        out.println("                            (default: <server-protocol>://<server-host>:" + DEFAULT_MANAGEMENT_PORT + OPENAPI_PATH + ")");
    }

    /** 凭据配置完成后尝试自动获取 OpenAPI 描述符。 */
    @Override
    public void process() {
        super.process();
        tryAutoFetchOpenApi();
    }

    /** 从 {@code --openapi-url} 或默认管理端口拉取描述符并写入缓存。 */
    private void tryAutoFetchOpenApi() {
        if (server == null) {
            // 仅 --status 且无 --server 时未登录，无需拉取
            return;
        }

        String url = openApiUrl;
        if (url == null) {
            try {
                url = deriveDefaultOpenApiUrl();
            } catch (Exception e) {
                warnAutoFetchFailed("could not determine OpenAPI URL from server " + server
                        + (e.getMessage() != null ? ": " + e.getMessage() : ""));
                return;
            }
        }

        try {
            KcAdmV2CommandDescriptor descriptor = KcAdmV2ConfigOpenApiCmd.fetchDescriptorFromUrl(url);
            KcAdmV2DescriptorCache cache = new KcAdmV2DescriptorCache(cacheDir);
            cache.save(server, descriptor);
            printToErr("OpenAPI descriptor fetched from " + url + " and cached for " + server
                    + " (version " + descriptor.getVersion() + ")");
        } catch (Exception e) {
            warnAutoFetchFailed(url + (e.getMessage() != null ? ": " + e.getMessage() : ""));
        }
    }

    private void warnAutoFetchFailed(String detail) {
        printToErr("Failed to fetch OpenAPI descriptor from " + detail + ". "
                + "CLI commands may not match your server version. "
                + "You can use 'config openapi' to manually load the descriptor.");
    }

    private void printToErr(String message) {
        spec.commandLine().getErr().println(message);
    }

    private String deriveDefaultOpenApiUrl() throws Exception {
        URL serverParsed = new URL(server);
        URI managementUri = new URI(serverParsed.getProtocol(), null,
                serverParsed.getHost(), DEFAULT_MANAGEMENT_PORT, OPENAPI_PATH, null, null);
        return managementUri.toString();
    }
}
