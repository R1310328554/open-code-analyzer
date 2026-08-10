package org.keycloak.testframework;

import java.util.List;

import org.keycloak.testframework.clustering.LoadBalancerSupplier;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.server.ClusteredKeycloakServerSupplier;

/**
 * 集群测试框架扩展，向测试框架注册 {@link ClusteredKeycloakServerSupplier} 与 {@link LoadBalancerSupplier}。
 */
public class ClusteringTestFrameworkExtension implements TestFrameworkExtension {

    @Override
    /** 返回集群测试所需的 Supplier 列表。 */
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new ClusteredKeycloakServerSupplier(), new LoadBalancerSupplier());
    }
}
