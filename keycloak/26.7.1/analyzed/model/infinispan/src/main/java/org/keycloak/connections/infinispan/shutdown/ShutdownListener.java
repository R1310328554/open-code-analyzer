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

import java.time.Instant;
import java.util.Date;

/**
 * 服务器开始关闭时的通知回调接口。
 * <p>
 * 实现类可在 {@link #onShutdown(Instant, Date)} 中阻塞关闭线程，直到特定条件满足
 * （例如等待 Infinispan 拓扑稳定），但必须在 {@code deadline} 之前返回。
 *
 * @see ShutdownManager
 */
public interface ShutdownListener {

    /**
     * 服务器关闭流程启动时调用。
     *
     * @param shutdownTime 关闭发起时刻
     * @param deadline     本监听器必须在此绝对截止时间前返回
     */
    void onShutdown(Instant shutdownTime, Date deadline);

}
