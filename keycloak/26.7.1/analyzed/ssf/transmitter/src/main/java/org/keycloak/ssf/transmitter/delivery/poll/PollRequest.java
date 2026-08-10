package org.keycloak.ssf.transmitter.delivery.poll;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 8936 §2.1 轮询请求体。
 *
 * <p>所有字段可选；{@link PollDeliveryService} 应用的默认值为
 * {@code maxEvents=100}、{@code returnImmediately=true}、{@code ack=[]}、{@code setErrs={}}。</p>
 *
 * <p>{@code returnImmediately} 为前向兼容而解析，v1 始终按立即返回处理——长轮询延后。
 * 参见 {@code keycloak-notes/ssf/design/ssf-poll-delivery.md} 决策 §5。</p>
 *
 * <p>{@code setErrs} 为接收方 NACK 通道：每条将收到但无法处理的 SET 的 jti 映射到错误描述符（RFC 8936 §2.1）：
 * <pre>{@code
 * {
 *   "setErrs": {
 *     "<jti>": { "err": "invalid_issuer", "description": "..." }
 *   }
 * }
 * }</pre>
 * 匹配的发件箱行转为
 * {@link org.keycloak.models.jpa.entities.OutboxEntryStatus#DEAD_LETTER DEAD_LETTER}，
 * {@code last_error} 写入接收方提供的错误消息。</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PollRequest {

    @JsonProperty("maxEvents")
    private Integer maxEvents;

    @JsonProperty("returnImmediately")
    private Boolean returnImmediately;

    @JsonProperty("ack")
    private List<String> ack;

    @JsonProperty("setErrs")
    private Map<String, Map<String, Object>> setErrs;

    public Integer getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(Integer maxEvents) {
        this.maxEvents = maxEvents;
    }

    public Boolean getReturnImmediately() {
        return returnImmediately;
    }

    public void setReturnImmediately(Boolean returnImmediately) {
        this.returnImmediately = returnImmediately;
    }

    public List<String> getAck() {
        return ack;
    }

    public void setAck(List<String> ack) {
        this.ack = ack;
    }

    public Map<String, Map<String, Object>> getSetErrs() {
        return setErrs;
    }

    public void setSetErrs(Map<String, Map<String, Object>> setErrs) {
        this.setErrs = setErrs;
    }
}
