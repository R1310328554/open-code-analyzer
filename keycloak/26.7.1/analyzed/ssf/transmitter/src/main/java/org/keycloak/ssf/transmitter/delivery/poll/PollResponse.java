package org.keycloak.ssf.transmitter.delivery.poll;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 8936 §2.2 轮询响应体。
 *
 * <p>{@code sets} 将每个已投递 SET 的 {@code jti} 映射为其已签名的 JWS
 * 载荷（接收方通过在下次请求的 {@code ack} 数组中回传这些 jti 进行确认）。
 * {@code moreAvailable} 表示接收方是否应立即再次轮询以清空剩余待处理事件。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PollResponse {

    @JsonProperty("sets")
    private Map<String, String> sets = new LinkedHashMap<>();

    @JsonProperty("moreAvailable")
    private boolean moreAvailable;

    public Map<String, String> getSets() {
        return sets;
    }

    public void setSets(Map<String, String> sets) {
        this.sets = sets;
    }

    public boolean isMoreAvailable() {
        return moreAvailable;
    }

    public void setMoreAvailable(boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }
}
