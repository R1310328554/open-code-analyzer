package org.keycloak.ssf.transmitter.resources;

import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.subject.SubjectIdJsonDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 向 SSF 流添加主体的请求体（SSF §8.1.3.2）。
 * 携带 stream_id、subject 及可选 verified 标志。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddSubjectRequest {

    /** 目标流标识符。 */
    @JsonProperty("stream_id")
    private String streamId;

    /** 要添加的 SSF 主体标识。 */
    @JsonProperty("subject")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    private SubjectId subject;

    /** 主体是否已验证（可选）。 */
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
