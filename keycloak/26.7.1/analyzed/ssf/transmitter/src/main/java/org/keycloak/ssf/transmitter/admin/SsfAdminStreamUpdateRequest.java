package org.keycloak.ssf.transmitter.admin;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code PATCH /admin/realms/{realm}/ssf/clients/{clientId}/stream} 的请求体。
 * 携带流配置中可由管理员编辑的子集；可只提交部分字段。未出现（或显式为 null）的字段
 * 不会改动已存储的对应值。
 *
 * <p>接收方提供的字段（如 {@code aud}、{@code iss}、{@code delivery}、
 * {@code default_subjects} 以及各接收方客户端属性 {@code ssf.streamAudience}、
 * {@code ssf.userSubjectFormat} 等）刻意不在此请求中——它们配置在接收方客户端本身，而非流上。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SsfAdminStreamUpdateRequest {

    @JsonProperty("description")
    private String description;

    @JsonProperty("events_requested")
    private Set<String> eventsRequested;

    /**
     * 若提供则原样采用——管理员指定的 {@code events_delivered} 会覆盖
     * 面向接收方端点计算的 {@code events_requested ∩ events_supported} 交集。
     * 省略时，根据最终的 {@code events_requested}（新值或已存值）重新计算交集。
     */
    @JsonProperty("events_delivered")
    private Set<String> eventsDelivered;

    public SsfAdminStreamUpdateRequest() {
    }

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

    public Set<String> getEventsDelivered() {
        return eventsDelivered;
    }

    public void setEventsDelivered(Set<String> eventsDelivered) {
        this.eventsDelivered = eventsDelivered;
    }
}
