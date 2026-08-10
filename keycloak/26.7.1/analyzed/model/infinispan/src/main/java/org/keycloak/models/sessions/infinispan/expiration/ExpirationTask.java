/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.expiration;

/**
 * 用户/客户端会话过期清理任务的生命周期接口。
 * <p>
 * 实现负责从数据库删除过期会话；{@link #start()} 在 Keycloak 启动时调用，{@link #stop()} 在关闭时调用。
 */
public interface ExpirationTask {

    /**
     * 启动过期清理任务。
     * <p>
     * 仅在 Keycloak 启动时调用一次；实现需自行调度后续轮次，防止数据库无限增长。
     */
    void start();

    /**
     * 停止过期清理任务并释放调度资源。
     */
    void stop();

}
