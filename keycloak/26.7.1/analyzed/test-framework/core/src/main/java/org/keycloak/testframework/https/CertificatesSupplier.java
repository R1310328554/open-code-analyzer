package org.keycloak.testframework.https;

import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.injection.SupplierOrder;

/**
 * 托管测试证书的 {@link Supplier} 实现。
 * <p>
 * 根据 {@link InjectCertificates#config()} 及可选的全局供应器配置组装 {@link ManagedCertificates}，
 * 并在 Keycloak 服务器启动前完成证书准备。
 */
public class CertificatesSupplier implements Supplier<ManagedCertificates, InjectCertificates> {

    /** {@inheritDoc} 合并注解配置与全局覆盖后创建 {@link ManagedCertificates}。 */
    @Override
    public ManagedCertificates getValue(InstanceContext<ManagedCertificates, InjectCertificates> instanceContext) {
        CertificatesConfig certConfig = SupplierHelpers.getInstance(instanceContext.getAnnotation().config());
        CertificatesConfigBuilder certBuilder = new CertificatesConfigBuilder();
        certBuilder = certConfig.configure(certBuilder);

        String supplierConfig = Config.getSupplierConfig(ManagedCertificates.class);
        if (supplierConfig != null) {
            CertificatesConfig certConfigOverride = SupplierHelpers.getInstance(supplierConfig);
            certConfigOverride.configure(certBuilder);
        }
        return new ManagedCertificates(certBuilder);
    }

    /** {@inheritDoc} 默认生命周期为 {@link LifeCycle#GLOBAL}。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    /** {@inheritDoc} 仅当 {@code config} 注解值相同时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<ManagedCertificates, InjectCertificates> a, RequestedInstance<ManagedCertificates, InjectCertificates> b) {
        return a.getAnnotation().config().equals(b.getAnnotation().config());
    }

    /** {@inheritDoc} 在 Keycloak 服务器启动前执行。 */
    @Override
    public int order() {
        return SupplierOrder.BEFORE_KEYCLOAK_SERVER;
    }
}
