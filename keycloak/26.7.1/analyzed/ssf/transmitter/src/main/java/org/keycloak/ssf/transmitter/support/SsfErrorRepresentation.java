package org.keycloak.ssf.transmitter.support;

import java.util.Map;

import org.keycloak.representations.idm.OAuth2ErrorRepresentation;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 扩展 OAuth2 风格错误体，可选携带与具体错误码关联的 {@code params} 结构化字段映射。
 * 调用方（如管理 UI）据此参数化翻译消息，无需解析 {@code error_description}。
 * 为 null/空时从 JSON 省略，简单错误保持标准 {@code {error, error_description}} 形态。
 */
public class SsfErrorRepresentation extends OAuth2ErrorRepresentation {

    /** 与错误码关联的结构化参数字段。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> params;

    /** 无参构造。 */
    public SsfErrorRepresentation() {}

    /** @param error 错误码 @param errorDescription 错误描述 */
    public SsfErrorRepresentation(String error, String errorDescription) {
        super(error, errorDescription);
    }

    /** @param error 错误码 @param errorDescription 错误描述 @param params 结构化参数 */
    public SsfErrorRepresentation(String error, String errorDescription, Map<String, String> params) {
        super(error, errorDescription);
        this.params = params;
    }

    /** 返回结构化参数字段。 */
    public Map<String, String> getParams() {
        return params;
    }

    /** 设置结构化参数字段。 */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
