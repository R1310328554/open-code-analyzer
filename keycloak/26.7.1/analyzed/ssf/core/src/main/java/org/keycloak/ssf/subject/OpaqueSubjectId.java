package org.keycloak.ssf.subject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 9493 opaque 格式的主体标识符，以不透明字符串标识主体。
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493#name-opaque-identifier-format</p>
 */
public class OpaqueSubjectId extends SubjectId {

    public static final String TYPE = "opaque";

    /** 不透明主体标识字符串。 */
    @JsonProperty("id")
    protected String id;

    public OpaqueSubjectId() {
        super(TYPE);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "OpaqueSubjectId{" +
               "id='" + id + '\'' +
               '}';
    }
}
