package org.keycloak.scim.model.filter;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import org.keycloak.scim.resource.schema.attribute.Attribute;

/**
 * 将 SCIM 属性映射为 JPA {@link Expression} 的函数式接口。
 *
 * <p>用于在 SCIM 过滤器转换为 JPA 查询时，动态解析各属性对应的 Criteria API 表达式。</p>
 */
public interface ScimAttributeJpaExpressionResolver {

    /**
     * 为给定 {@code attribute} 解析 JPA {@link Expression}。
     *
     * @param attribute 待解析的 SCIM 属性
     * @param cb Criteria 构建器
     * @param root 查询根实体
     * @param joinResolver 按类型解析或创建 {@link Join} 的函数；若 Join 不存在，应通过提供的 Supplier 创建
     * @return 对应属性的 JPA 表达式
     */
    Expression<?> getAttributeExpression(Attribute<?, ?> attribute, CriteriaBuilder cb, Root<?> root, BiFunction<Class<?>, Supplier<Join<?, ?>>, Join<?, ?>> joinResolver);
}
