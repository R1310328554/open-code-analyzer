package org.keycloak.ssf.stream;

import org.keycloak.ssf.subject.SubjectId;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Receiver 向 Transmitter 添加 subject 订阅的请求体。
 * <p>用于 SSF add-subject 端点。</p>
 */
public class AddSubjectRequest {

    /** REQUIRED。目标流标识字符串。 */

    @JsonProperty("stream_id")
    private String streamId;

    /** REQUIRED。待添加的安全主体 Subject 声明。 */

    @JsonProperty("subject")
    private SubjectId subject;

    /** OPTIONAL。{@code true} 表示 Receiver 已验证 Subject；{@code false} 表示未验证；省略时 Transmitter SHOULD 假定已验证。 */

    @JsonProperty("verified")
    private Boolean verified;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public SubjectId getSubject() {
        return subject;
    }

    public void setSubject(SubjectId subject) {
        this.subject = subject;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}
