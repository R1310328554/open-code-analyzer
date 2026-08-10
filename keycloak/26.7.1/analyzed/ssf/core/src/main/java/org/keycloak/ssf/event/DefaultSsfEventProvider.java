package org.keycloak.ssf.event;

/**
 * {@link SsfEventProvider} 的默认实现，直接暴露预构建的 {@link SsfEventRegistry}。
 */
public class DefaultSsfEventProvider implements SsfEventProvider {

    private final SsfEventRegistry registry;

    public DefaultSsfEventProvider(SsfEventRegistry registry) {
        this.registry = registry;
    }

    @Override
    public SsfEventRegistry getRegistry() {
        return registry;
    }
}
