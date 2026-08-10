package org.keycloak.models.sessions.infinispan.util;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.RemoteAuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.RemoteUserSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.UserSessionEntity;
import org.keycloak.models.utils.SessionExpirationUtils;

/**
 * 判断用户会话或客户端会话是否已过期的工具 record。
 * <p>
 * 统一处理 JPA 模型、本地缓存实体与远程缓存实体等多种来源。
 *
 * @param realm       用于读取 max-idle 与 lifespan 配置的 {@link RealmModel}
 * @param offline     是否为离线会话
 * @param currentTime 当前时间戳（秒）
 */
public record SessionExpirationPredicates(RealmModel realm, boolean offline, int currentTime) {

    /** 根据 {@link UserSessionModel} 判断是否过期。 */
    public boolean isUserSessionExpired(UserSessionModel model) {
        return isUserSessionExpired(model.isRememberMe(), model.getStarted(), model.getLastSessionRefresh());
    }

    /** 根据本地 {@link UserSessionEntity} 判断是否过期。 */
    public boolean isUserSessionExpired(UserSessionEntity entity) {
        return isUserSessionExpired(entity.isRememberMe(), entity.getStarted(), entity.getLastSessionRefresh());
    }

    /** 根据远程 {@link RemoteUserSessionEntity} 判断是否过期。 */
    public boolean isUserSessionExpired(RemoteUserSessionEntity entity) {
        return isUserSessionExpired(entity.isRememberMe(), entity.getStarted(), entity.getLastSessionRefresh());
    }

    /** 根据 {@link AuthenticatedClientSessionModel} 判断是否过期。 */
    public boolean isClientSessionExpired(AuthenticatedClientSessionModel model) {
        return isClientSessionExpired(model.getUserSession().isRememberMe(), model.getStarted(), model.getUserSessionStarted(), model.getTimestamp(), model.getClient());
    }

    /** 根据本地 {@link AuthenticatedClientSessionEntity} 判断是否过期。 */
    public boolean isClientSessionExpired(AuthenticatedClientSessionEntity entity, boolean rememberMe, ClientModel client) {
        return isClientSessionExpired(rememberMe, entity.getStarted(), entity.getUserSessionStarted(), entity.getTimestamp(), client);
    }

    /** 根据远程 {@link RemoteAuthenticatedClientSessionEntity} 判断是否过期。 */
    public boolean isClientSessionExpired(RemoteAuthenticatedClientSessionEntity entity, int userSessionStarted, boolean rememberMe, ClientModel client) {
        return isClientSessionExpired(rememberMe, entity.getStarted(), userSessionStarted, entity.getTimestamp(), client);
    }

    /** 用户会话过期判定：同时比较最大寿命与空闲超时。 */
    private boolean isUserSessionExpired(boolean rememberMe, long started, long lastRefresh) {
        var lifespan = SessionExpirationUtils.calculateUserSessionMaxLifespanTimestamp(offline, rememberMe, started, realm);
        var maxIdle = SessionExpirationUtils.calculateUserSessionIdleTimestamp(offline, rememberMe, lastRefresh, realm);
        return isExpired(lifespan, maxIdle);
    }

    /** 客户端会话过期判定：同时比较最大寿命与空闲超时。 */
    private boolean isClientSessionExpired(boolean rememberMe, long started, long userSessionStarted, long lastRefresh, ClientModel client) {
        var lifespan = SessionExpirationUtils.calculateClientSessionMaxLifespanTimestamp(offline, rememberMe, started, userSessionStarted, realm, client);
        var maxIdle = SessionExpirationUtils.calculateClientSessionIdleTimestamp(offline, rememberMe, lastRefresh, realm, client);
        return isExpired(lifespan, maxIdle);
    }

    /**
     * 综合 lifespan 与 maxIdle 判断是否过期。
     * lifespan 为 -1 时仅依据 maxIdle；否则任一条件满足即视为过期。
     */
    private boolean isExpired(long lifespanTimestamp, long maxIdleTimestamp) {
        var maxIdleExpired = maxIdleTimestamp - currentTime <= 0;
        return lifespanTimestamp == -1 ?
                maxIdleExpired :
                maxIdleExpired || lifespanTimestamp - currentTime <= 0;
    }
}
