package org.keycloak.testframework.injection.predicates;

import java.util.Objects;
import java.util.function.Predicate;

import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;

/**
 * 针对 {@link org.keycloak.testframework.injection.InstanceContext} 的谓词工厂。
 * <p>
 * 用于在注册表中按生命周期、值类型或 supplier 类型/ref 筛选已部署实例。
 */
public interface InstanceContextPredicates {

    /**
     * 返回生命周期等于给定值的谓词。
     *
     * @param lifeCycle 目标生命周期
     * @return 生命周期匹配谓词
     */
    static Predicate<InstanceContext<?, ?>> hasLifeCycle(LifeCycle lifeCycle) {
        return i -> i.getLifeCycle().equals(lifeCycle);
    }

    /**
     * 返回实例值属于指定类型的谓词。
     *
     * @param valueTypeClass 期望的值类型
     * @return 类型实例匹配谓词
     */
    static Predicate<InstanceContext<?, ?>> isInstanceof(Class<?> valueTypeClass) {
        return i -> valueTypeClass.isInstance(i.getValue());
    }

    /**
     * 返回 supplier 值类型与 ref 均精确匹配的谓词。
     *
     * @param typeClass supplier 声明的值类型
     * @param ref 实例引用标识
     * @return 精确匹配谓词
     */
    static Predicate<InstanceContext<?, ?>> matches(Class<?> typeClass, String ref) {
        return i -> i.getSupplier().getValueType().equals(typeClass) && Objects.equals(i.getRef(), ref);
    }

}
