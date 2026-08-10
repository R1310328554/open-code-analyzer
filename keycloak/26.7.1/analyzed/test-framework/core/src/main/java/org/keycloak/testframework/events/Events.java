package org.keycloak.testframework.events;

import java.util.List;

import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.testframework.realm.ManagedRealm;

import org.jboss.logging.Logger;

/**
 * 从 Keycloak 服务器轮询用户事件（User Events）。
 * <p>
 * 通过 {@link ManagedRealm#admin()} API 按时间范围增量拉取 {@link EventRepresentation}。
 */
public class Events extends AbstractEvents<EventRepresentation> {

    /** 本类日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(Events.class);

    /** @param realm 托管 realm，用于访问 Events API */
    public Events(ManagedRealm realm) {
        super(realm);
    }

    /** {@inheritDoc} 调用 Admin REST API 按时间升序拉取用户事件。 */
    @Override
    protected List<EventRepresentation> getEvents(long from, long to) {
        return realm.admin().getEvents(null, null, null, from, to, null, null, null, "asc");
    }

    /** {@inheritDoc} 返回 {@link EventRepresentation#getId()}。 */
    @Override
    protected String getEventId(EventRepresentation rep) {
        return rep.getId();
    }

    /** {@inheritDoc} 返回 {@link EventRepresentation#getRealmId()}。 */
    @Override
    protected String getRealmId(EventRepresentation rep) {
        return rep.getRealmId();
    }

    /** {@inheritDoc} 返回 {@link EventRepresentation#getTime()}。 */
    @Override
    protected long getTime(EventRepresentation rep) {
        return rep.getTime();
    }

    /** {@inheritDoc} 调用 API 清除 realm 上的全部用户事件。 */
    @Override
    protected void clearServerEvents() {
        realm.admin().clearEvents();
    }

    /** {@inheritDoc} 返回 {@link #LOGGER}。 */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
