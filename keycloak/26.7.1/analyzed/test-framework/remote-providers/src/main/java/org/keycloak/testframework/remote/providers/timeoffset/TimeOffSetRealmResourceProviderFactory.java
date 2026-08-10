package org.keycloak.testframework.remote.providers.timeoffset;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * {@link TimeOffSetRealmResourceProvider} 的 SPI 工厂。
 */
public class TimeOffSetRealmResourceProviderFactory implements RealmResourceProviderFactory {

    /** Realm 资源提供者标识符。 */
    private final String ID = "testing-timeoffset";

    /** {@inheritDoc} 创建 {@link TimeOffSetRealmResourceProvider} 实例。 */
    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new TimeOffSetRealmResourceProvider(session);
    }

    /** {@inheritDoc} 无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 无持久资源需关闭。 */
    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回 {@link #ID}。 */
    @Override
    public String getId() {
        return ID;
    }

    /** {@inheritDoc} 无配置项需读取。 */
    @Override
    public void init(org.keycloak.Config.Scope config) {

    }
}
