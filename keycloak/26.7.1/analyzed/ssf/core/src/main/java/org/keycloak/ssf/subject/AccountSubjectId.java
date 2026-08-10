package org.keycloak.ssf.subject;

/**
 * RFC 9493 account 格式的主体标识符，以 URI 引用账户资源。
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493#name-account-identifier-format</p>
 */
public class AccountSubjectId extends SubjectId {

    public static final String TYPE = "account";

    /** 账户资源的 URI 标识。 */
    protected String uri;

    public AccountSubjectId() {
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
        return "AccountSubjectId{" +
               "uri='" + uri + '\'' +
               '}';
    }
}
