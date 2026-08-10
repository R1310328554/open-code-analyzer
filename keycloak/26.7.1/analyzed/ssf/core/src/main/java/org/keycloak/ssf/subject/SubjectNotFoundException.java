package org.keycloak.ssf.subject;

import org.keycloak.ssf.SsfException;

/**
 * 表示管理员提供的 {@code (subjectType, subjectValue)} 简写无法解析为已知用户或组织。
 * 与通用 {@link SsfException} 区分，以便管理端 emit 接口返回专用的 {@code subject_not_found} 错误码；
 * 并将违规的 {@code subjectType}/{@code subjectValue} 作为结构化字段携带，
 * 供调用方（日志、响应构造）直接使用而无需从消息中反解析。
 */
public class SubjectNotFoundException extends SsfException {

    private final String subjectType;
    private final String subjectValue;

    public SubjectNotFoundException(String subjectType, String subjectValue) {
        super("Subject not found for type=" + subjectType + " value=" + subjectValue);
        this.subjectType = subjectType;
        this.subjectValue = subjectValue;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public String getSubjectValue() {
        return subjectValue;
    }
}
