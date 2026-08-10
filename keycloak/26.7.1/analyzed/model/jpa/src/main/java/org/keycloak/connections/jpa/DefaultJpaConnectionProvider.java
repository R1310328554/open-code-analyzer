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

import org.jboss.logging.Logger;

/**
 * 默认 {@link JpaConnectionProvider} 实现，封装单个 {@link EntityManager} 的生命周期。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultJpaConnectionProvider implements JpaConnectionProvider {

    private static final Logger logger = Logger.getLogger(DefaultJpaConnectionProvider.class);
    /** 本会话绑定的 EntityManager。 */
    private final EntityManager em;

    /** 用给定 EntityManager 构造 Provider。 */
    public DefaultJpaConnectionProvider(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /** 关闭底层 EntityManager，释放 JDBC 连接。 */
    @Override
    public void close() {
        logger.trace("DefaultJpaConnectionProvider close()");
        em.close();
    }

}
