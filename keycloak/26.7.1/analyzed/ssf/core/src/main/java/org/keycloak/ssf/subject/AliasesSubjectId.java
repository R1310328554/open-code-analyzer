package org.keycloak.ssf.subject;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 9493 aliases 格式的主体标识符，携带一组可互换的标识符映射。
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493#name-aliases-identifier-format</p>
 */
public class AliasesSubjectId extends SubjectId {

    public static final String TYPE = "aliases";

    /** 标识符列表，每项为 format→value 的映射，表示同一主体的多种等价标识。 */
    @JsonProperty("identifiers")
    protected List<Map<String, String>> identifiers;

    public AliasesSubjectId() {
        super(TYPE);
    }

    public List<Map<String, String>> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<Map<String, String>> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public String toString() {
        return "AliasesSubjectId{" +
               "identifiers=" + identifiers +
               '}';
    }
}
