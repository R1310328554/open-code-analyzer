package org.keycloak.testframework.injection;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 已部署托管实例的运行时上下文。
 * <p>
 * 持有供应器、注解、生命周期、依赖关系及实例值，供 {@link Registry} 与 {@link Supplier} 协作使用。
 *
 * @param <T> 托管值类型
 * @param <A> 注入注解类型
 */
public class InstanceContext<T, A extends Annotation> {

    /** 实例唯一标识（请求时指定或基于 hashCode）。 */
    private final int instanceId;
    /** 所属注册表。 */
    private final Registry registry;
    /** 创建该实例的供应器。 */
    private final Supplier<T, A> supplier;
    /** 触发注入的注解实例。 */
    private final A annotation;
    /** 依赖本实例的其他已部署实例。 */
    private final Set<InstanceContext<?, ?>> dependents = new HashSet<>();
    /** 当前托管对象值。 */
    private T value;
    /** 请求时声明的值类型（可能与供应器泛型不同）。 */
    private Class<? extends T> requestedValueType;
    /** 实例生命周期。 */
    private LifeCycle lifeCycle;
    /** 实例引用标识，用于区分同类型多实例。 */
    private final String ref;
    /** 供应器可附加的临时键值备注。 */
    private final Map<String, Object> notes = new HashMap<>();
    /** 供应器声明的显式依赖列表。 */
    private final List<Dependency> declaredDependencies;

    /**
     * 构造已部署实例上下文。
     *
     * @param instanceId 实例 ID，{@code -1} 时使用 {@link #hashCode()}
     * @param registry 注册表
     * @param supplier 供应器
     * @param annotation 注入注解
     * @param requestedValueType 请求的值类型
     * @param declaredDependencies 声明的依赖
     */
    public InstanceContext(int instanceId, Registry registry, Supplier<T, A> supplier, A annotation, Class<? extends T> requestedValueType, List<Dependency> declaredDependencies) {
        this.instanceId = instanceId != -1 ? instanceId : hashCode();
        this.registry = registry;
        this.supplier = supplier;
        this.annotation = annotation;
        this.requestedValueType = requestedValueType;
        this.lifeCycle = supplier.getLifeCycle(annotation);
        this.ref = StringUtil.convertEmptyToNull(supplier.getRef(annotation));
        this.declaredDependencies = declaredDependencies;
    }

    /** @return 实例 ID */
    public int getInstanceId() {
        return instanceId;
    }

    /**
     * 获取已声明的默认 ref 依赖。
     *
     * @param typeClazz 依赖类型
     * @return 依赖实例值
     */
    public <D> D getDependency(Class<D> typeClazz) {
        return getDependency(typeClazz, null);
    }

    /**
     * 获取已声明的指定 ref 依赖。
     *
     * @param typeClazz 依赖类型
     * @param ref 实例引用标识
     * @return 依赖实例值
     */
    public <D> D getDependency(Class<D> typeClazz, String ref) {
        return registry.getDependency(typeClazz, ref, this);
    }

    /** @return 所属注册表 */
    public Registry getRegistry() {
        return registry;
    }

    /** 由 {@link Registry} 设置实例值。 */
    void setValue(T value) {
        this.value = value;
    }

    /** @return 创建该实例的供应器 */
    public Supplier<T, A> getSupplier() {
        return supplier;
    }

    /** @return 当前托管对象 */
    public T getValue() {
        return value;
    }

    /** @return 请求时声明的值类型 */
    public Class<? extends T> getRequestedValueType() {
        return requestedValueType;
    }

    /** @return 实例生命周期 */
    public LifeCycle getLifeCycle() {
        return lifeCycle;
    }

    /** @return 实例引用标识 */
    public String getRef() {
        return ref;
    }

    /** @return 注入注解实例 */
    public A getAnnotation() {
        return annotation;
    }

    /** @return 依赖本实例的已部署实例集合 */
    public Set<InstanceContext<?, ?>> getDependents() {
        return dependents;
    }

    /** @return 供应器声明的依赖列表 */
    public List<Dependency> getDeclaredDependencies() {
        return declaredDependencies;
    }

    /** 登记依赖本实例的其他实例。 */
    public void registerDependent(InstanceContext<?, ?> instanceContext) {
        dependents.add(instanceContext);
    }

    /** 附加临时备注，供供应器跨阶段共享状态。 */
    public void addNote(String key, Object value) {
        notes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    /** 读取指定键的备注值。 */
    public <N> N getNote(String key, Class<N> type) {
        return (N) notes.get(key);
    }

}
