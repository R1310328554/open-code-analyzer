package org.keycloak.testframework.server;

import java.net.MalformedURLException;
import java.net.URL;

import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;

/**
 * 封装托管 Keycloak 服务器常用端点的 URL 解析。
 * <p>
 * 提供应用基址、管理端口、master realm、Admin API 与 metrics 等路径的字符串与 {@link URL} 形式。
 */
public class KeycloakUrls {

    private final String baseUrl;
    private final String managementBaseUrl;

    /**
     * @param baseUrl 应用 HTTP(S) 基址
     * @param managementBaseUrl 管理/指标端口基址
     */
    public KeycloakUrls(String baseUrl, String managementBaseUrl) {
        this.baseUrl = baseUrl;
        this.managementBaseUrl = managementBaseUrl;
    }

    /**
     * Keycloak 服务器基址的字符串形式（例如 {@code http://localhost:8080}）。
     *
     * @return 服务器基址字符串
     */
    public String getBase() {
        return baseUrl;
    }


    /**
     * Keycloak 服务器基址的 {@link URL} 形式。
     *
     * @return 服务器基址 URL
     */
    public URL getBaseUrl() {
        return toUrl(getBase());
    }

    /**
     * master realm 基址的字符串形式。
     *
     * @return master realm URL 字符串
     */
    public String getMasterRealm() {
        return baseUrl + "/realms/master";
    }

    /**
     * master realm 的 {@link URL}。
     *
     * @return master realm URL
     */
    public URL getMasterRealmUrl() {
        return toUrl(getMasterRealm());
    }

    /**
     * Admin REST API 基址的字符串形式。
     *
     * @return 管理端 URL 字符串
     */
    public String getAdmin() {
        return baseUrl + "/admin";
    }

    /**
     * Admin REST API 基址的 {@link URL}。
     *
     * @return 管理端 URL
     */
    public URL getAdminUrl() {
        return toUrl(getAdmin());
    }

    /**
     * 基于服务器基址构建 URI 路径。
     *
     * @return 基址 URI 构建器
     */
    public KeycloakUriBuilder getBaseBuilder() {
        return toBuilder(getBase());
    }

    /**
     * 基于 Admin API 基址构建 URI 路径。
     *
     * @return 管理端 URI 构建器
     */
    public KeycloakUriBuilder getAdminBuilder() {
        return toBuilder(getAdmin());
    }

    /**
     * metrics 端点的 URL 字符串。
     *
     * @return metrics 端点地址
     */
    public String getMetric() {
        return managementBaseUrl + "/metrics";
    }

    private URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private KeycloakUriBuilder toBuilder(String url) {
        return KeycloakUriBuilder.fromUri(url);
    }

    /**
     * 返回指定 realm 的 OIDC token 端点 URL。
     *
     * @param realm realm 名称
     * @return token 端点 URL 字符串
     */
    public String getToken(String realm) {
        return baseUrl + "/realms/" + realm + "/protocol/" + OIDCLoginProtocol.LOGIN_PROTOCOL + "/token";
    }
}
