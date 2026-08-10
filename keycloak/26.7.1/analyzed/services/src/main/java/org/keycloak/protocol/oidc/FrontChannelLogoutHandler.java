package org.keycloak.protocol.oidc;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.headers.SecurityHeadersProvider;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.StringUtil;

/**
 * OIDC 前端通道（Front-Channel）登出协调器。
 * <p>收集需 iframe 登出的客户端、配置 CSP frame-src，并渲染登出确认页。</p>
 */
public class FrontChannelLogoutHandler {

    /** @param session Keycloak 会话 @return 会话中已绑定的处理器，或 null */
    public static FrontChannelLogoutHandler current(KeycloakSession session) {
        return (FrontChannelLogoutHandler) session.getAttribute(FrontChannelLogoutHandler.class.getName());
    }

    /**
     * 获取或创建前端通道登出处理器并绑定到会话。
     * @param session Keycloak 会话
     * @param clientSession 已认证客户端会话
     * @return 处理器实例
     */
    public static FrontChannelLogoutHandler currentOrCreate(KeycloakSession session, AuthenticatedClientSessionModel clientSession) {
        FrontChannelLogoutHandler current = current(session);

        if (current == null) {
            return new FrontChannelLogoutHandler(session, clientSession);
        }

        return current;
    }

    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** 用户会话 ID（sid）。 */
    private final String sid;
    /** 令牌 issuer（iss）。 */
    private final String issuer;
    /** 待前端登出的客户端列表。 */
    private final List<ClientInfo> clients = new ArrayList<>();

    /** 登出完成后的重定向 URI。 */
    private String logoutRedirectUri;

    private FrontChannelLogoutHandler(KeycloakSession session, AuthenticatedClientSessionModel clientSession) {
        this.session = session;
        this.sid = clientSession.getUserSession().getId();
        this.issuer = clientSession.getNote(OIDCLoginProtocol.ISSUER);
        this.session.setAttribute(getClass().getName(), this);
    }

    /** @param client 需前端登出的客户端 */
    public void addClient(ClientModel client) {
        clients.add(new ClientInfo(client));
    }

    /** @return 客户端登出信息列表 */
    public List<ClientInfo> getClients() {
        return clients;
    }

    /** @return 登出后重定向 URI */
    public String getLogoutRedirectUri() {
        return logoutRedirectUri;
    }

    /**
     * 配置 CSP 并渲染前端通道登出页。
     * @param redirectUri 登出完成后跳转地址
     * @return 登出页面 HTTP 响应
     */
    public Response renderLogoutPage(String redirectUri) {
        configureCSP();
        this.logoutRedirectUri = redirectUri;
        return session.getProvider(LoginFormsProvider.class).createFrontChannelLogoutPage();
    }

    private void configureCSP() {
        StringBuilder allowFrameSrc = new StringBuilder();

        for (ClientInfo client : clients) {
            allowFrameSrc.append(client.frontChannelLogoutUrl.getAuthority()).append(' ');
        }

        session.getProvider(SecurityHeadersProvider.class).options().allowFrameSrc(allowFrameSrc.toString());
    }

    private URI createFrontChannelLogoutUrl(ClientModel client) {
        OIDCAdvancedConfigWrapper config = OIDCAdvancedConfigWrapper.fromClientModel(client);
        String frontChannelLogoutUrl = config.getFrontChannelLogoutUrl();

        if (StringUtil.isBlank(frontChannelLogoutUrl)) {
            frontChannelLogoutUrl = client.getBaseUrl();
        }

        if (frontChannelLogoutUrl == null) {
            throw new RuntimeException("Client [" + client.getClientId() + "] does not have a valid frontend logout URL");
        }

        UriBuilder builder = UriBuilder.fromUri(frontChannelLogoutUrl);

        if (config.isFrontChannelLogoutSessionRequired()) {
            builder.queryParam("sid", FrontChannelLogoutHandler.this.sid);
            builder.queryParam("iss", FrontChannelLogoutHandler.this.issuer);
        }

        return builder.build();
    }

    /** 单个客户端的前端登出 URL 与展示名。 */
    public class ClientInfo {

        /** 客户端模型。 */
        private final ClientModel client;
        /** 构建好的前端登出 URI。 */
        private final URI frontChannelLogoutUrl;

        public ClientInfo(ClientModel client) {
            this.client = client;
            this.frontChannelLogoutUrl = createFrontChannelLogoutUrl(client);
        }

        /** @return 前端登出 URL 字符串 */
        public String getFrontChannelLogoutUrl() {
            return frontChannelLogoutUrl.toString();
        }

        /** @return 客户端显示名或 clientId */
        public String getName() {
            String name = client.getName();

            if (name == null) {
                return client.getClientId();
            }

            return name;
        }
    }
}
