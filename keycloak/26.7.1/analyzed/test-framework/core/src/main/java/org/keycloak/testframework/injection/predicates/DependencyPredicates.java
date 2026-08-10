package org.keycloak.testframework.injection.predicates;

import java.util.Objects;
import java.util.function.Predicate;

import org.keycloak.testframework.injection.Dependency;

/**
 * 针对 {@link org.keycloak.testframework.injection.Dependency} 的谓词工厂。
 * <p>
 * 供依赖图解析与实例匹配逻辑复用类型/ref 筛选条件。
 */
public interface DependencyPredicates {

    /**
     * 返回精确匹配指定值类型与 ref 的谓词。
     *
     * @param typeClass 依赖值类型
     * @param ref 实例引用标识
     * @return 匹配谓词
     */
    static Predicate<Dependency> matches(Class<?> typeClass, String ref) {
        return d -> d.valueType().equals(typeClass) && Objects.equals(d.ref(), ref);
    }

    /**
     * 返回值类型可赋给 {@code typeClass} 的谓词。
     *
     * @param typeClass 目标类型
     * @return 可赋值匹配谓词
     */
    static Predicate<Dependency> assignableTo(Class<?> typeClass) {
        return d -> typeClass.isAssignableFrom(d.valueType());
    }

}
