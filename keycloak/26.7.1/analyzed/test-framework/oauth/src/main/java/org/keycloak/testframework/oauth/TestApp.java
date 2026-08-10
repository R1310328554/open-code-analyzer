package org.keycloak.testframework.oauth;

import com.sun.net.httpserver.HttpServer;

/**
 * 在嵌入式 HTTP 服务器上暴露回调端点的模拟 OAuth 客户端。
 * <p>
 * Keycloak 可将授权回调、管理回调及 front-channel 登出请求发送至本应用注册的 URI。
 */
public class TestApp {

    /** OAuth 授权回调路径。 */
    public static final String OAUTH_CALLBACK_PATH = "/callback/oauth";
    /** Keycloak 管理回调路径。 */
    public static final String K_ADMIN_PATH = "/k_admin";
    /** front-channel 登出回调路径。 */
    public static final String FRONTCHANNEL_LOGOUT_PATH = "/frontchannel-logout";

    private final HttpServer httpServer;

    private final KcAdminInvocations kcAdminInvocations;

    private final String redirectionUri;
    private final String adminUri;
    private final String frontChannelLogoutUri;

    /**
     * 在指定 HTTP 服务器上注册 OAuth 回调、管理回调与 front-channel 登出处理器。
     *
     * @param httpServer 嵌入式 HTTP 服务器
     */
    public TestApp(HttpServer httpServer) {
        this.httpServer = httpServer;
        this.kcAdminInvocations = new KcAdminInvocations();

        try {
            httpServer.createContext(OAUTH_CALLBACK_PATH, new OAuthCallbackHandler());
            httpServer.createContext(K_ADMIN_PATH, new KcAdminCallbackHandler(kcAdminInvocations));
            httpServer.createContext(FRONTCHANNEL_LOGOUT_PATH, new FrontChannelLogoutHandler(kcAdminInvocations));

            String base = "http://127.0.0.1:" + httpServer.getAddress().getPort();
            redirectionUri = base + OAUTH_CALLBACK_PATH;
            adminUri = base + K_ADMIN_PATH;
            frontChannelLogoutUri = base + FRONTCHANNEL_LOGOUT_PATH;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /** 返回 OAuth 授权重定向 URI（含主机与端口）。 */
    public String getRedirectionUri() {
        return redirectionUri;
    }

    /** 返回客户端管理回调 URI。 */
    public String getAdminUri() {
        return adminUri;
    }

    /** 返回 front-channel 登出 URI。 */
    public String getFrontChannelLogoutUri() {
        return frontChannelLogoutUri;
    }

    /** 返回用于断言 Keycloak 管理回调的调用记录器。 */
    public KcAdminInvocations kcAdmin() {
        return kcAdminInvocations;
    }

    /** 从 HTTP 服务器移除所有已注册的上下文。 */
    public void close() {
        httpServer.removeContext(OAUTH_CALLBACK_PATH);
        httpServer.removeContext(K_ADMIN_PATH);
        httpServer.removeContext(FRONTCHANNEL_LOGOUT_PATH);
    }

}
