package org.keycloak.scim.protocol.response;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SCIM 错误响应体（RFC 7644 第 3.12 节）。
 * <p>服务端在请求失败时返回的标准错误消息格式。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** SCIM Error 消息 Schema URN。 */
    public static final String SCHEMA = "urn:ietf:params:scim:api:messages:2.0:Error";

    /** 消息 Schema 集合。 */
    @JsonProperty("schemas")
    private Set<String> schemas = Set.of(SCHEMA);

    /** HTTP 状态码（字符串形式）。 */
    @JsonProperty("status")
    private String status;

    /** SCIM 错误类型标识（如 {@code invalidFilter}）。 */
    @JsonProperty("scimType")
    private String scimType;

    /** 人类可读的错误详情。 */
    @JsonProperty("detail")
    private String detail;

    /** 供 Jackson 反射使用的无参构造器。 */
    public ErrorResponse() {
        // for reflection
    }

    /**
     * 构造带详情与 HTTP 状态码的错误响应。
     *
     * @param detail 错误详情描述
     * @param status HTTP 状态码
     */
    public ErrorResponse(String detail, int status) {
        this.detail = detail;
        this.status = Integer.toString(status);
    }

    /** 返回消息 Schema 集合。 */
    public Set<String> getSchemas() {
        return schemas;
    }

    /** 设置消息 Schema 集合。 */
    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    /** 返回 HTTP 状态码字符串。 */
    public String getStatus() {
        return status;
    }

    /** 将 HTTP 状态码解析为整数；无效时返回 {@code -1}。 */
    @JsonIgnore
    public int getStatusInt() {
        return status == null ? -1 : Integer.parseInt(status);
    }

    /** 设置 HTTP 状态码字符串。 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 返回 SCIM 错误类型。 */
    public String getScimType() {
        return scimType;
    }

    /** 设置 SCIM 错误类型。 */
    public void setScimType(String scimType) {
        this.scimType = scimType;
    }

    /** 返回错误详情。 */
    public String getDetail() {
        return detail;
    }

    /** 设置错误详情。 */
    public void setDetail(String detail) {
        this.detail = detail;
    }
}
