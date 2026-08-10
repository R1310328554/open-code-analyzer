package org.keycloak.infinispan.module.factory;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

import io.opentelemetry.api.OpenTelemetry;
import org.infinispan.factories.AbstractComponentFactory;
import org.infinispan.factories.AutoInstantiableFactory;
import org.infinispan.factories.annotations.DefaultFactoryFor;
import org.infinispan.factories.scopes.Scope;
import org.infinispan.factories.scopes.Scopes;
import org.infinispan.telemetry.InfinispanTelemetry;
import org.infinispan.telemetry.impl.DisabledInfinispanTelemetry;

/**
 * {@link InfinispanTelemetry} 的 Infinispan 组件工厂。
 * <p>
 * 通过 Quarkus CDI 解析 {@link OpenTelemetry} Bean；无 CDI 上下文或无可解析 Bean 时回退为禁用遥测实现。
 */
@Scope(Scopes.GLOBAL)
@DefaultFactoryFor(classes = InfinispanTelemetry.class)
public class InfinispanTelemetryFactory extends AbstractComponentFactory implements AutoInstantiableFactory {

    /** {@inheritDoc} 优先使用 Keycloak/Quarkus 提供的 OpenTelemetry，否则禁用追踪。 */
    @Override
    public Object construct(String componentName) {
        CDI<Object> current;
        try {
            current = CDI.current();
        } catch (IllegalStateException e) {
            // 无 CDI 上下文，假定追踪不可用
            return new DisabledInfinispanTelemetry();
        }
        Instance<OpenTelemetry> selector = current.select(OpenTelemetry.class);
        if (!selector.isResolvable()) {
            return new DisabledInfinispanTelemetry();
        } else {
            return new OpenTelemetryService(selector.get());
        }
    }
}
