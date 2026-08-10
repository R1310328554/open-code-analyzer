package org.keycloak.ssf.subject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 9493 email 格式的主体标识符，以电子邮件地址标识主体。
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493#name-email-identifier-format</p>
 */
public class EmailSubjectId extends SubjectId {

    public static final String TYPE = "email";

    /** 主体的电子邮件地址。 */
    @JsonProperty("email")
    protected String email;

    public EmailSubjectId() {
        super(TYPE);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "EmailSubjectId{" +
               "email='" + email + '\'' +
               '}';
    }
}
