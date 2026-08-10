package org.keycloak.testframework.events;

import org.keycloak.testframework.annotations.InjectEvents;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.realm.RealmBuilder;

/**
 * 为 {@link InjectEvents} 注入 {@link Events} 收集器的供应器。
 * <p>
 * 自动启用 realm 的用户事件记录。
 */
public class EventsSupplier extends AbstractEventsSupplier<Events, InjectEvents> {

    /** {@inheritDoc} */
    @Override
    public Events getValue(InstanceContext<Events, InjectEvents> instanceContext) {
        return super.getValue(instanceContext);
    }

    /** {@inheritDoc} 创建绑定到 realm 的 {@link Events} 实例。 */
    @Override
    protected Events createValue(ManagedRealm realm) {
        return new Events(realm);
    }

    /** {@inheritDoc} 启用 user events。 */
    @Override
    public RealmBuilder intercept(RealmBuilder realm, InstanceContext<Events, InjectEvents> instanceContext) {
        return realm.eventsEnabled(true);
    }

}
