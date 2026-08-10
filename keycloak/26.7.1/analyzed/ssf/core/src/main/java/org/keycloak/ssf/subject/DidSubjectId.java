package org.keycloak.ssf.subject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 9493 去中心化标识符（DID）格式的主体标识。
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493#name-decentralized-identifier-di</p>
 */
public class DidSubjectId extends SubjectId {

    public static final String DID = "did";

    /** DID 文档或解析端点的 URL。 */
    @JsonProperty("url")
    protected String url;

    public DidSubjectId() {
        super(DID);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "DidSubjectId{" +
               "url='" + url + '\'' +
               '}';
    }
}
