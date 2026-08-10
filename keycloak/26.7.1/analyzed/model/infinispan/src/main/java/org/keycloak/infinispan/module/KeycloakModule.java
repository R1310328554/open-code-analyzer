package org.keycloak.infinispan.module;

import org.keycloak.jgroups.certificates.CertificateReloadManager;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.annotations.InfinispanModule;
import org.infinispan.factories.impl.BasicComponentRegistry;
import org.infinispan.lifecycle.ModuleLifecycle;

/**
 * Keycloak 自定义 Infinispan 模块生命周期钩子。
 * <p>
 * 在 {@link org.infinispan.remoting.transport.jgroups.JGroupsTransport} 启动之前
 * 提前启动 {@link CertificateReloadManager}，确保 JGroups mTLS 证书在集群通信建立前就绪。
 */
@InfinispanModule(name = "keycloak", requiredModules = {"core"})
public class KeycloakModule implements ModuleLifecycle {

    /** {@inheritDoc} 在缓存管理器启动阶段激活证书重载管理器。 */
    @Override
    public void cacheManagerStarting(GlobalComponentRegistry gcr, GlobalConfiguration globalConfiguration) {
        // 在 JGroupsTransport 启动前先启动证书重载管理器
        //noinspection removal
        gcr.getComponent(BasicComponentRegistry.class)
                .getComponent(CertificateReloadManager.class)
                .running();
    }
}
