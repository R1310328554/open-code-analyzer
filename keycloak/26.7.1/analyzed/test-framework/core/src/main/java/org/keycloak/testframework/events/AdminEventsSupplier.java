package org.keycloak.testframework.events;

import org.keycloak.testframework.annotations.InjectAdminEvents;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.realm.RealmBuilder;

/**
 * 为 {@link InjectAdminEvents} 注入 {@link AdminEvents} 收集器的供应器。
 * <p>
 * 自动启用 realm 的管理事件及详情记录。
 */
public class AdminEventsSupplier extends AbstractEventsSupplier<AdminEvents, InjectAdminEvents> {

    /** {@inheritDoc} */
    @Override
    public AdminEvents getValue(InstanceContext<AdminEvents, InjectAdminEvents> instanceContext) {
        return super.getValue(instanceContext);
    }

    /** {@inheritDoc} 创建绑定到 realm 的 {@link AdminEvents} 实例。 */
    @Override
    public AdminEvents createValue(ManagedRealm realm) {
        return new AdminEvents(realm);
    }

    /** {@inheritDoc} 启用 admin events 与 admin event details。 */
    @Override
    public RealmBuilder intercept(RealmBuilder realm, InstanceContext<AdminEvents, InjectAdminEvents> instanceContext) {
        return realm.adminEventsEnabled(true).adminEventsDetailsEnabled(true);
    }

}
