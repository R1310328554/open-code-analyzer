package org.keycloak.forms.login.freemarker.model;

import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.FrontChannelLogoutHandler;

/**
 * 前端通道登出 FreeMarker Bean：暴露 OIDC 前端通道登出流程中的重定向 URI 与客户端列表。
 * <p>数据来源于当前会话上的 {@link FrontChannelLogoutHandler}。</p>
 */
public class FrontChannelLogoutBean {

    /** 当前会话关联的前端通道登出处理器。 */
    private final FrontChannelLogoutHandler logoutInfo;

    /** @param session Keycloak 会话，用于读取当前登出上下文 */
    public FrontChannelLogoutBean(KeycloakSession session) {
        logoutInfo = FrontChannelLogoutHandler.current(session);
    }

    /** @return 登出完成后的重定向 URI */
    public String getLogoutRedirectUri() {
        return logoutInfo.getLogoutRedirectUri();
    }

    /** @return 需通过 iframe/重定向执行前端登出的客户端信息列表 */
    public List<FrontChannelLogoutHandler.ClientInfo> getClients() {
        return logoutInfo.getClients();
    }

}
