package org.keycloak.scripting;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 脚本 SPI：注册 {@link ScriptingProvider} 及工厂。
 * <p>内部 SPI，名称 {@code scripting}。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class ScriptingSpi implements Spi {

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code scripting} */
    @Override
    public String getName() {
        return "scripting";
    }

    /** @return 提供者接口 {@link ScriptingProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ScriptingProvider.class;
    }

    /** @return 工厂接口 {@link ScriptingProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ScriptingProviderFactory.class;
    }
}
