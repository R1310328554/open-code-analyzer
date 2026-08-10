package org.keycloak.models.cache.infinispan;

import java.util.function.Supplier;

/**
 * 惰性模型包装器，实现 {@link Supplier}，首次调用 {@link #get()} 时才从底层 supplier 加载模型。
 * <p>
 * 用于缓存适配器延迟获取数据库委托对象，避免不必要的 DB 访问。
 */
public class LazyModel<M> implements Supplier<M> {

    /** 底层模型供应器。 */
    private final Supplier<M> supplier;
    /** 已加载的模型实例，null 表示尚未加载。 */
    private M model;

    /** 构造惰性模型，绑定底层供应器。 */
    public LazyModel(Supplier<M> supplier) {
        this.supplier = supplier;
    }

    /** 首次调用时加载并缓存模型，后续直接返回缓存实例。 */
    @Override
    public M get() {
        if (model == null) {
            model = supplier.get();
        }
        return model;
    }
}
