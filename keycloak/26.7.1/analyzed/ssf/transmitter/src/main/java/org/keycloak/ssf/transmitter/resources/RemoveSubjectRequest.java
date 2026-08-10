package org.keycloak.ssf.transmitter.resources;

import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.subject.SubjectIdJsonDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 从 SSF 流移除主体的请求体（SSF §8.1.3.3）。
 * 携带 stream_id 与 subject。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoveSubjectRequest {

    /** 目标流标识符。 */
    @JsonProperty("stream_id")
    private String streamId;

    /** 要移除的 SSF 主体标识。 */
    @JsonProperty("subject")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    private SubjectId subject;

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
}
