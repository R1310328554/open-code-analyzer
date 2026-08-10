/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.services.managers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.ClientConnection;
import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 阻塞式暴力破解保护器。
 * <p>在 {@link DefaultBruteForceProtector} 基础上，对同一用户并发登录尝试进行线程级串行化，避免并行请求绕过临时锁定判定。</p>
 */
public class DefaultBlockingBruteForceProtector extends DefaultBruteForceProtector {

    // TODO：并发上限是否可配置
    /** 并发登录跟踪 map 最大条目数 */
    private static final int DEFAULT_MAX_CONCURRENT_ATTEMPTS = 1000;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /** 标记暴力破解处理已在后台线程启动的后缀 */
    private static final String OFF_THREAD_STARTED = "#brute_force_started";

    /** 用户 ID → 正在处理登录的线程名（LRU 淘汰） */
    private final Map<String, String> loginAttempts = Collections.synchronizedMap(new LinkedHashMap<>(100, DEFAULT_LOAD_FACTOR) {
        @Override
        protected boolean removeEldestEntry(Entry<String, String> eldest) {
            return loginAttempts.size() > DEFAULT_MAX_CONCURRENT_ATTEMPTS;
        }
    });

    /** @param factory Keycloak 会话工厂 */
    DefaultBlockingBruteForceProtector(KeycloakSessionFactory factory) {
        super(factory);
    }

    @Override
    public boolean isPermanentlyLockedOut(KeycloakSession session, RealmModel realm, UserModel user) {
        if (super.isPermanentlyLockedOut(session, realm, user)) {
            return true;
        }

        if (!realm.isPermanentLockout()) return false;

        return isLoginInProgress(session, user);
    }

    @Override
    public boolean isTemporarilyDisabled(KeycloakSession session, RealmModel realm, UserModel user) {
        if (super.isTemporarilyDisabled(session, realm, user)) {
            return true;
        }

        return isLoginInProgress(session, user);
    }

    private boolean isLoginInProgress(KeycloakSession session, UserModel user) {
        AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();

        if (authSession == null) {
            // not authenticating as there is no auth session bound to the session
            return false;
        }

        return !tryEnlistBlockingTransactionOrSameThread(session, user);
    }

    // 当前线程成功登记或已由同一线程登记时返回 true
    private boolean tryEnlistBlockingTransactionOrSameThread(KeycloakSession session, UserModel user) {
        AtomicBoolean inserted = new AtomicBoolean(false);
        String threadInProgress = loginAttempts.computeIfAbsent(user.getId(), k -> {
            inserted.set(true);
            return getThreadName();
        });

        // This means that this thread successfully added itself into the map. We can enlist transaction just in that case
        if (inserted.get()) {
            session.getTransactionManager().enlistAfterCompletion(new AbstractKeycloakTransaction() {
                @Override
                protected void commitImpl() {
                    // remove or wait the brute force thread to finish
                    loginAttempts.computeIfPresent(user.getId(), (k, v) -> v.endsWith(OFF_THREAD_STARTED)? "" : null);
                }

                @Override
                protected void rollbackImpl() {
                    // remove on rollback
                    loginAttempts.remove(user.getId());
                }
            });

            return true;
        } else {
            return isCurrentThread(threadInProgress);
        }
    }

    private boolean isCurrentThread(String name) {
        return name.equals(getThreadName()) || name.equals(getThreadName() + OFF_THREAD_STARTED);
    }

    private String getThreadName() {
        return Thread.currentThread().getName();
    }

    private void enlistRemoval(KeycloakSession session, String userId) {
        session.getTransactionManager().enlistAfterCompletion(new AbstractKeycloakTransaction() {
            @Override
            protected void commitImpl() {
                // remove or wait the main thread to finish
                loginAttempts.computeIfPresent(userId, (k, v) -> v.isEmpty()? null : v.substring(0, v.length() - OFF_THREAD_STARTED.length()));
            }

            @Override
            protected void rollbackImpl() {
                loginAttempts.remove(userId);
            }
        });
    }

    @Override
    protected void processLogin(RealmModel realm, UserModel user, ClientConnection clientConnection, UriInfo uriInfo, boolean success, Set<String> categories) {
        // mark the off-thread is started for this request
        loginAttempts.computeIfPresent(user.getId(), (k, v) -> v + OFF_THREAD_STARTED);
        super.processLogin(realm, user, clientConnection, uriInfo, success, categories);
    }

    @Override
    public void failure(KeycloakSession session, RealmModel realm, String userId, String remoteAddr, long failureTime, Set<String> categories) {
        // remove the user from concurrent login attemps once it's processed
        enlistRemoval(session, userId);
        super.failure(session, realm, userId, remoteAddr, failureTime, categories);
    }

    @Override
    protected void success(KeycloakSession session, RealmModel realm, String userId, Set<String> categories) {
        // remove the user from concurrent login attempts once it's processed
        enlistRemoval(session, userId);
        super.success(session, realm, userId, categories);
    }
}
