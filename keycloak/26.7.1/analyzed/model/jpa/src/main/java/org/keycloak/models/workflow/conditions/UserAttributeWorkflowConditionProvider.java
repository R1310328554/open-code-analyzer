package org.keycloak.models.workflow.conditions;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserAttributeEntity;
import org.keycloak.models.workflow.ResourceType;
import org.keycloak.models.workflow.WorkflowConditionProvider;
import org.keycloak.models.workflow.WorkflowExecutionContext;
import org.keycloak.models.workflow.WorkflowInvalidStateException;
import org.keycloak.storage.jpa.JpaHashUtils;

import static org.keycloak.common.util.CollectionUtil.collectionEquals;

/**
 * 用户属性工作流条件：判断用户是否拥有指定 {@code key:value} 属性。
 * <p>
 * 支持仅检测属性键存在（{@code key:}）或精确匹配逗号分隔的多值；提供运行时评估与 JPA 子查询谓词。
 */
public class UserAttributeWorkflowConditionProvider implements WorkflowConditionProvider {

    /** 期望的属性键值对字符串（格式 {@code key:value}）。 */
    private final String expectedAttribute;
    private final KeycloakSession session;

    public UserAttributeWorkflowConditionProvider(KeycloakSession session, String expectedAttribute) {
        this.session = session;
        this.expectedAttribute = expectedAttribute;
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        validate();

        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());

        if (user == null) {
            return false;
        }

        String[] parsedKeyValuePair = parseKeyValuePair(expectedAttribute);
        String key = parsedKeyValuePair[0];
        String valuePart = parsedKeyValuePair[1];

        // 仅检测存在性："key:" 表示用户至少有一个该键的属性即满足
        if (valuePart.isEmpty()) {
            List<String> values = user.getAttributes().getOrDefault(key, List.of());
            return !values.isEmpty();
        }

        List<String> values = user.getAttributes().getOrDefault(key, List.of());
        List<String> expectedValues = List.of(valuePart.split(","));

        return collectionEquals(expectedValues, values);
    }

    @Override
    public Predicate toPredicate(CriteriaBuilder cb, CriteriaQuery<String> query, Root<?> path) {
        validate();

        String[] parsedKeyValuePair = parseKeyValuePair(expectedAttribute);
        String attributeName = parsedKeyValuePair[0];
        String valuePart = parsedKeyValuePair[1];

        // 仅检测存在性：要求用户至少有一个该名称的属性
        if (valuePart.isEmpty()) {
            return cb.greaterThan(createTotalCountSubquery(cb, query, path, attributeName), 0L);
        }

        List<String> expectedValues = Arrays.asList(valuePart.split(","));

        // 子查询：统计用户拥有的期望值数量，以确认无缺失值
        Subquery<Long> matchingCountSubquery = query.subquery(Long.class);
        Root<UserAttributeEntity> attrRoot1 = matchingCountSubquery.from(UserAttributeEntity.class);
        matchingCountSubquery.select(cb.count(attrRoot1));

        // 构建值匹配谓词
        // 长度 ≤255：直接比较 value 字段
        // 长度 >255：比较 longValueHash 字段（避免 Oracle NCLOB 比较问题）
        Predicate[] valuePredicates = expectedValues.stream()
                .map(expectedValue -> {
                    if (expectedValue.length() > 255) {
                        // 长值使用哈希比较，避免 Oracle NCLOB 比较异常
                        return cb.equal(attrRoot1.get("longValueHash"), JpaHashUtils.hashForAttributeValue(expectedValue));
                    } else {
                        // 短值直接比较
                        return cb.equal(attrRoot1.get("value"), expectedValue);
                    }
                })
                .toArray(Predicate[]::new);

        matchingCountSubquery.where(
                cb.and(
                        cb.equal(attrRoot1.get("user").get("id"), path.get("id")),
                        cb.equal(attrRoot1.get("name"), attributeName),
                        cb.or(valuePredicates)
                )
        );

        // 子查询：统计用户该属性名的总数，以确认无多余值
        createTotalCountSubquery(cb, query, path, attributeName);

        // 两个计数均须等于期望值数量（精确匹配）
        int expectedCount = expectedValues.size();
        return cb.and(
                cb.equal(matchingCountSubquery, expectedCount),
                cb.equal(createTotalCountSubquery(cb, query, path, attributeName), expectedCount)
        );
    }

    /** 创建统计用户指定属性名总数的子查询。 */
    private Subquery<Long> createTotalCountSubquery(CriteriaBuilder cb, CriteriaQuery<String> query, Root<?> path, String attributeName) {
        Subquery<Long> totalCountSubquery = query.subquery(Long.class);
        Root<UserAttributeEntity> attrRoot = totalCountSubquery.from(UserAttributeEntity.class);
        totalCountSubquery.select(cb.count(attrRoot));
        totalCountSubquery.where(
                cb.and(
                        cb.equal(attrRoot.get("user").get("id"), path.get("id")),
                        cb.equal(attrRoot.get("name"), attributeName)
                )
        );
        return totalCountSubquery;
    }

    @Override
    public void validate() {
        if (expectedAttribute == null) {
            throw new WorkflowInvalidStateException("Expected 'key:value' pair is not set.");
        }
    }

    @Override
    public void close() {

    }

    /**
     * 解析 {@code key:value} 格式的键值对字符串，返回键与值的数组。
     * 使用 {@link Properties#load(java.io.Reader)} 处理转义冒号等边界情况。
     *
     * @param keyValuePair 待解析的键值对字符串
     * @return 第一个元素为键、第二个元素为值的 {@link String} 数组
     */
    public static String[] parseKeyValuePair(String keyValuePair) {
        Properties props = new Properties();
        try {
            props.load(new StringReader(keyValuePair));
        } catch (java.io.IOException e) {
            throw new WorkflowInvalidStateException("Error reading key-value pair " + keyValuePair + ". Expected format 'key:value'");
        }
        String key = props.stringPropertyNames().iterator().next();
        String value = props.getProperty(key);
        return new String[]{key, value};
    }
}
