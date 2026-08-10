package org.keycloak.ssf.transmitter.stream;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code POST /streams}（SSF §8.1.1.2）请求体的 wire 格式 DTO。
 * 仅携带接收方可写子集：{@code stream_id}、{@code iss}、{@code aud}、
 * {@code events_supported}、{@code events_delivered} 及 Keycloak {@code kc_*} 扩展
 * 由发送方生成，故刻意不在输入类型中出现。Jackson 默认 {@code FAIL_ON_UNKNOWN_PROPERTIES}
 * 会在绑定阶段以 400 拒绝未知字段。
 *
 * <p>{@link StreamConfigUpdateRepresentation} 继承本类并添加 {@code stream_id}，
 * 供 PATCH/PUT 定位已有流；创建请求 MUST NOT 携带接收方提供的 {@code stream_id}。</p>
 *
 * @see https://openid.github.io/sharedsignals/openid-sharedsignals-framework-1_0.html#section-8.1.1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamConfigInputRepresentation {

    @JsonProperty("description")
    protected String description;

    @JsonProperty("events_requested")
    protected Set<String> eventsRequested;

    @JsonProperty("delivery")
    protected StreamDeliveryConfig delivery;

    // ------------------------------------------------------------------
    //  遗留 SSE CAEP 1.0 ID1 兼容字段
    // ------------------------------------------------------------------
    //  Legacy compatibility fields for SSE CAEP 1.0 ID1
    // See: https://openid.net/specs/openid-sse-framework-1_0.html#rfc.section.7.1.2
    //
    //  Per SSF §8.1.1.1 these are transmitter-supplied — a spec-compliant
    //  receiver MUST NOT include them in a create/update body. Apple
    //  Business Manager's legacy CAEP SSE profile nevertheless echoes them
    //  back in its create request (it round-trips the GET metadata shape
    //  and submits it as a create), so we declare them here purely so
    //  Jackson's default FAIL_ON_UNKNOWN_PROPERTIES doesn't 400 those
    //  requests at bind time.
    //
    //  Behavioural contract:
    //    * {@code iss}  — always ignored. The transmitter unconditionally
    //                     sets the issuer from its own metadata.
    //    * {@code aud}  — used as a fallback when the receiver client
    //                     does not have an admin-configured
    //                     {@code ssf.streamAudience} attribute. This lets
    //                     Apple's federation feed URL land on the stored
    //                     stream instead of being overwritten by the
    //                     generated {@code clientId/streamId} default.
    //                     Admin-configured audience still wins.
    //    * {@code format} — always ignored. The per-stream subject format
    //                     is driven by the {@code ssf.userSubjectFormat}
    //                     client attribute, validated against
    //                     {@code SsfUserSubjectFormats.ALLOWED}.
    // ------------------------------------------------------------------

    // LEGACY FIELDS START
    @JsonProperty("iss")
    protected String issuer;

    @JsonProperty("aud")
    protected Set<String> audience;

    @JsonProperty("format")
    protected String format;
    // LEGACY FIELDS END

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getEventsRequested() {
        return eventsRequested;
    }

    public void setEventsRequested(Set<String> eventsRequested) {
        this.eventsRequested = eventsRequested;
    }

    public StreamDeliveryConfig getDelivery() {
        return delivery;
    }

    public void setDelivery(StreamDeliveryConfig delivery) {
        this.delivery = delivery;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Set<String> getAudience() {
        return audience;
    }

    public void setAudience(Set<String> audience) {
        this.audience = audience;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
