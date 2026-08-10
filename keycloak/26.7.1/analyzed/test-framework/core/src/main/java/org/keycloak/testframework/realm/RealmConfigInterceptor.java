package org.keycloak.testframework.realm;

import java.lang.annotation.Annotation;

import org.keycloak.testframework.injection.InstanceContext;

/**
 * Realm 构建过程的拦截器契约。
 * <p>
 * 实现类可在 {@link RealmSupplier} 创建 Realm 前修改 {@link RealmBuilder}，
 * 常用于跨测试模块注入默认 Realm 设置。
 *
 * @param <T> 拦截器关联的托管值类型
 * @param <S> 关联的注入注解类型
 */
public interface RealmConfigInterceptor<T, S extends Annotation> {

    /**
     * 拦截并修改 Realm 构建器。
     *
     * @param realm 当前 Realm 构建器
     * @param instanceContext 触发拦截的实例上下文
     * @return 修改后的构建器
     */
    RealmBuilder intercept(RealmBuilder realm, InstanceContext<T, S> instanceContext);

}
