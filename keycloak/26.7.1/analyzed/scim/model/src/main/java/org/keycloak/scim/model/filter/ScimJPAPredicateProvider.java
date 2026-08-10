package org.keycloak.scim.model.filter;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.keycloak.common.util.TriFunction;
import org.keycloak.scim.filter.ScimFilterException;
import org.keycloak.scim.resource.schema.ModelSchema;
import org.keycloak.scim.resource.schema.attribute.Attribute;
import org.keycloak.scim.resource.spi.ScimResourceTypeProvider;

import org.jboss.logging.Logger;

/**
 * 为 SCIM 过滤运算符创建 JPA 谓词。
 * <p>支持根实体直接字段与存储于 {@code attributes} 集合中的自定义属性，并对时间戳、布尔等类型做规范化转换。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class ScimJPAPredicateProvider {

    private static final Logger logger = Logger.getLogger(ScimJPAPredicateProvider.class);

    /** 当前 SCIM 资源类型提供者。 */
    private final ScimResourceTypeProvider resourceTypeProvider;
    /** 用于解析属性元数据的 schema 列表。 */
    private final List<ModelSchema<?, ?>> schemas;
    /** JPA Criteria 构建器。 */
    private final CriteriaBuilder cb;
    /** 查询根实体。 */
    private final Root<?> root;

    @SuppressWarnings("rawtypes,unchecked")
    private final Map<String, TriFunction<CriteriaBuilder, Expression, Object, Predicate>> operatorMap = Map.of(
            "eq", CriteriaBuilder::equal,
            "ne", CriteriaBuilder::notEqual,
            "pr", (cb, exp, val) -> cb.isNotNull(exp),
            "gt", (cb, exp, val) -> cb.greaterThan(exp, asComparable(val)),
            "ge", (cb, exp, val) -> cb.greaterThanOrEqualTo(exp, asComparable(val)),
            "lt", (cb, exp, val) -> cb.lessThan(exp, asComparable(val)),
            "le", (cb, exp, val) -> cb.lessThanOrEqualTo(exp, asComparable(val)),
            "co", (cb, exp, val) -> cb.like(exp.as(String.class), "%" + escapeLike(val.toString()) + "%", '\\'),
            "sw", (cb, exp, val) -> cb.like(exp.as(String.class), escapeLike(val.toString()) + "%", '\\'),
            "ew", (cb, exp, val) -> cb.like(exp.as(String.class), "%" + escapeLike(val.toString()), '\\')
    );

    // 缓存 Join，避免同一过滤条件重复创建关联
    /** 按 Join 类型缓存已创建的关联。 */
    private Map<String, Join<?, ?>> attributeJoin = new HashMap<>();

    public ScimJPAPredicateProvider(ScimResourceTypeProvider resourceTypeProvider, List<ModelSchema<?, ?>> schemas, CriteriaBuilder cb, Root<?> root) {
        this.resourceTypeProvider = resourceTypeProvider;
        this.schemas = schemas;
        this.cb = cb;
        this.root = root;
    }

    /**
     * 为 SCIM 运算符（eq、ne、pr、gt、ge、lt、le、co、sw、ew）创建谓词。
     * <p>先解析属性路径获取元数据，校验运算符是否适用于该类型，规范化比较值，再按直接字段或自定义属性构建谓词。</p>
     *
     * @param path SCIM 属性路径
     * @param operator 比较运算符
     * @param value 字符串形式的比较值（将按属性类型规范化）
     * @return 属性已知且已映射时返回有效 {@link JPAFilterResult}，未知属性返回 unsupported
     */
    public JPAFilterResult createPredicate(String path, String operator, String value) {
        Attribute<?,?> attrInfo = resolve(path);
        if (attrInfo == null) {
            logger.debugf("Filter attribute '%s' could not be resolved to a known SCIM attribute; filter will not match any resources", path);
            return JPAFilterResult.unsupported(cb.disjunction());
        }

        String op = operator.toLowerCase();
        // 在规范化与构建谓词前先校验运算符
        validateOperator(attrInfo, path, op);

        // 规范化值（String → Long、Boolean 等）
        Object normalizedValue = normalizeValue(attrInfo, value);

        // 构建 JPA 谓词
        return JPAFilterResult.valid(getAttributePredicate(attrInfo, op, normalizedValue));
    }

    /**
     * 按属性元数据将过滤表达式中的字符串规范化为正确类型。
     * <p>时间戳属性转为 Long 毫秒；布尔属性转为 Boolean；其他类型保持原字符串。</p>
     *
     * @param attrInfo 属性元数据
     * @param value 原始字符串值
     * @return 规范化后的值
     */
    private Object normalizeValue(Attribute<?,?> attrInfo, String value) {
        if (value == null) return null;
        if (attrInfo.isTimestamp()) return parseDateTime(value);
        if (attrInfo.isBoolean()) return parseBoolean(value);
        return value;
    }

    /**
     * 为给定属性、运算符与值构建 JPA 谓词。
     * <p>直接字段在根实体上应用运算符；自定义属性则 Join {@code attributes} 集合并匹配 name/value。</p>
     *
     * @param attrInfo 属性元数据
     * @param operation 比较运算符
     * @param value 已规范化的比较值
     * @return JPA {@link Predicate}
     */
    private Predicate getAttributePredicate(Attribute<?,?> attrInfo, String operation, Object value) {
        Expression<?> expression = null;
        Predicate basePredicate = null;
        String modelAttributeName = attrInfo.getModelAttributeName();

        try {
            expression = root.get(modelAttributeName);
        } catch (IllegalArgumentException ignore) {
            // 非主属性字段，继续尝试自定义属性或表达式解析器
        }

        if (expression == null) {
            if (resourceTypeProvider instanceof ScimAttributeJpaExpressionResolver mapper) {
                expression = mapper.getAttributeExpression(attrInfo, cb, root, (aClass, joinSupplier) -> getOrCreateAttributeJoin(aClass.getName(), joinSupplier));
            }
        }

        if (expression == null) {
            Join<?, ?> join = getOrCreateAttributeJoin("attributes", createAttributesJoinSupplier());
            expression = join.get("value");
            basePredicate = cb.equal(join.get("name"), modelAttributeName);
        }

        if (value != null && !attrInfo.isCaseExact() && "string".equals(attrInfo.getType())) {
            value = value.toString().toLowerCase();
            expression = cb.lower((Expression<String>) expression);
        }

        Predicate predicate = operatorMap.get(operation).apply(cb, expression, value);
        return (basePredicate != null) ? cb.and(basePredicate, predicate) : predicate;
    }

    /**
     * 获取或创建 Join，优先使用 {@code attributeJoin} 缓存。
     *
     * @return 已存在或新创建的 Join
     */
    private Join<?, ?> getOrCreateAttributeJoin(String type, Supplier<Join<?, ?>> joinFactory) {
        return attributeJoin.computeIfAbsent(type, k -> joinFactory.get());
    }

    /**
     * 校验运算符是否适用于属性类型（如布尔仅支持 eq/ne/pr，时间戳不支持 co/sw/ew）。
     *
     * @param attrInfo 属性元数据
     * @param scimAttribute 原始 SCIM 属性路径（用于错误信息）
     * @param operator 待校验运算符
     * @throws ScimFilterException 运算符不支持时抛出
     */
    private void validateOperator(Attribute<?,?> attrInfo, String scimAttribute, String operator) {
        String op = operator.toLowerCase();

        // 布尔属性：仅允许相等与存在性运算符
        if (attrInfo.isBoolean()) {
            if (!op.equals("eq") && !op.equals("ne") && !op.equals("pr")) {
                throw new ScimFilterException(
                        "Operator '" + operator + "' is not supported for boolean attribute: " + scimAttribute);
            }
        }

        // 时间戳/数值属性：禁止字符串专用运算符
        if (attrInfo.isTimestamp()) {
            if (op.equals("co") || op.equals("sw") || op.equals("ew")) {
                throw new ScimFilterException(
                        "String operators (co, sw, ew) are not supported for timestamp attribute: " + scimAttribute);
            }
        }
    }

    /**
     * 将 ISO 8601 日期时间字符串解析为 {@link Long} 毫秒时间戳。
     *
     * @param dateTimeString 日期时间字符串
     * @return 毫秒时间戳
     * @throws ScimFilterException 格式无效时抛出
     */
    private Long parseDateTime(String dateTimeString) {
        try {
            Instant instant = Instant.parse(dateTimeString);
            return instant.toEpochMilli();
        } catch (DateTimeParseException e) {
            // 非 ISO 8601 时尝试按数字时间戳解析
            try {
                return Long.parseLong(dateTimeString);
            } catch (NumberFormatException nfe) {
                throw new ScimFilterException(
                        "Invalid date/time format: " + dateTimeString +
                                ". Expected ISO 8601 format (e.g., 2011-05-13T04:42:34Z) or timestamp");
            }
        }
    }

    /**
     * 将 "true"/"false" 字符串解析为 {@link Boolean}。
     *
     * @param value 待解析字符串
     * @return 布尔值
     * @throws ScimFilterException 非法布尔字符串时抛出
     */
    private Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        throw new ScimFilterException("Invalid boolean value found in boolean expression: " + value);
    }

    /**
     * 将对象转为 {@link Comparable}，供 gt/ge/lt/le 比较使用。
     *
     * @param val 待转换对象
     * @return Comparable 实例
     * @throws ScimFilterException 不可比较时抛出
     */
    @SuppressWarnings("unchecked")
    private Comparable<Object> asComparable(Object val) {
        if (val instanceof Comparable) {
            return (Comparable<Object>) val;
        }
        throw new ScimFilterException("Value is not comparable: " + val);
    }

    /**
     * 将 SCIM 属性路径解析为 {@link Attribute} 元数据。
     * <p>遍历已注册 schema；未映射到模型字段或未找到时返回 {@code null}。</p>
     *
     * @param path SCIM 属性路径
     * @return 已映射的 Attribute，否则 {@code null}
     */
    public Attribute<?, ?> resolve(String path) {
        Attribute<?, ?> metadata = null;

        for (ModelSchema<?, ?> schema : schemas) {
            metadata = schema.getAttributeByPath(path);
            if (metadata != null    ) {
                break;
            }
        }
        if (metadata != null) {
            String modelAttributeName = metadata.getModelAttributeName();
            if (modelAttributeName != null) {
                return metadata;
            }
        }
        // 未找到属性，返回 null 表示过滤不支持
        return null;
    }

    /**
     * 转义 SQL LIKE 特殊字符（反斜杠、%、下划线）。
     *
     * @param value 原始字符串
     * @return 可用于 LIKE 的安全字符串
     */
    private String escapeLike(String value) {
        // 转义 SQL LIKE 特殊字符
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /** 创建指向 {@code attributes} 集合的 LEFT JOIN 供应器。 */
    private Supplier<Join<?, ?>> createAttributesJoinSupplier() {
        return () -> root.join("attributes", JoinType.LEFT);
    }
}
