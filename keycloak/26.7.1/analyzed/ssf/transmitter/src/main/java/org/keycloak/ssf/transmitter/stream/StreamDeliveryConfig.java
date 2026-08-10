package org.keycloak.ssf.transmitter.stream;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 流的投递配置，描述 SET 的投递方式（push/poll）、端点 URL 及鉴权头等。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamDeliveryConfig {

    /** 投递方法 URI（如 push、poll），对应 SSF §6.1。 */
    @JsonProperty("method")
    private String method;

    @JsonProperty("endpoint_url")
    private String endpointUrl;

    @JsonProperty("authorization_header")
    private String authorizationHeader;

    @JsonProperty("additional_parameters")
    private Map<String, Object> additionalParameters;

    public StreamDeliveryConfig() {
    }

    /**
     * 浅拷贝构造函数，供 {@link StreamConfig#StreamConfig(StreamConfig)} 使用，
     * 使草稿投递配置可在不改动已存实例的情况下被修改（例如 {@code finalizePollEndpointUrlIfApplicable}）。
     */
    public StreamDeliveryConfig(StreamDeliveryConfig other) {
        if (other == null) {
            return;
        }
        this.method = other.method;
        this.endpointUrl = other.endpointUrl;
        this.authorizationHeader = other.authorizationHeader;
        this.additionalParameters = other.additionalParameters == null
                ? null
                : new LinkedHashMap<>(other.additionalParameters);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public Map<String, Object> getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(Map<String, Object> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }
}
