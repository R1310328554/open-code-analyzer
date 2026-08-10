package org.keycloak.ssf.stream;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SSF 流状态变更的 API 响应/请求表示，携带 stream_id、status 与可选 reason。
 * <p>用于 pause、resume、disable 等流生命周期管理操作。</p>
 */
public class SsfStreamStatusRepresentation {

    /** 目标流的唯一标识符。 */
    @JsonProperty("stream_id")
    private String streamId;

    /** 流的新状态（enabled / paused / disabled）。 */
    @JsonProperty("status")
    private StreamStatus status;

    /** 状态变更的可选原因说明，供审计或运维排查。 */
    @JsonProperty("reason")
    private String reason;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
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
