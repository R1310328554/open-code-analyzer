package org.keycloak.testframework.injection;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 测试框架托管实例的供应器契约。
 * <p>
 * 实现类负责创建值、声明兼容性、生命周期及可选依赖；由 {@link Extensions} 加载并由 {@link Registry} 调度。
 *
 * @param <T> 供应的值类型
 * @param <S> 关联的注入注解类型
 */
public interface Supplier<T, S extends Annotation> {

    /** @return 从泛型签名解析的注解类型 */
    default Class<S> getAnnotationClass() {
        //noinspection unchecked
        return (Class<S>) ReflectionUtils.getAnnotationType(this);
    }

    /** @return 从泛型签名解析的值类型 */
    default Class<T> getValueType() {
        //noinspection unchecked
        return (Class<T>) ReflectionUtils.getValueType(this);
    }

    /**
     * 创建或获取托管实例值。
     *
     * @param instanceContext 部署上下文，可用于解析 {@link #getDependencies} 声明的依赖
     * @return 实例值
     */
    T getValue(InstanceContext<T, S> instanceContext);

    /** 从注解读取 {@code ref} 属性并规范空串为 {@code null}。 */
    default String getRef(S annotation) {
        return StringUtil.convertEmptyToNull(SupplierHelpers.getAnnotationField(annotation, AnnotationFields.REF));
    }

    /** 从注解读取 {@code lifecycle}，缺省时使用 {@link #getDefaultLifecycle()}。 */
    default LifeCycle getLifeCycle(S annotation) {
        return SupplierHelpers.getAnnotationField(annotation, AnnotationFields.LIFECYCLE, getDefaultLifecycle());
    }

    /** @return 默认生命周期 {@link LifeCycle#CLASS} */
    default LifeCycle getDefaultLifecycle() {
        return LifeCycle.CLASS;
    }

    /**
     * 判断已部署实例能否复用于新的请求（生命周期与配置一致）。
     *
     * @param a 已部署实例上下文
     * @param b 新请求实例
     * @return 兼容则 {@code true}
     */
    boolean compatible(InstanceContext<T, S> a, RequestedInstance<T, S> b);

    /** 实例销毁时的清理钩子，默认无操作。 */
    default void close(InstanceContext<T, S> instanceContext) {
    }

    /** @return 供应器配置别名，默认为简单类名 */
    default String getAlias() {
        return getClass().getSimpleName();
    }

    /** 每个测试方法 {@code beforeEach} 完成后、测试执行前的回调。 */
    default void onBeforeEach(InstanceContext<T, S> instanceContext) {
    }

    /** @return 部署与销毁顺序，见 {@link SupplierOrder} */
    default int order() {
        return SupplierOrder.DEFAULT;
    }

    /**
     * 声明本实例部署前必须满足的依赖；默认无依赖。
     *
     * @param instanceContext 当前请求上下文
     * @return 依赖列表
     */
    default List<Dependency> getDependencies(RequestedInstance<T, S> instanceContext) {
        return List.of();
    }

}
