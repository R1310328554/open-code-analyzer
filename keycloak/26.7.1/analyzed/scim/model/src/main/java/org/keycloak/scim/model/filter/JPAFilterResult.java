package org.keycloak.scim.model.filter;

import jakarta.persistence.criteria.Predicate;

/**
 * 封装 SCIM 过滤表达式求值结果的记录类型，包含生成的 JPA {@link Predicate}
 * 以及过滤器是否不受支持的标志（例如因无法识别的属性）。
 * <p>允许访问者在遇到不支持的过滤条件时优雅降级。</p>
 *
 * @param predicate 由过滤表达式生成的 JPA Predicate
 * @param unsupported 过滤器是否不受支持（{@code true} 表示不支持，{@code false} 表示有效）
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public record JPAFilterResult(Predicate predicate, boolean unsupported) {

    /** 构造有效（受支持）的过滤结果。 */
    public static JPAFilterResult valid(Predicate p) {
        return new JPAFilterResult(p, false);
    }

    /** 构造不受支持但仍携带 Predicate 占位符的过滤结果。 */
    public static JPAFilterResult unsupported(Predicate p) {
        return new JPAFilterResult(p, true);
    }
}
