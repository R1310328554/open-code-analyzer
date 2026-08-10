package org.keycloak.testframework.remote.timeoffset;

import java.util.List;

import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierOrder;
import org.keycloak.testframework.remote.RemoteProviders;
import org.keycloak.testframework.server.KeycloakUrls;

import org.apache.http.client.HttpClient;

/**
 * 为 {@link InjectTimeOffSet} 注解提供 {@link TimeOffSet} 实例的供应器。
 * <p>
 * 测试结束时若偏移已变更，会自动重置为 0。
 */
public class TimeOffsetSupplier implements Supplier<TimeOffSet, InjectTimeOffSet> {

    /** {@inheritDoc} 声明对 HTTP 客户端、远程组件及 Keycloak URL 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<TimeOffSet, InjectTimeOffSet> instanceContext) {
        return DependenciesBuilder.create(HttpClient.class)
                .add(RemoteProviders.class).add(KeycloakUrls.class).build();
    }

    /** {@inheritDoc} 根据注解配置创建并初始化时间偏移控制器。 */
    @Override
    public TimeOffSet getValue(InstanceContext<TimeOffSet, InjectTimeOffSet> instanceContext) {
        var httpClient = instanceContext.getDependency(HttpClient.class);
        var remoteProviders = instanceContext.getDependency(RemoteProviders.class);
        KeycloakUrls keycloakUrls = instanceContext.getDependency(KeycloakUrls.class);

        int initOffset = instanceContext.getAnnotation().offset();
        boolean caches = instanceContext.getAnnotation().enableForCaches();
        return new TimeOffSet(httpClient, keycloakUrls.getMasterRealm(), initOffset, caches);
    }

    /** {@inheritDoc} 所有 {@link InjectTimeOffSet} 实例均视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<TimeOffSet, InjectTimeOffSet> a, RequestedInstance<TimeOffSet, InjectTimeOffSet> b) {
        return true;
    }

    /** {@inheritDoc} 默认按测试方法生命周期管理偏移实例。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.METHOD;
    }

    /** {@inheritDoc} 若偏移已变更则重置为 0，避免影响后续测试。 */
    @Override
    public void close(InstanceContext<TimeOffSet, InjectTimeOffSet> instanceContext) {
        TimeOffSet timeOffSet = instanceContext.getValue();
        if (timeOffSet.hasChanged()) {
            timeOffSet.set(0);
        }
    }

    /** {@inheritDoc} 在 Keycloak 服务器启动前完成时间偏移准备。 */
    @Override
    public int order() {
        return SupplierOrder.BEFORE_KEYCLOAK_SERVER;
    }

}
