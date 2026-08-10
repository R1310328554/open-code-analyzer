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

package org.keycloak.loginfailures.jpa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserLoginFailureModel;
import org.keycloak.models.UserLoginFailureProvider;

/**
 * 基于 JPA 的 {@link UserLoginFailureProvider} 实现，管理 LOGIN_FAILURE 表的读写。
 * <p>
 * 会话级两级缓存：
 * <ul>
 *   <li>{@code notInDatabaseCache}：负缓存，避免对不存在的 (realm, user) 重复 SELECT。</li>
 *   <li>{@code entityInSession}：同一 {@link LoginFailureKey} 仅包装一个 {@link UserLoginFailureAdapter}，
 *       防止 refresh 时丢失未刷新的修改。</li>
 * </ul>
 */
public class JpaUserLoginFailureProvider implements UserLoginFailureProvider {

    private final KeycloakSession session;
    /** 已确认数据库中不存在的键，避免 JPA 重复 miss 查询。 */
    private final Set<LoginFailureKey> notInDatabaseCache = new HashSet<>();
    /** 当前会话内已 materialize 的适配器，保证每实体单 adapter 实例。 */
    private final Map<LoginFailureKey, UserLoginFailureModel> entityInSession = new HashMap();

    public JpaUserLoginFailureProvider(KeycloakSession session) {
        this.session = session;
    }

    /** 按 (realmId, userId) 查找；不存在时写入负缓存并返回 null。 */
    @Override
    public UserLoginFailureModel getUserLoginFailure(RealmModel realm, String userId) {
        var key = new LoginFailureKey(realm.getId(), userId);
        if (notInDatabaseCache.contains(key)) {
            // JPA 会缓存 persistence context 中已有行，但 miss 时每次 find 仍会打库；
            // 会话级负缓存确保「不存在」只查一次。
            return null;
        }
        UserLoginFailureModel model = entityInSession.get(key);
        if (model != null) {
            // Adapter 在写操作前会 refresh 实体；同 key 复用同一 adapter 避免并发修改被丢弃。
            return model;
        }
        var em = getEntityManager();
        var entity = em.find(LoginFailureEntity.class, key);
        if (entity == null) {
            notInDatabaseCache.add(key);
            return null;
        }
        model = new UserLoginFailureAdapter(em, entity);
        entityInSession.put(key, model);
        return model;
    }

    /**
     * 幂等插入：命名查询使用 ON CONFLICT DO NOTHING。
     * 插入后清除负缓存并加载（或复用已存在行）实体。
     */
    @Override
    public UserLoginFailureModel addUserLoginFailure(RealmModel realm, String userId) {
        var em = getEntityManager();
        em.createNamedQuery("insertLoginFailure")
                .setParameter("realmId", realm.getId())
                .setParameter("userId", userId)
                .executeUpdate();
        var key = new LoginFailureKey(realm.getId(), userId);
        notInDatabaseCache.remove(key);
        var entity = em.find(LoginFailureEntity.class, key);
        UserLoginFailureModel model = new UserLoginFailureAdapter(em, entity);
        entityInSession.put(key, model);
        return model;
    }

    /** 悲观写锁删除单行；实体不存在时静默返回。 */
    @Override
    public void removeUserLoginFailure(RealmModel realm, String userId) {
        var key = new LoginFailureKey(realm.getId(), userId);
        var em = getEntityManager();
        var entity = em.find(LoginFailureEntity.class, key, LockModeType.PESSIMISTIC_WRITE);
        if (entity == null) {
            return;
        }
        em.remove(entity);
        entityInSession.remove(key);
        // 依赖 JPA 脏检查刷新，无需显式 flush
    }

    /** 批量删除 realm 下全部登录失败记录（realm 删除等场景）。 */
    @Override
    public void removeAllUserLoginFailures(RealmModel realm) {
        var em = getEntityManager();
        em.createNamedQuery("deleteLoginFailureByRealm")
                    .setParameter("realmId", realm.getId())
                    .executeUpdate();
    }

    @Override
    public void close() {
    }

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }
}
