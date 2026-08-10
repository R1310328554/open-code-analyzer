package org.keycloak.ssf.event.token;

import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.subject.SubjectIdJsonDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * RFC 8417 安全事件令牌（SET）的标准 SSF 实现。
 * <p>在 {@link AbstractSecurityEventToken} 基础上增加 {@code sub_id} 与 {@code txn} 声明。</p>
 * <p>定义见 https://datatracker.ietf.org/doc/html/rfc8417</p>
 */
public class SsfSecurityEventToken extends AbstractSecurityEventToken {

    /** 安全主体标识（Subject Identifier）。 */
    @JsonProperty("sub_id")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId subjectId;

    /** 可选事务标识，用于关联同一逻辑操作产生的多个事件。 */
    @JsonProperty("txn")
    protected String txn;

    public SubjectId getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(SubjectId subjectId) {
        this.subjectId = subjectId;
    }

    /** 流式设置 {@link #subjectId} 并返回 {@code this}。 */
    public SsfSecurityEventToken subjectId(SubjectId subjectId) {
        setSubjectId(subjectId);
        return this;
    }

    /** 流式设置 {@link #txn} 并返回 {@code this}。 */
    public SsfSecurityEventToken txn(String txn) {
        setTxn(txn);
        return this;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }
}
