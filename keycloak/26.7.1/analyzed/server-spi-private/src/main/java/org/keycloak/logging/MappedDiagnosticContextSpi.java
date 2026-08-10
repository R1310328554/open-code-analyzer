package org.keycloak.logging;

import org.keycloak.provider.Spi;

/**
 * 映射诊断上下文 SPI，注册 {@link MappedDiagnosticContextProvider} 提供者类型。
 * <p>为每个请求定义应写入 MDC 的键值对，便于结构化日志关联。</p>
 *
 * @author <a href="mailto:b.eicki@gmx.net">Björn Eickvonder</a>
 */
public class MappedDiagnosticContextSpi implements Spi {

    /** 非内部 SPI，扩展模块可替换 MDC 行为。 */
    @Override
    public boolean isInternal() {
        return false;
    }

    /** SPI 名称：{@code mappedDiagnosticContext}。 */
    @Override
    public String getName() {
        return "mappedDiagnosticContext";
    }

    /** MDC 提供者接口类型。 */
    @Override
    public Class<MappedDiagnosticContextProvider> getProviderClass() {
        return MappedDiagnosticContextProvider.class;
    }

    /** MDC 工厂类型。 */
    @Override
    public Class<MappedDiagnosticContextProviderFactory> getProviderFactoryClass() {
        return MappedDiagnosticContextProviderFactory.class;
    }
}
