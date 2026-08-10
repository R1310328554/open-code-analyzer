package org.keycloak.ssf.event.caep;

/**
 * CAEP Session Revoked（会话撤销）事件：表示 subject 所标识的会话已被撤销。
 * <p>显式会话标识符可直接出现在 subject 中，也可包含会话的其他属性，
 * 以便接收方识别适用的会话。</p>
 */
public class CaepSessionRevoked extends CaepEvent {

    /** 事件类型 URI，定义见 https://openid.github.io/sharedsignals/openid-caep-1_0.html#name-session-revoked */

    public static final String TYPE = "https://schemas.openid.net/secevent/caep/event-type/session-revoked";

    public CaepSessionRevoked() {
        super(TYPE);
    }

    @Override
    public String toString() {
        return "SessionRevoked{}";
    }
}
