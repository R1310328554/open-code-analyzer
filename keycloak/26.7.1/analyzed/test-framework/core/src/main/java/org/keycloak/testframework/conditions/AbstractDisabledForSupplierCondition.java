package org.keycloak.testframework.conditions;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.keycloak.testframework.injection.Extensions;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * 基于 Supplier 别名的 JUnit 5 {@link ExecutionCondition} 抽象基类。
 * 当当前选中的 Supplier 别名出现在排除列表时禁用测试。
 */
abstract class AbstractDisabledForSupplierCondition implements ExecutionCondition {

    /** 解析注解中的排除 Supplier 列表并与当前选中 Supplier 比较。 */
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Extensions extensions = Extensions.getInstance();

        Class<?> valueType = valueType();
        String valueTypeAlias = extensions.getValueTypeAlias().getAlias(valueType);

        Annotation annotation = getAnnotation(context, annotation());
        String[] excludedSuppliers = SupplierHelpers.getAnnotationField(annotation, "value");

        Supplier<?, ?> supplier = extensions.findSupplierByType(valueType);

        boolean excluded = Arrays.asList(excludedSuppliers).contains(supplier.getAlias());

        if (excluded) {
            return ConditionEvaluationResult.disabled("Disabled for " + valueTypeAlias + " " + supplier.getAlias());
        } else {
            return ConditionEvaluationResult.enabled("Enabled for " + valueTypeAlias + " " + supplier.getAlias());
        }
    }

    /** @return 受条件约束的值类型（如 {@link org.keycloak.testframework.server.KeycloakServer}） */
    abstract Class<?> valueType();

    /** @return 携带 {@code value} 排除列表的注解类型 */
    abstract Class<? extends Annotation> annotation();

    /** 从测试方法或类上读取条件注解（方法优先，否则回退到类）。 */
    private <T extends Annotation> T getAnnotation(ExtensionContext context, Class<T> annotationClass) {
        T[] annotations = context.getElement().get().getAnnotationsByType(annotationClass);
        if (annotations.length == 0) {
            annotations = context.getParent().get().getElement().get().getAnnotationsByType(annotationClass);
        }
        return annotations[0];
    }
}
