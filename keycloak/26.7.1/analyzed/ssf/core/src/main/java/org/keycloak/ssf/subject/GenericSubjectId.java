package org.keycloak.ssf.subject;

/**
 * 通用主体标识符，用于 RFC 9493 未定义或 Keycloak 未显式建模的 {@code format} 值。
 * <p>反序列化时若 {@link SubjectIds#getSubjectIdType(String)} 无法识别 format，则回退为此类型，
 * 未知字段保留在 {@link #getAttributes()} 中。</p>
 */
public class GenericSubjectId extends SubjectId {

    public GenericSubjectId() {
        super(null);
    }

    @Override
    public String toString() {
        return "GenericSubjectId{" +
               "format='" + format + '\'' +
               ", attributes=" + attributes +
               '}';
    }
}
