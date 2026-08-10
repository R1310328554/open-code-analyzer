package org.keycloak.scripting;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link ScriptingProvider} 的 SPI 工厂接口。
 * <p>注册并创建脚本引擎提供者实例。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public interface ScriptingProviderFactory extends ProviderFactory<ScriptingProvider> {
}
