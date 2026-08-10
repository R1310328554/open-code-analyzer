/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.testsuite.model.infinispan;

import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanUtil.setTimeServiceToKeycloakTime;

/**
 * Infinispan 测试时间服务工具类：在测试中让缓存管理器感知 Keycloak 时间偏移。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanTestUtil {

    protected static final Logger logger = Logger.getLogger(InfinispanTestUtil.class);

    /** 切换前保存的原始 TimeService 恢复回调。 */
    private static Runnable origTimeService = null;

    /**
     * 将 Keycloak 测试 TimeService 设置到 Infinispan cacheManager。这样 Infinispan 能感知
     * Keycloak 时间偏移，便于测试通过 {@link org.keycloak.common.util.Time#setOffset} 前移时间后条目是否过期。
     *
     * @param session 当前 Keycloak 会话
     */
    public static void setTestingTimeService(KeycloakSession session) {
        // 测试 TimeService 已设置；若工具类使用正确则不应发生
        if (origTimeService != null) {
            throw new IllegalStateException("Calling setTestingTimeService when testing TimeService was already set");
        }

        InfinispanConnectionProvider ispnProvider = session.getProvider(InfinispanConnectionProvider.class);
        if (ispnProvider != null) {
            logger.info("Will set KeycloakIspnTimeService to the infinispan cacheManager");
            EmbeddedCacheManager cacheManager = ispnProvider.getCache(InfinispanConnectionProvider.USER_CACHE_NAME).getCacheManager();
            origTimeService = setTimeServiceToKeycloakTime(cacheManager);
        }
    }

    /**
     * 恢复 Infinispan 原始 TimeService。
     *
     * @param session 当前 Keycloak 会话
     */
    public static void revertTimeService(KeycloakSession session) {
        // 测试 TimeService 未设置；若工具类使用正确则不应发生
        InfinispanConnectionProvider ispnProvider = session.getProvider(InfinispanConnectionProvider.class);
        if (ispnProvider != null) {
            if (origTimeService == null) {
                throw new IllegalStateException("Calling revertTimeService when testing TimeService was not set");
            }

            origTimeService.run();
            origTimeService = null;
        }
    }
}
