package org.keycloak.ssf.transmitter.stream;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code PATCH /streams}（SSF §8.1.1.3 合并语义）与 {@code PUT /streams}
 *（§8.1.1.4 替换语义）请求体的 wire 格式 DTO。
 *
 * <p>在 {@link StreamConfigInputRepresentation} 基础上添加 {@code stream_id}，
 * 供调用方标识待更新流。其余接收方可写字段均继承自父类。
 * 发送方控制字段刻意缺失；Jackson 通过 {@code FAIL_ON_UNKNOWN_PROPERTIES} 在绑定时返回 400。</p>
 */
public class StreamConfigUpdateRepresentation extends StreamConfigInputRepresentation {

    /** 待更新/替换的目标流 ID。 */
    @JsonProperty("stream_id")
    protected String streamId;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }
}
