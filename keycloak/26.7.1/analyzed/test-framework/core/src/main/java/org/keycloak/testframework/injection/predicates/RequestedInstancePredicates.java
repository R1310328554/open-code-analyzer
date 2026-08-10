package org.keycloak.testframework.injection.predicates;

import java.util.Objects;
import java.util.function.Predicate;

import org.keycloak.testframework.injection.RequestedInstance;

/**
 * 针对 {@link org.keycloak.testframework.injection.RequestedInstance} 的谓词工厂。
 * <p>
 * 用于匹配尚未部署、由测试注解触发的请求实例。
 */
public interface RequestedInstancePredicates {

    /**
     * 返回 supplier 值类型与 ref 均精确匹配的谓词。
     *
     * @param typeClass supplier 声明的值类型
     * @param ref 实例引用标识
     * @return 精确匹配谓词
     */
    static Predicate<RequestedInstance<?, ?>> matches(Class<?> typeClass, String ref) {
        return r -> r.getSupplier().getValueType().equals(typeClass) && Objects.equals(r.getRef(), ref);
    }

}
