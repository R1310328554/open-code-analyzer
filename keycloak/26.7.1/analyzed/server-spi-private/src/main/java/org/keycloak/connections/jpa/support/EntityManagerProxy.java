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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Query;

import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelException;
import org.keycloak.models.ModelIllegalStateException;

import org.hibernate.exception.ConstraintViolationException;

/**
 * {@link EntityManager} 动态代理，统一异常转换与可选批量 flush 优化。
 * <p>将 JPA/Hibernate 异常映射为 {@link ModelException} 子类，并在批量模式下按阈值 flush/clear。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class EntityManagerProxy {

    private static final Pattern WRITE_METHOD_NAMES = Pattern.compile("persist|merge");

    private Set<EntityManagerProxy> entityManagerProxies;
    private EntityManager em;
    private final boolean batchEnabled;
    private final int batchSize;
    private int changeCount = 0;

    /**
     * 为会话创建带代理的 {@link EntityManager}。
     * @param session Keycloak 会话
     * @param em 底层实体管理器
     * @param sessionManaged 是否由会话跟踪代理实例
     */
    public static EntityManager create(KeycloakSession session, EntityManager em, boolean sessionManaged) {
        Set<EntityManagerProxy> entityManagerProxies = null;
        if (sessionManaged) {
            // 替代方案是在 session 上提供获取在用 provider 的方法，但会触发全部 provider 创建
            entityManagerProxies = session.getAttribute(EntityManagers.ENTITY_MANAGER_PROXIES, Set.class);
            if (entityManagerProxies == null) {
                entityManagerProxies = new HashSet<>();
                session.setAttribute(EntityManagers.ENTITY_MANAGER_PROXIES, entityManagerProxies);
            }
        }
        boolean batchEnabled = session.getAttributeOrDefault(Constants.STORAGE_BATCH_ENABLED, false);
        int batchSize = session.getAttributeOrDefault(Constants.STORAGE_BATCH_SIZE, 100);
        return create(em, entityManagerProxies, batchEnabled, batchSize);
    }

    static EntityManager create(EntityManager em, Set<EntityManagerProxy> entityManagerProxies,
            boolean batchEnabled, int batchSize) {
        EntityManagerProxy converter = new EntityManagerProxy(em, entityManagerProxies, batchEnabled, batchSize);
        if (entityManagerProxies != null) {
            entityManagerProxies.add(converter);
        }
        return (EntityManager) Proxy.newProxyInstance(EntityManager.class.getClassLoader(), new Class[]{EntityManager.class}, converter::invoke);
    }

    private EntityManagerProxy(EntityManager em, Set<EntityManagerProxy> entityManagerProxies, boolean batchEnabled, int batchSize) {
        this.batchEnabled = batchEnabled;
        this.batchSize = batchSize;
        this.em = em;
        this.entityManagerProxies = entityManagerProxies;
    }

    void setEntityManager(EntityManager manager) {
        this.em = manager;
    }

    EntityManager getEntityManager() {
        return this.em;
    }

    private Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean batched = EntityManagers.isBatchMode();
        try {
            flushInBatchIfEnabled(method);
            Object result = method.invoke(em, args);
            if (batched && result instanceof Query query) {
                // TODO: it would be safer if there were a way to validate
                // if this or disabling persist/detach where correct for a given batch
                // and types were correct
                query.setFlushMode(FlushModeType.COMMIT);
            }
            if (entityManagerProxies != null && args == null && method.getName().equals("close")) {
                entityManagerProxies.remove(this);
            }
            return result;
        } catch (InvocationTargetException e) {
            throw convert(e);
        }
    }

    private void flushInBatchIfEnabled(Method method) {
        if (batchEnabled) {
            if (WRITE_METHOD_NAMES.matcher(method.getName()).matches()) {
                if (changeCount++ > batchSize) {
                    em.flush();
                    em.clear();
                    changeCount = 0;
                }
            }
        }
    }

    // JTA 下数据库操作在提交阶段执行，异常传播路径可能不同
    /** 将底层异常链转换为 Keycloak {@link ModelException}。 */
    public static ModelException convert(Throwable t) {
        Predicate<Throwable> throwModelDuplicateEx = throwable ->
                throwable instanceof EntityExistsException
                        || throwable instanceof ConstraintViolationException
                        || isSqlStateClass23(throwable)
                        || throwable instanceof SQLIntegrityConstraintViolationException;
        while (true) {
            if (t instanceof ModelException me) {
                throw me;
            } else if (throwModelDuplicateEx.test(t)) {
                return new ModelDuplicateException("Duplicate resource error", t);
            } else if (t instanceof OptimisticLockException) {
                return new ModelIllegalStateException("Database operation failed", t);
            } else if (t.getCause() == null) {
                return new ModelException("Database operation failed", t);
            } else {
                t = t.getCause();
            }
        }
    }

    /**
     * SQLSTATE 23 类表示完整性约束违反（如 23505 UNIQUE VIOLATION）。
     * 捕获未映射到其他异常类型的 BatchUpdateException 等。
     * @see <a href="https://en.wikipedia.org/wiki/SQLSTATE">SQLSTATE</a>
     */
    private static boolean isSqlStateClass23(Throwable t) {
        return t instanceof SQLException bue
            && bue.getSQLState() != null
            && bue.getSQLState().startsWith("23");
    }

}
