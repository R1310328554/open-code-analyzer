package org.keycloak.ssf.transmitter.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 管理端主体订阅操作的请求体，携带 {@code type}/{@code value} 简写对。
 * <p>用于 {@code /subjects:add} 等管理接口，将 Keycloak 用户或组织解析为 SSF 主体订阅条目。</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SsfAdminSubjectRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("value")
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
