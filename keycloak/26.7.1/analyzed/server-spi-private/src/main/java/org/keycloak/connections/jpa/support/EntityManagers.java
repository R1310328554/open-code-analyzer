/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.jpa.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;

import org.keycloak.models.KeycloakSession;

import org.hibernate.Session;

/**
 * JPA {@link EntityManager} 批量模式与会话级 flush 工具。
 * <p>通过 {@link ThreadLocal} 标记批量上下文，并协调多个 {@link EntityManagerProxy}。</p>
 */
public class EntityManagers {

    /** 会话属性键：当前活跃的 {@link EntityManagerProxy} 集合。 */
    static final String ENTITY_MANAGER_PROXIES = "ENTITY_MANAGER_PROXIES";

    private static final ThreadLocal<Boolean> batchMode = new ThreadLocal<Boolean>();

    static void runInBatchMode(Runnable runnable) {
        boolean isBatched = isBatchMode();
        batchMode.set(true);
        try {
            runnable.run();
        } finally {
            if (!isBatched) {
                batchMode.remove();
            }
        }
    }

    /** 当前线程是否处于批量模式。 */
    public static boolean isBatchMode() {
        return Boolean.TRUE.equals(batchMode.get());
    }

    static void forEachEntityManager(KeycloakSession session, Consumer<EntityManager> op) {
        try {
            getEntityManagerProxies(session).map(EntityManagerProxy::getEntityManager)
                    .filter(EntityManager::isOpen).forEach(op);
        } catch (Exception e) {
            // this was run directly on the unproxied entitymanagers, so the exception will need converted
            throw EntityManagerProxy.convert(e);
        }
    }

    static Stream<EntityManagerProxy> getEntityManagerProxies(KeycloakSession session) {
        return Optional.ofNullable((Set<EntityManagerProxy>) session.getAttribute(ENTITY_MANAGER_PROXIES, Set.class))
                .map(Set::stream).orElse(Stream.of());
    }

    /** 对所有在用 {@link EntityManager} 执行 flush，可选 clear。 */
    public static void flush(KeycloakSession session, boolean clear) {
        forEachEntityManager(session, em -> {
            em.flush(); // TODO: avoid if read-only
            if (clear) {
                em.clear();
            }
        });
    }

    /**
     * 在批量模式下运行操作，执行前先 flush。
     * <p>建议 {@code nestedEntityManagers=true} 以隔离持久化上下文。批量模式下查询为 COMMIT flush 模式，无法看到批内未 flush 的变更。</p>
     * <p>警告：传入任务的待持久化实体不得已关联打开的 EntityManager。</p>
     *
     * @param nestedEntityManagers 为 true 时使用隔离 EntityManager
     */
    public static void runInBatch(KeycloakSession session, Runnable runnable, boolean nestedEntityManagers) {
        Map<EntityManagerProxy, Session> previous = new HashMap<EntityManagerProxy, Session>();

        flush(session, false); // 进入批处理前确保当前状态已提交

        // 创建共享事务协调器的局部 EntityManager，避免批处理后残留状态
        if (nestedEntityManagers) {
            getEntityManagerProxies(session).forEach(p -> {
                if (!p.getEntityManager().isOpen()) {
                    return;
                }
                Session em = p.getEntityManager().unwrap(Session.class);
                Session derived = em.sessionWithOptions().connection().openSession();
                previous.put(p, em);
                p.setEntityManager(derived);
            });
        }

        try {
            runInBatchMode(runnable);
            if (nestedEntityManagers) {
                flush(session, true);
            }
        } finally {
            // 恢复原有 EntityManager
            if (nestedEntityManagers) {
                getEntityManagerProxies(session).forEach(p -> {
                    EntityManager current = p.getEntityManager();
                    EntityManager old = previous.get(p);
                    if (old != null) {
                        if (current.isOpen()) {
                            current.close();
                        }
                        p.setEntityManager(old);
                    } // 批处理期间新建，已 flush/clear 即可
                });
            }
        }
    }

}
