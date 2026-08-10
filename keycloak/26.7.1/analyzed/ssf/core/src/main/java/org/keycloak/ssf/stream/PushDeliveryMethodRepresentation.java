package org.keycloak.ssf.stream;

import java.net.URI;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PUSH 投递方式的流配置表示：发送方通过 HTTP POST 将 SET 推送到接收方端点。
 * <p>参见 SSF 规范 10.3.1.1：https://openid.net/specs/openid-sharedsignals-framework-1_0.html#section-10.3.1.1</p>
 */
public class PushDeliveryMethodRepresentation extends AbstractDeliveryMethodRepresentation {


    /**
     * {@code authorization_header}：若配置存在，发送方在每次事件投递时 MUST 设置的 HTTP Authorization 头。
     * 该值可选，由接收方提供。
     */
    @JsonProperty("authorization_header")
    protected String authorizationHeader;

    /**
     * @param endpointUrl 接收方 MUST 提供的推送端点 URL
     * @param authorizationHeader 接收方 MAY 提供的 Authorization 头值
     */
    public PushDeliveryMethodRepresentation(URI endpointUrl, String authorizationHeader) {
        super(DeliveryMethod.PUSH, Objects.requireNonNull(endpointUrl, "endpointUrl"));
        this.authorizationHeader = authorizationHeader;
    }

    @Override
    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    @Override
    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }
}
