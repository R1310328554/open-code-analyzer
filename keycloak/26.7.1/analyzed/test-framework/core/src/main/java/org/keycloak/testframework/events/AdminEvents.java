package org.keycloak.testframework.events;

import java.util.List;

import org.keycloak.representations.idm.AdminEventRepresentation;
import org.keycloak.testframework.realm.ManagedRealm;

import org.jboss.logging.Logger;

/**
 * 从 Keycloak 服务器轮询管理事件（Admin Events）。
 * <p>
 * 通过 {@link ManagedRealm#admin()} API 按时间范围增量拉取 {@link AdminEventRepresentation}。
 */
public class AdminEvents extends AbstractEvents<AdminEventRepresentation> {

    /** 本类日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(AdminEvents.class);

    /** @param realm 托管 realm，用于访问 Admin Events API */
    public AdminEvents(ManagedRealm realm) {
        super(realm);
    }

    /** {@inheritDoc} 调用 Admin REST API 按时间升序拉取事件。 */
    @Override
    protected List<AdminEventRepresentation> getEvents(long from, long to) {
        return realm.admin().getAdminEvents(null, null, null, null, null, null, null, from, to, null, null, "asc");
    }

    /** {@inheritDoc} 返回 {@link AdminEventRepresentation#getId()}。 */
    @Override
    protected String getEventId(AdminEventRepresentation rep) {
        return rep.getId();
    }

    /** {@inheritDoc} 返回 {@link AdminEventRepresentation#getRealmId()}。 */
    @Override
    protected String getRealmId(AdminEventRepresentation rep) {
        return rep.getRealmId();
    }

    /** {@inheritDoc} 返回 {@link AdminEventRepresentation#getTime()}。 */
    @Override
    protected long getTime(AdminEventRepresentation rep) {
        return rep.getTime();
    }

    /** {@inheritDoc} 调用 API 清除 realm 上的全部管理事件。 */
    @Override
    protected void clearServerEvents() {
        realm.admin().clearAdminEvents();
    }

    /** {@inheritDoc} 返回 {@link #LOGGER}。 */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
