package org.keycloak.ssf.stream;

import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * SSF 事件流的完整配置表示，对应规范中的 Stream Configuration JSON 对象。
 * <p>参见 https://openid.net/specs/openid-sharedsignals-framework-1_0.html#name-stream-configuration</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"iss", "aud", "events_supported", "events_requested", "events_delivered", "delivery", "min_verification_interval", "format"})
public class SsfStreamRepresentation {

    //see: https://openid.net/specs/openid-sharedsignals-framework-1_0.html#section-7.1.1

    /** 发送方提供，REQUIRED。唯一标识该流的字符串；创建非删除流时发送方 MUST 生成唯一 ID。 */
    @JsonProperty("stream_id")
    private String id;

    /** 接收方提供，OPTIONAL。流的描述性字符串，便于多流场景下人工识别；发送方 MAY 截断超长内容。 */
    @JsonProperty("description")
    private String description;

    /** 发送方提供，REQUIRED。发送方声明的 Issuer 标识（https URL，无 query/fragment），MUST 与该发送方签发的 SET 中 {@code iss} 声明一致。 */
    @JsonProperty("iss")
    private URI issuer;

    /** 发送方提供，REQUIRED。JWT 风格的 audience 声明（字符串或字符串数组），标识事件接收方；创建后不可更新。 */
    @JsonProperty("aud")
    private Object audience; // Can be URI or List<URI>

    /** 发送方提供，OPTIONAL。该接收方所支持的事件类型 URI 列表；若省略，发送方 SHOULD 通过其他途径（如在线文档）告知。 */
    @JsonProperty("events_supported")
    private List<URI> eventsSupported;

    /** 接收方提供，OPTIONAL。接收方请求订阅的事件类型 URI 列表；接收方 SHOULD 仅请求其理解且能处理的事件，且数组 SHOULD NOT 为空。 */
    @JsonProperty("events_requested")
    private List<URI> eventsRequested;

    /** 发送方提供，REQUIRED。发送方 MUST 在该流中投递的事件类型 URI 列表，为 events_supported 与 events_requested 交集的子集；接收方 MUST 据此判断可收到的事件类型。 */
    @JsonProperty("events_delivered")
    private List<URI> eventsDelivered;

    /** REQUIRED. SET 投递方式的配置对象（name/value 对）；{@code method} 键标识具体投递 URI，参见规范 10.3.1。 */
    @JsonProperty("delivery")
    private AbstractDeliveryMethodRepresentation delivery;

    /** 发送方提供，OPTIONAL。两次验证请求之间的最小间隔（秒）；接收方超频时发送方 MAY 返回 429。 */
    @JsonProperty("min_verification_interval")
    private Integer minVerificationInterval;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URI getIssuer() {
        return issuer;
    }

    public void setIssuer(URI issuer) {
        this.issuer = issuer;
    }

    public Object getAudience() {
        return audience;
    }

    public void setAudience(Object audience) {
        this.audience = audience;
    }

    public List<URI> getEventsSupported() {
        return eventsSupported;
    }

    public void setEventsSupported(List<URI> eventsSupported) {
        this.eventsSupported = eventsSupported;
    }

    public List<URI> getEventsRequested() {
        return eventsRequested;
    }

    public void setEventsRequested(List<URI> eventsRequested) {
        this.eventsRequested = eventsRequested;
    }

    public List<URI> getEventsDelivered() {
        return eventsDelivered;
    }

    public void setEventsDelivered(List<URI> eventsDelivered) {
        this.eventsDelivered = eventsDelivered;
    }

    public AbstractDeliveryMethodRepresentation getDelivery() {
        return delivery;
    }

    public void setDelivery(AbstractDeliveryMethodRepresentation delivery) {
        this.delivery = delivery;
    }

    public Integer getMinVerificationInterval() {
        return minVerificationInterval;
    }

    public void setMinVerificationInterval(Integer minVerificationInterval) {
        this.minVerificationInterval = minVerificationInterval;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
