package org.keycloak.testframework.events;

import java.lang.annotation.Annotation;
import java.util.List;

import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.realm.RealmConfigInterceptor;

@SuppressWarnings("rawtypes")
/**
 * 事件收集器 {@link Supplier} 抽象基类。
 * <p>
 * 按注解中的 {@code realmRef} 解析 {@link ManagedRealm}，创建对应 {@link AbstractEvents} 实例，
 * 并在每个测试前后重置或清理事件状态。
 *
 * @param <E> 具体事件收集器类型
 * @param <A> 注入注解类型
 */
public abstract class AbstractEventsSupplier<E extends AbstractEvents, A extends Annotation> implements Supplier<E, A>, RealmConfigInterceptor<E, A> {

    /** {@inheritDoc} 声明对 {@link ManagedRealm} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<E, A> instanceContext) {
        return DependenciesBuilder.create(ManagedRealm.class, SupplierHelpers.getAnnotationField(instanceContext.getAnnotation(), "realmRef")).build();
    }

    /** {@inheritDoc} 解析 realm 并创建事件收集器实例。 */
    @Override
    public E getValue(InstanceContext<E, A> instanceContext) {
        String realmRef = SupplierHelpers.getAnnotationField(instanceContext.getAnnotation(), "realmRef");
        ManagedRealm realm = instanceContext.getDependency(ManagedRealm.class, realmRef);
        return createValue(realm);
    }

    /** {@inheritDoc} 默认生命周期为 {@link LifeCycle#GLOBAL}。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    /** {@inheritDoc} 事件供应器实例始终互相兼容。 */
    @Override
    public boolean compatible(InstanceContext<E, A> a, RequestedInstance<E, A> b) {
        return true;
    }

    /** {@inheritDoc} 每个测试前重置事件时间窗口。 */
    @Override
    public void onBeforeEach(InstanceContext<E, A> instanceContext) {
        instanceContext.getValue().testStarted();
    }

    /** {@inheritDoc} 关闭时清空本地与远程事件。 */
    @Override
    public void close(InstanceContext<E, A> instanceContext) {
        instanceContext.getValue().clear();
    }

    /** 由子类根据 realm 创建具体 {@link AbstractEvents} 实例。 */
    protected abstract E createValue(ManagedRealm realm);

}
