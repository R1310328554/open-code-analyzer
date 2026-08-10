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

package org.keycloak.events;

import org.keycloak.events.admin.AdminEventQuery;
import org.keycloak.models.RealmModel;

/**
 * 事件存储提供者 SPI：持久化用户/管理事件并支持查询与清理。
 * <p>同时继承 {@link EventListenerProvider}，在 {@link EventBuilder} 发送流程中接收 {@link #onEvent(Event)} 回调。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface EventStoreProvider extends EventListenerProvider {

    /**
     * 创建用户事件查询构建器 {@link EventQuery}。
     * <p>链式设置 realm、类型、时间边界等条件后调用 {@link EventQuery#getResultStream()} 获取结果。</p>
     *
     * @return a query object
     */
    EventQuery createQuery();

    /**
     * 创建管理事件查询构建器 {@link AdminEventQuery}。
     *
     * @return a query object
     */
    AdminEventQuery createAdminQuery();

    /**
     * 清空全部用户事件（测试专用，已弃用）。
     *
     * @deprecated Unused method. Currently, used only in the testsuite
     */
    @Deprecated(forRemoval = true, since = "26.7")
    void clear();

    /**
     * 清空指定领域的全部用户事件。
     * @param realm the realm
     *
     */
    void clear(RealmModel realm);

    /**
     * 删除指定领域中早于 {@code olderThan} 的用户事件。
     *
     * @param realm the realm
     * @param olderThan point in time in milliseconds
     */
    void clear(RealmModel realm, long olderThan);

    /**
     * 清理所有领域中已过期的用户事件（已弃用，性能较差）。
     * <p>推荐各存储实现自行处理实体级过期（如 Infinispan entry lifespan）。</p>
     *
     * @deprecated This method is problem from the performance perspective. Some storages can provide better way
     * for doing this (e.g. entry lifespan in the Infinispan server, etc.). We need to leave solving event expiration
     * to each storage provider separately using expiration field on entity level.
     *
     */
    void clearExpiredEvents();

    /**
     * 清空全部管理事件（测试专用，已弃用）。
     *
     * @deprecated Unused method. Currently, used only in the testsuite
     */
    @Deprecated(forRemoval = true, since = "26.7")
    void clearAdmin();

    /**
     * 清空指定领域的全部管理事件。
     * @param realm the realm
     */
    void clearAdmin(RealmModel realm);

    /**
     * 删除指定领域中早于 {@code olderThan} 的管理事件。
     *
     * @param realm the realm
     * @param olderThan point in time in milliseconds
     */
    void clearAdmin(RealmModel realm, long olderThan);

}
