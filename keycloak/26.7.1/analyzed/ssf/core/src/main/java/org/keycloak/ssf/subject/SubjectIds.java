package org.keycloak.ssf.subject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RFC 9493 主体标识符格式的注册表，维护 {@code format} 字符串到具体 {@link SubjectId} 实现类的映射。
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493</p>
 */
public class SubjectIds {

    /** 所有已知标准 SUBJECT_ID_FORMATS 的 format → 实现类映射。 */
    public final static Map<String, Class<? extends SubjectId>> SUBJECT_ID_FORMAT_TYPES;

    static {
        var map = new HashMap<String, Class<? extends SubjectId>>();
        List.of(//
                new AccountSubjectId(), //
                new AliasesSubjectId(), //
                new ComplexSubjectId(), //
                new DidSubjectId(), //
                new EmailSubjectId(), //
                new IssuerSubjectId(), //
                new JwtSubjectId(), //
                new OpaqueSubjectId(), //
                new PhoneNumberSubjectId(), //
                new SamlAssertionSubjectId(), //
                new UriSubjectId() //
        ).forEach(subjectId -> map.put(subjectId.getFormat(), subjectId.getClass()));
        SUBJECT_ID_FORMAT_TYPES = map;
    }

    /**
     * 根据 format 字符串返回对应的 {@link SubjectId} 实现类；未知 format 时返回 {@link GenericSubjectId}。
     *
     * @param format RFC 9493 定义的 format 判别式
     * @return 具体 SubjectId 类型，或 {@link GenericSubjectId}
     */
    public static Class<? extends SubjectId> getSubjectIdType(String format) {
        var subjectIdType = SUBJECT_ID_FORMAT_TYPES.get(format);
        if (subjectIdType != null) {
            return subjectIdType;
        }
        return GenericSubjectId.class;
    }
}
