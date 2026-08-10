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

package org.keycloak.expiration.jpa;

import java.util.function.IntConsumer;

import org.keycloak.models.KeycloakSession;

/**
 * 从数据库删除过期条目的回调函数式接口。
 * <p>
 * 实现类执行实际删除逻辑（如 JPA 查询），并通过 {@code removeCount} 上报本批删除行数。
 * 框架在事务内调用，可循环多次以支持分批删除。
 * </p>
 *
 * @see ExpirationTaskBuilder
 */
@FunctionalInterface
public interface ExpirationAction {

    /**
     * 删除一批过期条目。
     * <p>
     * 在事务内调用；应删除过期时间早于或等于 {@code currentTime} 的条目，
     * 并通过 {@code removeCount} 报告本批删除数量。
     * </p>
     *
     * @param session     当前 Keycloak 会话，在 enclosing 事务期间有效。
     * @param realmId     待清理的 realm；非 realm 感知任务时为 {@code null}。
     * @param currentTime 当前 Unix 秒时间戳，作为过期阈值；单次任务运行内各批次保持不变。
     * @param maxRemoval  本批最多删除的条目数。
     * @param removeCount 用于上报本批实际删除数量的消费者。
     * @return {@code true} 表示仍有更多过期条目待删（框架将在新事务中再次调用）；
     *         {@code false} 表示已全部清理完毕。
     */
    boolean removeExpired(KeycloakSession session, String realmId, int currentTime, int maxRemoval, IntConsumer removeCount);

}
