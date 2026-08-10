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

package org.keycloak.connections.infinispan.shutdown;

/**
 * 表示可能阻塞服务器优雅关闭的条件。
 * <p>
 * 实现类定义"仍在进行中"的含义（例如缓存 rehash），并在条件解除时收到
 * {@link #complete()} 回调，或在关闭超时时收到 {@link #onTimeout()} 回调。
 * <p>
 * Represents a condition that may block shutdown until it is resolved.
 * Implementations define what "in progress" means (e.g. a cache rehash), and receive callbacks when the condition is
 * resolved ({@link #complete()}) or when the shutdown timeout expires ({@link #onTimeout()}).
 */
public interface ShutdownCondition {

    /**
     * 判断关闭等待条件是否仍未完成。
     *
     * @return {@code true} 表示操作仍在进行，关闭流程应继续等待
     */
    boolean inProgress();

    /**
     * 关闭超时到期且条件仍 {@linkplain #inProgress() 进行中} 时调用。
     */
    void onTimeout();

    /**
     * 条件不再 {@linkplain #inProgress() 进行中}、关闭可以继续时调用；
     * 若关闭发起时条件本就不在进行，也会调用此方法。
     */
    void complete();

}
