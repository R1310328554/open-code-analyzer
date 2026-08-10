/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.sessions.infinispan.changes.remote.updater;

import org.keycloak.models.sessions.infinispan.util.SessionTimeouts;

/**
 * Infinispan 存储的过期配置，单位为毫秒。
 *
 * @param maxIdle  最大空闲时间；超过未访问则移除
 * @param lifespan 绝对存活时间；到期后移除
 */
public record Expiration(long maxIdle, long lifespan) {

    /** 判断条目是否已标记为过期（使用 {@link SessionTimeouts#ENTRY_EXPIRED_FLAG}）。 */
    public boolean isExpired() {
        return maxIdle == SessionTimeouts.ENTRY_EXPIRED_FLAG || lifespan == SessionTimeouts.ENTRY_EXPIRED_FLAG;
    }

}
