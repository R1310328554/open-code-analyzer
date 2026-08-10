package org.keycloak.logging;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link MappedDiagnosticContextProvider} 的 {@link ProviderFactory} 工厂接口。
 * <p>默认实现将领域、客户端、用户会话等上下文写入 JBoss Logging MDC。</p>
 *
 * @author <a href="mailto:b.eicki@gmx.net">Björn Eickvonder</a>
 */
public interface MappedDiagnosticContextProviderFactory extends ProviderFactory<MappedDiagnosticContextProvider> {
}
