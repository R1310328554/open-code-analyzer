package org.keycloak.ssf.event.stream;

import org.keycloak.ssf.stream.StreamStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SSF 流状态更新事件（Stream Updated）。
 * <p>当 Transmitter 变更流状态时向 Receiver 发送。</p>
 * <p>定义见 https://openid.net/specs/openid-sharedsignals-framework-1_0-final.html#name-stream-updated-event</p>
 */
public class SsfStreamUpdatedEvent extends SsfStreamEvent {

    public static final String TYPE = "https://schemas.openid.net/secevent/ssf/event-type/stream-updated";

    /** REQUIRED。流的新状态。 */

    @JsonProperty("status")
    protected StreamStatus status;

    /** OPTIONAL。Transmitter 更新状态的简短原因说明。 */

    @JsonProperty("reason")
    protected String reason;

    public SsfStreamUpdatedEvent() {
        super(TYPE);
    }

    public StreamStatus getStatus() {
        return status;
    }

    public void setStatus(StreamStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
