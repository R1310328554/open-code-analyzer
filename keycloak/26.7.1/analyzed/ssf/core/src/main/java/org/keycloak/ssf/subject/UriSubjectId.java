package org.keycloak.ssf.subject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 9493 uri 格式的主体标识符，以 URI 标识主体。
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493#section-3.2.7</p>
 */
public class UriSubjectId extends SubjectId {

    public static final String TYPE = "uri";

    /** 主体的 URI 标识。 */
    @JsonProperty("uri")
    protected String uri;

    public UriSubjectId() {
        super(TYPE);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "UriSubjectId{" +
               "uri='" + uri + '\'' +
               '}';
    }
}
