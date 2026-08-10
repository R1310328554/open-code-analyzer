package org.keycloak.models.workflow;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 工作流事件 SPI：注册 {@link WorkflowEventProvider} 及其工厂。
 * <p>内部 SPI，名称 {@value #NAME}。</p>
 */
public class WorkflowEventSpi implements Spi {

    /** SPI 名称：{@code workflow-event}。 */
    public static final String NAME = "workflow-event";

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@link #NAME} */
    @Override
    public String getName() {
        return NAME;
    }


    /** @return 提供者接口 {@link WorkflowEventProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return WorkflowEventProvider.class;
    }

    /** @return 工厂接口 {@link WorkflowEventProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return WorkflowEventProviderFactory.class;
    }
}
