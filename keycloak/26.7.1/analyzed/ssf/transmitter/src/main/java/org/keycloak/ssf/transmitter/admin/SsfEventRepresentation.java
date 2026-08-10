package org.keycloak.ssf.transmitter.admin;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 单条 SSF 发件箱行的管理端快照，由按 jti 查询的管理端点返回。
 * 涵盖任意发件箱状态（PENDING、HELD、DELIVERED、DEAD_LETTER），
 * 使操作员能回答「该事件在投递管道中的位置」，无论仍在排队、已投递或终态失败。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SsfEventRepresentation {

    @JsonProperty("jti")
    private String jti;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("deliveryMethod")
    private String deliveryMethod;

    @JsonProperty("status")
    private String status;

    @JsonProperty("attempts")
    private int attempts;

    @JsonProperty("createdAt")
    private Long createdAt;

    @JsonProperty("nextAttemptAt")
    private Long nextAttemptAt;

    @JsonProperty("deliveredAt")
    private Long deliveredAt;

    @JsonProperty("lastError")
    private String lastError;

    @JsonProperty("streamId")
    private String streamId;

    /**
     * 解码后的安全事件令牌（JWS 载荷）——接收方将处理的完整声明集，原样呈现。
     * 含发送方提供的头声明（{@code iss}、{@code iat}、{@code jti}、{@code aud}、{@code txn}）、
     * 主体（SSF 1.0 的 {@code sub_id}，或旧版 SSE CAEP 下嵌套于 {@code events.<type>.subject}）
     * 及事件体。查询结果中以格式化 JSON 展示，供操作员检视接收方将见内容。
     * 无法解码已编码 SET 时为 null。
     */
    @JsonProperty("decodedSet")
    private Map<String, Object> decodedSet;

    /**
     * SET 所涉用户的 Keycloak UUID。主体仅为组织、无法解析到用户或非用户标识格式时为 null。
     * 供管理 UI 从 Pending Events 查询结果直达用户详情页，无需再次搜索。
     */
    @JsonProperty("userId")
    private String userId;

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(Long nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    public Long getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Long deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public Map<String, Object> getDecodedSet() {
        return decodedSet;
    }

    public void setDecodedSet(Map<String, Object> decodedSet) {
        this.decodedSet = decodedSet;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
