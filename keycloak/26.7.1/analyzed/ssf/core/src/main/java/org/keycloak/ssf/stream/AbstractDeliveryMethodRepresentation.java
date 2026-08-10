package org.keycloak.ssf.stream;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SET HTTP 投递方式配置的抽象表示。
 * <p>定义见 SSF 规范 SET Token Delivery Using HTTP Profile：
 * https://openid.net/specs/openid-sharedsignals-framework-1_0.html#section-10.3.1.1</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractDeliveryMethodRepresentation {

    /** Receiver 提供，REQUIRED。具体投递方式，取 {@code urn:ietf:rfc:8935}（push）或 {@code urn:ietf:rfc:8936}（poll）之一，不可同时使用。 */

    @JsonProperty("method")
    private final DeliveryMethod method;

    /**
     * {@code endpoint_url}：Receiver 设置的 HTTP POST 推送 URL。
     * <p>若 Receiver 从同一 Transmitter 使用多条流且需隔离 SET，
     * RECOMMENDED 为每条流使用唯一 URL。</p>
     */
    @JsonProperty("endpoint_url")
    private final URI endpointUrl;

    /**
     * {@code authorization_header}：配置存在时 Transmitter 每次投递 MUST 设置的 HTTP Authorization 头。
     * <p>值可选，由 Receiver 设置。</p>
     */
    @JsonProperty("authorization_header")
    private String authorizationHeader;

    private Map<String, Object> metadata;

    protected AbstractDeliveryMethodRepresentation(DeliveryMethod method, URI endpointUrl) {
        this.method = method;
        this.endpointUrl = endpointUrl;
    }

    public DeliveryMethod getMethod() {
        return method;
    }

    public URI getEndpointUrl() {
        return endpointUrl;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    @JsonAnySetter
    public void setMetadataValue(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public Object getMetadataValue(String key) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return this.metadata.get(key);
    }

    /**
     * Jackson 多态工厂：按 {@code method} 创建 Push 或 Poll 投递方式表示。
     * @param method 投递方式枚举
     * @param endpointUrl 端点 URL
     * @param authorizationHeader 可选 Authorization 头值
     * @return 具体投递方式子类实例
     */
    @JsonCreator
    public static AbstractDeliveryMethodRepresentation create(@JsonProperty("method") DeliveryMethod method, @JsonProperty("endpoint_url") URI endpointUrl, @JsonProperty("authorization_header") String authorizationHeader) {
        switch (method) {
            case PUSH:
                return new PushDeliveryMethodRepresentation(endpointUrl, authorizationHeader);
            case POLL:
                return new PollDeliveryMethodRepresentation(endpointUrl);
            default:
                throw new IllegalArgumentException();
        }
    }
}
