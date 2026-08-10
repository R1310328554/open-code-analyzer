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

import java.util.Date;
import java.util.stream.Stream;

/**
 * 用户事件查询构建器，由 {@link EventStoreProvider#createQuery()} 返回。
 * <p>支持按类型、领域、客户端、用户、时间范围、IP 等条件过滤，并链式设置分页与排序。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface EventQuery {

    /**
     * 按事件类型过滤。
     * @param types requested types
     * @return this object for method chaining
     */
    EventQuery type(EventType... types);

    /**
     * 限定领域。
     * @param realmId id of realm
     * @return this object for method chaining
     */
    EventQuery realm(String realmId);

    /**
     * 限定客户端。
     * @param clientId id of client
     * @return this object for method chaining
     */
    EventQuery client(String clientId);

    /**
     * 限定用户。
     * @param userId id of user
     * @return this object for method chaining
     */
    EventQuery user(String userId);

    /**
     * 起始时间（含），{@link Date} 版本（已弃用）。
     * @param fromDate date
     * @return this object for method chaining
     */
    @Deprecated
    EventQuery fromDate(Date fromDate);

    /**
     * 起始时间戳（含，毫秒）。
     * @param fromDate from timestamp
     * @return this object for method chaining
     */
    EventQuery fromDate(long fromDate);

    /**
     * 截止时间（含），{@link Date} 版本（已弃用）。
     * @param toDate date
     * @return this object for method chaining
     */
    @Deprecated
    EventQuery toDate(Date toDate);

    /**
     * 截止时间戳（含，毫秒）。
     * @param toDate to timestamp
     * @return this object for method chaining
     */
    EventQuery toDate(long toDate);

    /**
     * 按客户端 IP 过滤。
     * @param ipAddress ip
     * @return this object for method chaining
     */
    EventQuery ipAddress(String ipAddress);

    /**
     * 分页起始索引（负值忽略）。
     * @param firstResult the index. Ignored if negative.
     * @return this object for method chaining
     */
    EventQuery firstResult(int firstResult);

    /**
     * 最大返回条数（负值忽略）。
     * @param max a number. Ignored if negative.
     * @return this object for method chaining
     */
    EventQuery maxResults(int max);

    /**
     * 按时间降序排列。
     *
     * @return <code>this</code> for method chaining
     */
    EventQuery orderByDescTime();

    /**
     * 按时间升序排列。
     *
     * @return <code>this</code> for method chaining
     */
    EventQuery orderByAscTime();

    /**
     * 执行查询并以 Stream 返回匹配事件。
     * @return Stream of events. Never returns {@code null}.
     */
    Stream<Event> getResultStream();
}
