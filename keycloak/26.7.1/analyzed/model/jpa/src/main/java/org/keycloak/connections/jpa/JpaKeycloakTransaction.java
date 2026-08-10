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

package org.keycloak.connections.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import org.keycloak.models.KeycloakTransaction;

import org.jboss.logging.Logger;

/**
 * 基于 JPA {@link EntityManager} 的 Keycloak 事务适配器，将领域事务语义委托给底层 JTA/资源本地事务。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class JpaKeycloakTransaction implements KeycloakTransaction {

    private static final Logger logger = Logger.getLogger(JpaKeycloakTransaction.class);

    /** 参与本事务的 EntityManager 实例。 */
    protected EntityManager em;

    public JpaKeycloakTransaction(EntityManager em) {
        this.em = em;
    }

    /** 启动底层 JPA 事务。 */
    @Override
    public void begin() {
        em.getTransaction().begin();
    }

    /** 提交事务；持久化异常经 {@link PersistenceExceptionConverter} 转为领域异常后抛出。 */
    @Override
    public void commit() {
        try {
            logger.trace("Committing transaction");
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            throw PersistenceExceptionConverter.convert(e);
        }
    }

    /** 回滚当前事务。 */
    @Override
    public void rollback() {
        logger.trace("Rollback transaction");
        em.getTransaction().rollback();
    }

    /** 标记事务为仅回滚，后续 commit 将失败。 */
    @Override
    public void setRollbackOnly() {
        em.getTransaction().setRollbackOnly();
    }

    /** 查询事务是否已被标记为仅回滚。 */
    @Override
    public boolean getRollbackOnly() {
        return  em.getTransaction().getRollbackOnly();
    }

    /** 判断 JPA 事务是否仍处于活动状态。 */
    @Override
    public boolean isActive() {
        return em.getTransaction().isActive();
    }
}
