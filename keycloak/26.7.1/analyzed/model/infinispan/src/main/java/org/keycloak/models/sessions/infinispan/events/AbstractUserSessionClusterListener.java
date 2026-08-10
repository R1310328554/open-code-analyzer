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

package org.keycloak.models.sessions.infinispan.events;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterListener;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.Provider;

import org.jboss.logging.Logger;

/**
 * 用户会话集群事件的抽象监听器基类。
 * <p>
 * 通过 {@link KeycloakModelUtils#runJobInTransaction} 在事务中获取指定类型的 Provider 并处理事件。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractUserSessionClusterListener<SE extends SessionClusterEvent, T extends Provider> implements ClusterListener {

    private static final Logger log = Logger.getLogger(AbstractUserSessionClusterListener.class);

    private final KeycloakSessionFactory sessionFactory;

    /** 处理事件时从会话中解析的 Provider 类型。 */
    private final Class<T> providerClazz;

    public AbstractUserSessionClusterListener(KeycloakSessionFactory sessionFactory, Class<T> providerClazz) {
        this.sessionFactory = sessionFactory;
        this.providerClazz = providerClazz;
    }


    @Override
    public void eventReceived(ClusterEvent event) {
        KeycloakModelUtils.runJobInTransaction(sessionFactory, (KeycloakSession session) -> {
            T provider = session.getProvider(providerClazz);
            SE sessionEvent = (SE) event;

            if (log.isDebugEnabled()) {
                log.debugf("Received user session event '%s'.", sessionEvent.toString());
            }

            eventReceived(provider, sessionEvent);
        });
    }

    /** 子类实现：在目标 Provider 上处理具体用户会话集群事件。 */
    protected abstract void eventReceived(T provider, SE sessionEvent);
}
