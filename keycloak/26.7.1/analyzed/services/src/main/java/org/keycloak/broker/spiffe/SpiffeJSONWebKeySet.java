package org.keycloak.broker.spiffe;

import org.keycloak.jose.jwk.JSONWebKeySet;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 扩展标准 {@link JSONWebKeySet}，携带 SPIFFE 工作负载 API 返回的
 * {@code spiffe_refresh_hint} 刷新提示字段。
 */
public class SpiffeJSONWebKeySet extends JSONWebKeySet {

    /** SPIFFE JWKS 建议的密钥刷新间隔（秒）。 */
    @JsonProperty("spiffe_refresh_hint")
    private Long spiffeRefreshHint;

    /** @return spiffe_refresh_hint 值，可能为 null */
    public Long getSpiffeRefreshHint() {
        return spiffeRefreshHint;
    }

    /** 设置 SPIFFE 密钥刷新提示。 */
    public void setSpiffeRefreshHint(Long spiffeRefreshHint) {
        this.spiffeRefreshHint = spiffeRefreshHint;
    }
}
