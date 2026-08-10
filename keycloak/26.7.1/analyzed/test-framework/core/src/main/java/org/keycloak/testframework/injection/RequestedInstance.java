package org.keycloak.testframework.injection;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 单个测试方法内待部署的托管实例请求描述。
 * <p>
 * 由 {@link Registry} 在 {@code beforeEach} 阶段从注解、字段或隐式依赖扫描得到，
 * 部署后转为 {@link InstanceContext}。
 *
 * @param <T> 值类型
 * @param <A> 注入注解类型
 */
public class RequestedInstance<T, A extends Annotation> {

    /** 请求实例 ID（基于 hashCode）。 */
    private final int instanceId;
    /** 匹配的供应器。 */
    private final Supplier<T, A> supplier;
    /** 触发请求的注解实例。 */
    private final A annotation;
    /** 等待本实例部署后才能部署的依赖方。 */
    private final Set<InstanceContext<?, ?>> dependents = new HashSet<>();
    /** 请求的值类型。 */
    private final Class<? extends T> valueType;
    /** 请求的生命周期。 */
    private final LifeCycle lifeCycle;
    /** 实例引用标识。 */
    private final String ref;
    /** 由依赖图解析器填充的声明依赖列表。 */
    private List<Dependency> declaredDependencies;

    /**
     * 构造请求实例。
     *
     * @param supplier 供应器
     * @param annotation 注入注解
     * @param valueType 值类型
     */
    public RequestedInstance(Supplier<T, A> supplier, A annotation, Class<? extends T> valueType) {
        this.instanceId = this.hashCode();
        this.supplier = supplier;
        this.annotation = annotation;
        this.valueType = valueType;
        this.lifeCycle = supplier.getLifeCycle(annotation);
        this.ref = StringUtil.convertEmptyToNull(supplier.getRef(annotation));
    }

    /** @return 实例 ID */
    public int getInstanceId() {
        return instanceId;
    }

    /** @return 供应器 */
    public Supplier<T, A> getSupplier() {
        return supplier;
    }

    /** @return 注入注解 */
    public A getAnnotation() {
        return annotation;
    }

    /** @return 值类型 */
    public Class<? extends T> getValueType() {
        return valueType;
    }

    /** @return 生命周期 */
    public LifeCycle getLifeCycle() {
        return lifeCycle;
    }

    /** @return 引用标识 */
    public String getRef() {
        return ref;
    }

    /** 登记依赖本请求的其他实例上下文。 */
    public void registerDependent(InstanceContext<?, ?> instanceContext) {
        dependents.add(instanceContext);
    }

    /** @return 依赖方集合 */
    public Set<InstanceContext<?, ?>> getDependents() {
        return dependents;
    }

    /** @return 声明的依赖列表 */
    public List<Dependency> getDeclaredDependencies() {
        return declaredDependencies;
    }

    /** 设置由依赖图解析得到的声明依赖。 */
    public void setDeclaredDependencies(List<Dependency> declaredDependencies) {
        this.declaredDependencies = declaredDependencies;
    }
}
