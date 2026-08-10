package org.keycloak.ssf.transmitter.delivery.poll;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 8936 poll 端点的错误响应体。poll 规范要求 {@code err} / {@code description} 键对（§2.4.4），
 * 与 SSF 发送方其余部分通过 {@link org.keycloak.ssf.transmitter.support.SsfErrorRepresentation
 * SsfErrorRepresentation} 使用的 OAuth 风格 {@code error} / {@code error_description} 不同。
 * 本 DTO 承载 poll 专用形状，严格 RFC 8936 接收方无需转换。
 *
 * <p>请求体 {@code setErrs} 映射（{@link PollRequest}）已使用相同键对，本表示使请求与响应错误信封对称。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PollErrorRepresentation {

    @JsonProperty("err")
    private String err;

    @JsonProperty("description")
    private String description;

    public PollErrorRepresentation() {
    }

    public PollErrorRepresentation(String err, String description) {
        this.err = err;
        this.description = description;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
