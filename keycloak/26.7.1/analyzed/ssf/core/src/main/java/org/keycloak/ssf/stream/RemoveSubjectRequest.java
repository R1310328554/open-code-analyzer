package org.keycloak.ssf.stream;

import org.keycloak.ssf.subject.SubjectId;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 从 SSF 流中移除主体的管理 API 请求体。
 * <p>对应 SSF Management API 的 remove-subject 操作。</p>
 */
public class RemoveSubjectRequest {
        /** REQUIRED. 标识待移除主体所属流的 stream_id。 */
        @JsonProperty("stream_id")
        private String streamId;

        /** REQUIRED. 标识待移除主体的 Subject 声明（{@link SubjectId}）。 */
        @JsonProperty("subject")
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
