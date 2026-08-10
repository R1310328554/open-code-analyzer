package org.keycloak.ssf.event.stream;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SSF 验证事件（Verification）。
 * <p>Transmitter 用于验证 Receiver 端点可达性。</p>
 * <p>定义见 https://openid.net/specs/openid-sharedsignals-framework-1_0-final.html#name-verification</p>
 */
public class SsfStreamVerificationEvent extends SsfStreamEvent {

    public static final String TYPE = "https://schemas.openid.net/secevent/ssf/event-type/verification";

    /** OPTIONAL。Receiver 回显的 opaque 状态值，用于关联验证请求与响应。 */
    @JsonProperty("state")
    protected String state;

    public SsfStreamVerificationEvent() {
        super(TYPE);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        // 无 state 时渲染为空对象而非 "state='null'"，
        // 与 Jackson 实际序列化行为一致（@JsonInclude(NON_NULL) 会省略 null）。
        return state == null
                ? "VerificationEvent{}"
                : "VerificationEvent{state='" + state + "'}";
    }
}
