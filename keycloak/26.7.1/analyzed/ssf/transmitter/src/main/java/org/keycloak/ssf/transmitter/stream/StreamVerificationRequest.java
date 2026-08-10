package org.keycloak.ssf.transmitter.stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SSF 发送方中流验证请求的 wire 表示（{@code POST /streams/verify}）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamVerificationRequest {

    /** 待验证的目标流 ID。 */
    @JsonProperty("stream_id")
    private String streamId;

    /** 接收方可选的状态参数，原样嵌入验证 SET 供关联回调。 */
    @JsonProperty("state")
    private String state;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
