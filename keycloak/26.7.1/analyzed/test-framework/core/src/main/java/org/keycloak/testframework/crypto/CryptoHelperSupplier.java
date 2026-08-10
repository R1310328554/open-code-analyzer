package org.keycloak.testframework.crypto;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.crypto.FipsMode;
import org.keycloak.crypto.def.DefaultCryptoProvider;
import org.keycloak.testframework.annotations.InjectCryptoHelper;
import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;

/**
 * 注入 {@link CryptoHelper}：初始化 {@link org.keycloak.common.crypto.CryptoIntegration} 并按配置选择 FIPS 模式。
 */
public class CryptoHelperSupplier implements Supplier<CryptoHelper, InjectCryptoHelper> {

    /** 确保 Crypto 提供者已初始化，并从 {@link Config} 读取 {@code fips} 配置。 */
    @Override
    public CryptoHelper getValue(InstanceContext<CryptoHelper, InjectCryptoHelper> instanceContext) {
        if (!CryptoIntegration.isInitialised()) {
            CryptoIntegration.setProvider(new DefaultCryptoProvider());
        }
        FipsMode fips = Config.getValueTypeConfig(CryptoHelper.class, "fips", FipsMode.DISABLED.name(), FipsMode.class);
        return new CryptoHelper(fips);
    }

    /** CryptoHelper 默认 GLOBAL 生命周期。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    /** 单例 CryptoHelper 始终兼容复用。 */
    @Override
    public boolean compatible(InstanceContext<CryptoHelper, InjectCryptoHelper> a, RequestedInstance<CryptoHelper, InjectCryptoHelper> b) {
        return true;
    }

}
