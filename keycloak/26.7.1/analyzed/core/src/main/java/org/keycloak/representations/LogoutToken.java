package org.keycloak.representations;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.TokenCategory;
import org.keycloak.util.TokenUtil;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenID Connect 后台/前台登出通知用的 Logout Token（JWT）。
 * <p>
 * 含 {@code sid} 会话标识与 {@code events} 登出事件声明。
 */
public class LogoutToken extends JsonWebToken {

    /** 登出目标会话 ID。 */
    @JsonProperty("sid")
    protected String sid;

    /** OIDC 登出事件对象（如 {@code http://schemas.openid.net/event/backchannel-logout}）。 */
    @JsonProperty("events")
    protected Map<String, Object> events = new HashMap<>();

    public Map<String, Object> getEvents() {
        return events;
    }

    public void putEvents(String name, Object value) {
        events.put(name, value);
    }

    public String getSid() {
        return sid;
    }

    public LogoutToken setSid(String sid) {
        this.sid = sid;
        return this;
    }

    public LogoutToken() {
        type(TokenUtil.TOKEN_TYPE_LOGOUT);
    }

    @Override
    public TokenCategory getCategory() {
        return TokenCategory.LOGOUT;
    }
}
