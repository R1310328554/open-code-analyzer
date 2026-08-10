package org.keycloak.ssf.transmitter.admin;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 通过管理端点 {@code $KC_ADMIN_URL/admin/realms/{realm}/ssf/config} 暴露的 SSF 配置表示。
 *
 * <p>当前主要携带发送方默认支持的 SSF 事件类型集合。随着 SSF 功能演进，
 * 可在此追加 realm/发送方级设置，而无需引入独立端点。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SsfConfigRepresentation {

    private Set<String> defaultSupportedEvents;

    private Set<String> availableSupportedEvents;

    /**
     * {@link #availableSupportedEvents} 中由 Keycloak 事件监听器原生触发的子集。
     * 管理 UI 以此显示「natively emitted」徽章——集合外的事件仍可选，
     * 但仅在外部系统调用合成 emit 端点或部署自定义 mapper 时才会触发。
     */
    private Set<String> nativelyEmittedEvents;

    private Integer defaultPushEndpointConnectTimeoutMillis;

    private Integer defaultPushEndpointSocketTimeoutMillis;

    private String defaultUserSubjectFormat;

    public Set<String> getDefaultSupportedEvents() {
        return defaultSupportedEvents;
    }

    public void setDefaultSupportedEvents(Set<String> defaultSupportedEvents) {
        this.defaultSupportedEvents = defaultSupportedEvents;
    }

    public Set<String> getAvailableSupportedEvents() {
        return availableSupportedEvents;
    }

    public void setAvailableSupportedEvents(Set<String> availableSupportedEvents) {
        this.availableSupportedEvents = availableSupportedEvents;
    }

    public Set<String> getNativelyEmittedEvents() {
        return nativelyEmittedEvents;
    }

    public void setNativelyEmittedEvents(Set<String> nativelyEmittedEvents) {
        this.nativelyEmittedEvents = nativelyEmittedEvents;
    }

    public Integer getDefaultPushEndpointConnectTimeoutMillis() {
        return defaultPushEndpointConnectTimeoutMillis;
    }

    public void setDefaultPushEndpointConnectTimeoutMillis(Integer defaultPushEndpointConnectTimeoutMillis) {
        this.defaultPushEndpointConnectTimeoutMillis = defaultPushEndpointConnectTimeoutMillis;
    }

    public Integer getDefaultPushEndpointSocketTimeoutMillis() {
        return defaultPushEndpointSocketTimeoutMillis;
    }

    public void setDefaultPushEndpointSocketTimeoutMillis(Integer defaultPushEndpointSocketTimeoutMillis) {
        this.defaultPushEndpointSocketTimeoutMillis = defaultPushEndpointSocketTimeoutMillis;
    }

    public String getDefaultUserSubjectFormat() {
        return defaultUserSubjectFormat;
    }

    public void setDefaultUserSubjectFormat(String defaultUserSubjectFormat) {
        this.defaultUserSubjectFormat = defaultUserSubjectFormat;
    }
}
