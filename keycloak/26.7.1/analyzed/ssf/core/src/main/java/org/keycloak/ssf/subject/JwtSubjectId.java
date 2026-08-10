package org.keycloak.ssf.subject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenID SSE 框架 jwt_id 格式的主体标识符，以 JWT 发行方（iss）与 JWT ID（jti）标识主体。
 * <p>参见 https://openid.net/specs/openid-sse-framework-1_0.html#sub-id-jwt-id</p>
 */
public class JwtSubjectId extends SubjectId {

    public static final String TYPE = "jwt_id";

    /** JWT 发行方标识。 */
    @JsonProperty("iss")
    protected String iss;

    /** JWT 唯一标识符（jti）。 */
    @JsonProperty("jti")
    protected String jti;

    public JwtSubjectId() {
        super(TYPE);
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    @Override
    public String toString() {
        return "JwtSubjectId{" +
               "iss='" + iss + '\'' +
               ", jti='" + jti + '\'' +
               '}';
    }
}
