package org.keycloak.testframework.injection;

import java.util.LinkedList;
import java.util.List;

/**
 * 拦截器链执行的抽象辅助基类。
 * <p>
 * 从 {@link Registry} 收集已部署与已请求的拦截器 {@link Supplier}，按序对给定值应用 {@link #intercept}。
 *
 * @param <I> 拦截器接口类型
 * @param <V> 被拦截的值类型
 */
public abstract class AbstractInterceptorHelper<I, V> {

    /** 测试框架实例注册表。 */
    private final Registry registry;
    /** 拦截器接口 Class，用于筛选供应器。 */
    private final Class<?> interceptorClass;
    /** 待执行的拦截器列表（已部署 + 已请求）。 */
    private final List<Interception> interceptions = new LinkedList<>();

    /**
     * 扫描注册表并收集所有匹配的拦截器。
     *
     * @param registry 实例注册表
     * @param interceptorClass 拦截器接口类型
     */
    public AbstractInterceptorHelper(Registry registry, Class<I> interceptorClass) {
        this.registry = registry;
        this.interceptorClass = interceptorClass;

        registry.getDeployedInstances().stream().filter(i -> isInterceptor(i.getSupplier())).forEach(i -> interceptions.add(new Interception(i)));
        registry.getRequestedInstances().stream().filter(r -> isInterceptor(r.getSupplier())).forEach(r -> interceptions.add(new Interception(r)));
    }

    /**
     * 依次应用所有拦截器并注册依赖关系。
     *
     * @param value 初始值
     * @param instanceContext 触发拦截的实例上下文
     * @return 拦截链处理后的值
     */
    public V intercept(V value, InstanceContext<?, ?> instanceContext) {
        for (Interception interception : interceptions) {
            value = intercept(value, interception.supplier, interception.existingInstance);
            registry.getLogger().logIntercepted(value, interception.supplier);
            if (interception.existingInstance != null) {
                interception.existingInstance.registerDependent(instanceContext);
            } else {
                interception.requestedInstance.registerDependent(instanceContext);
            }
        }
        return value;
    }

    /**
     * 由子类实现单个拦截器对值的变换逻辑。
     *
     * @param value 当前值
     * @param supplier 拦截器供应器
     * @param existingInstance 已部署实例上下文，未部署时为 {@code null}
     * @return 拦截后的值
     */
    public abstract V intercept(V value, Supplier<?, ?> supplier, InstanceContext<?, ?> existingInstance);

    /** 判断供应器是否实现目标拦截器接口。 */
    private boolean isInterceptor(Supplier<?, ?> supplier) {
        return interceptorClass.isAssignableFrom(supplier.getClass());
    }

    /** 封装一次拦截所需的供应器与实例/请求上下文。 */
    private static class Interception {

        /** 拦截器供应器。 */
        private final Supplier<?, ?> supplier;
        /** 尚未部署时的请求实例。 */
        private final RequestedInstance<?, ?> requestedInstance;
        /** 已部署的实例上下文。 */
        private final InstanceContext<?, ?> existingInstance;

        /** 基于已部署实例构造拦截项。 */
        public Interception(InstanceContext<?, ?> existingInstance) {
            this.supplier = existingInstance.getSupplier();
            this.requestedInstance = null;
            this.existingInstance = existingInstance;
        }

        /** 基于待部署请求构造拦截项。 */
        public Interception(RequestedInstance<?, ?> requestedInstance) {
            this.supplier = requestedInstance.getSupplier();
            this.requestedInstance = requestedInstance;
            this.existingInstance = null;
        }
    }

}
