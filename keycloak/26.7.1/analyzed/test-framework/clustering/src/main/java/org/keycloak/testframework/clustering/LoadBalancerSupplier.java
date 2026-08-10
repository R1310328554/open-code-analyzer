package org.keycloak.testframework.clustering;

import java.util.List;

import org.keycloak.testframework.annotations.InjectLoadBalancer;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierOrder;
import org.keycloak.testframework.server.ClusteredKeycloakServer;
import org.keycloak.testframework.server.KeycloakServer;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;
import org.keycloak.testframework.server.KeycloakServerConfigInterceptor;

/**
 * 测试框架 {@link Supplier}，解析 {@link InjectLoadBalancer} 注入并配置 hostname 指向负载均衡器。
 * 要求依赖 {@link ClusteredKeycloakServer}。
 */
public class LoadBalancerSupplier implements Supplier<LoadBalancer, InjectLoadBalancer>, KeycloakServerConfigInterceptor<LoadBalancer, InjectLoadBalancer> {

    @Override
    /** 从集群服务器获取已创建的 {@link LoadBalancer}。 */
    public LoadBalancer getValue(InstanceContext<LoadBalancer, InjectLoadBalancer> instanceContext) {
        KeycloakServer server = instanceContext.getDependency(KeycloakServer.class);

        if (server instanceof ClusteredKeycloakServer clusteredKeycloakServer) {
            return clusteredKeycloakServer.getLoadBalancer();
        }

        throw new IllegalStateException("Load balancer can only be used with ClusteredKeycloakServer");
    }

    @Override
    /** 负载均衡器实例在测试间始终兼容复用。 */
    public boolean compatible(InstanceContext<LoadBalancer, InjectLoadBalancer> a, RequestedInstance<LoadBalancer, InjectLoadBalancer> b) {
        return true;
    }

    @Override
    /** 在领域创建之前初始化（{@link SupplierOrder#BEFORE_REALM}）。 */
    public int order() {
        return SupplierOrder.BEFORE_REALM;
    }

    @Override
    /** 将 Keycloak hostname 选项设为 {@link LoadBalancer#HOSTNAME}。 */
    public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder serverConfig, InstanceContext<LoadBalancer, InjectLoadBalancer> instanceContext) {
        return serverConfig.option("hostname", LoadBalancer.HOSTNAME);
    }

    @Override
    /** 声明对 {@link KeycloakServer} 的依赖。 */
    public List<Dependency> getDependencies(RequestedInstance<LoadBalancer, InjectLoadBalancer> instanceContext) {
        return DependenciesBuilder.create(KeycloakServer.class).build();
    }
}
