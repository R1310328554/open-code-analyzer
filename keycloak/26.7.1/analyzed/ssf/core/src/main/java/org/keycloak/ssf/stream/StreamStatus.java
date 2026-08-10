package org.keycloak.ssf.stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SSF 发送方内部使用的流状态快照，包含 stream_id、status 字符串与可选 reason。
 * <p>与 {@link SsfStreamStatusRepresentation} 类似，但 status 为 wire 字符串而非枚举。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamStatus {

    /** 流的唯一标识符。 */
    @JsonProperty("stream_id")
    private String streamId;

    /** 当前状态码（如 enabled、paused、disabled）。 */
    @JsonProperty("status")
    private String status;

    /** 状态关联的可选原因说明。 */
    @JsonProperty("reason")
    private String reason;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
