package org.keycloak.ssf.transmitter.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 合成 SSF 事件 emit 尝试的结果。调用方（可信 IAM 管理客户端）可见精确派发结果，
 * 便于区分成功推送与被过滤丢弃的事件。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SsfEmitEventResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("jti")
    private String jti;

    @JsonProperty("message")
    private String message;

    public SsfEmitEventResponse() {
    }

    public SsfEmitEventResponse(String status) {
        this.status = status;
    }

    public SsfEmitEventResponse(String status, String jti) {
        this.status = status;
        this.jti = jti;
    }

    public SsfEmitEventResponse(String status, String jti, String message) {
        this.status = status;
        this.jti = jti;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
