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

import java.time.Duration;

/**
 * 过期清理任务每次运行结束后的回调监听器。
 * <p>
 * 可用于日志、告警或自定义指标。启用指标时，可通过
 * {@link ExpirationTaskBuilder#withMetrics(boolean)} 自动注册内置 Micrometer 监听器。
 * </p>
 *
 * @see ExpirationTaskBuilder#withListener(ExpirationListener)
 */
@FunctionalInterface
public interface ExpirationListener {

    /**
     * 某次过期清理（全局或按 realm）完成后被调用。
     *
     * @param realmId  被清理的 realm ID；非 realm 感知任务时为 {@code null}。
     * @param outcome  本次任务执行结果。
     * @param removed  所有批次合计删除的过期条目数。
     * @param duration 任务运行的墙钟耗时。
     */
    void onTaskRun(String realmId, Outcome outcome, int removed, Duration duration);
}
