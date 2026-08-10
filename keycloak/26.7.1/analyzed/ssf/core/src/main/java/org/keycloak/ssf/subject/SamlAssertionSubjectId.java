package org.keycloak.ssf.subject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenID SSE 框架 saml_assertion_id 格式的主体标识符，以 SAML 断言发行方与 assertion_id 标识主体。
 * <p>参见 https://openid.net/specs/openid-sse-framework-1_0.html#sub-id-saml-assertion-id</p>
 */
public class SamlAssertionSubjectId extends SubjectId {

    public static final String TYPE = "saml_assertion_id";

    /** SAML 断言的发行方标识。 */
    @JsonProperty("issuer")
    protected String issuer;

    /** SAML 断言的唯一标识符。 */
    @JsonProperty("assertion_id")
    protected String assertionId;

    public SamlAssertionSubjectId() {
        super(TYPE);
    }

    @Override
    public String toString() {
        return "SamlAssertionSubjectId{" +
               "issuer='" + issuer + '\'' +
               ", assertionId='" + assertionId + '\'' +
               '}';
    }
}
