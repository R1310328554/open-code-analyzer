package org.keycloak.ssf.subject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 9493 iss_sub 格式的主体标识符，以 OAuth 2.0 发行方（iss）与主体（sub）对标识主体。
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493#name-issuer-and-subject-identifi</p>
 */
public class IssuerSubjectId extends SubjectId {

    public static final String TYPE = "iss_sub";

    /** OAuth 2.0 发行方标识（iss 声明）。 */
    @JsonProperty("iss")
    protected String iss;

    /** OAuth 2.0 主体标识（sub 声明）。 */
    @JsonProperty("sub")
    protected String sub;

    public IssuerSubjectId() {
        super(TYPE);
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    @Override
    public String toString() {
        return "IssuerSubjectId{" +
               "iss='" + iss + '\'' +
               ", sub='" + sub + '\'' +
               '}';
    }
}
