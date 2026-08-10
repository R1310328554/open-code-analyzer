/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authentication.jpa;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.SessionExpiration;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import org.jboss.logging.Logger;

/** 基于 JPA 的认证会话 Provider，在 STATELESS 特性下替代 Infinispan 实现。 */
public class JpaAuthenticationSessionProvider extends AbstractKeycloakTransaction implements AuthenticationSessionProvider {

    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private final KeycloakSession session;
    private final int authSessionsLimit;
    private final Map<String, RootAuthenticationSessionAdapter> transientSessions = new HashMap<>();
    private boolean enlisted;

    public JpaAuthenticationSessionProvider(KeycloakSession session, int authSessionsLimit) {
        this.session = Objects.requireNonNull(session);
        this.authSessionsLimit = authSessionsLimit;
    }

    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm) {
        var model = RootAuthenticationSessionAdapter.create(session, realm, SecretGenerator.SECURE_ID_GENERATOR.get(), Time.currentTimeSeconds(), authSessionsLimit);
        // 随机 ID 的新会话尚未入库，不存在冲突；ROPC 等临时会话仅在本次请求内未删除时才 persist
        transientSessions.put(model.getEntity().getId(), model);
        prepareTransaction();
        return model;
    }

    private void prepareTransaction() {
        if (!enlisted) {
            enlisted = true;
            session.getTransactionManager().enlistPrepare(this);
        }
    }

    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm, String id) {
        if (id == null) {
            return createRootAuthenticationSession(realm);
        }
        var em = getEntityManager();
        em.createNamedQuery("insertRootAuthSessionIfAbsent")
                .setParameter("id", id)
                .setParameter("realmId", realm.getId())
                .setParameter("timestamp", Time.currentTimeSeconds())
                .executeUpdate();
        var entity = em.find(RootAuthenticationSessionEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity == null) {
            throw new ModelException("Unable to create or find root authentication session with id '" + id + "'");
        }
        if (!Objects.equals(realm.getId(), entity.getRealmId())) {
            throw new ModelException("Another root authentication session with id '" + id + "' already exists in other realm");
        }
        var lifespan = SessionExpiration.getAuthSessionLifespan(realm);
        if (entity.getTimestamp() + lifespan < Time.currentTimeSeconds()) {
            logger.debugf("Root authentication session with id '%s' is expired.", id);
            // 过期则重置时间戳并清空子会话，相当于重启
            entity.setTimestamp(Time.currentTimeSeconds());
            entity.getAuthenticationSessions().clear();
        }
        return RootAuthenticationSessionAdapter.wrapEntity(session, realm,  entity, authSessionsLimit);
    }

    @Override
    public RootAuthenticationSessionModel getRootAuthenticationSession(RealmModel realm, String id) {
        if (id == null) {
            return null;
        }
        var model = transientSessions.get(id);
        if (model != null && Objects.equals(model.getRealm().getId(), realm.getId())) {
            // 二次校验：防止跨域错误引用
            return model;
        }

        var em = getEntityManager();
        var entity = em.find(RootAuthenticationSessionEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity == null || !Objects.equals(realm.getId(), entity.getRealmId())) {
            return null;
        }
        var lifespan = SessionExpiration.getAuthSessionLifespan(realm);
        if (entity.getTimestamp() + lifespan < Time.currentTimeSeconds()) {
            logger.debugf("Root authentication session with id '%s' is expired.", id);
            em.remove(entity);
            return null;
        }
        return RootAuthenticationSessionAdapter.wrapEntity(session, realm, entity, authSessionsLimit);
    }

    @Override
    public void removeRootAuthenticationSession(RealmModel realm, RootAuthenticationSessionModel authenticationSession) {
        if (!Objects.equals(realm.getId(), authenticationSession.getRealm().getId())) {
            throw new ModelException("Authentication session with id '" + authenticationSession.getId() + "' does not belong to realm '" + realm.getId() + "'");
        }
        if (transientSessions.remove(authenticationSession.getId()) != null) {
            return;
        }
        var em = getEntityManager();
        if (authenticationSession instanceof RootAuthenticationSessionAdapter adapter) {
            em.remove(adapter.getEntity());
            return;
        }
        var entity = em.find(RootAuthenticationSessionEntity.class, authenticationSession.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (entity != null) {
            em.remove(entity);
        }
    }

    @Override
    public void onRealmRemoved(RealmModel realm) {
        getEntityManager()
                .createNamedQuery("deleteRootAuthSessionByRealm")
                .setParameter("realmId", realm.getId())
                .executeUpdate();
    }

    @Override
    public void close() {

    }

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    /** 提交时将暂存的新根会话批量 persist 到数据库。 */
    @Override
    protected void commitImpl() {
        transientSessions.forEach((key, value) -> getEntityManager().persist(value.getEntity()));
    }

    @Override
    protected void rollbackImpl() {
    }
}
