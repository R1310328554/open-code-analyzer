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

package org.keycloak.models.sessions.infinispan.stream;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureEntity;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureKey;
import org.keycloak.models.sessions.infinispan.util.SessionTimeouts;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

import static org.keycloak.marshalling.Marshalling.LOGIN_FAILURES_LIFESPAN_UPDATE;

/**
 * 根据 realm 暴力破解防护策略更新登录失败缓存条目的存活时间（TTL）的 {@link BiConsumer}。
 * <p>
 * 用于在 Infinispan 缓存中重新计算登录失败记录的过期时间，依据 realm 是否启用永久锁定、
 * 允许的最大临时锁定次数以及失败计数时间窗口等配置。
 * <p>
 * 通过 Infinispan ProtoStream 序列化，以支持远程缓存上的分布式流操作。
 */
@ProtoTypeId(LOGIN_FAILURES_LIFESPAN_UPDATE)
public class LoginFailuresLifespanUpdate implements BiConsumer<Cache<LoginFailureKey, SessionEntityWrapper<LoginFailureEntity>>, Map.Entry<LoginFailureKey, SessionEntityWrapper<LoginFailureEntity>>> {

    /** 跟踪失败次数的最大时间窗口（毫秒）。 */
    @ProtoField(1)
    final long maxDeltaTimeMillis;
    /** 允许的最大临时锁定次数。 */
    @ProtoField(2)
    final int maxTemporaryLockouts;
    /** 是否启用永久锁定。 */
    @ProtoField(3)
    final boolean permanentLockout;

    /**
     * 使用指定的锁定策略参数创建登录失败存活时间更新操作。
     * <p>
     * 标注 {@link ProtoFactory} 以支持 Infinispan ProtoStream 远程缓存序列化。
     *
     * @param maxDeltaTimeMillis   跟踪失败的最大时间窗口（毫秒）
     * @param maxTemporaryLockouts 允许的最大临时锁定次数
     * @param permanentLockout     是否启用永久锁定
     */
    @ProtoFactory
    public LoginFailuresLifespanUpdate(long maxDeltaTimeMillis, int maxTemporaryLockouts, boolean permanentLockout) {
        this.maxDeltaTimeMillis = maxDeltaTimeMillis;
        this.maxTemporaryLockouts = maxTemporaryLockouts;
        this.permanentLockout = permanentLockout;
    }

    /**
     * 根据配置的锁定策略更新登录失败缓存条目的存活时间。
     * <p>
     * 新 TTL 由 {@link SessionTimeouts#getLoginFailuresLifespanMs} 计算，综合考虑当前失败次数、
     * 永久锁定设置与最大时间窗口。使用无锁、静默失败、忽略返回值等 Flag 优化缓存更新性能。
     *
     * @param cache 存放登录失败条目的 Infinispan 缓存
     * @param entry 待更新的缓存键值对（含包装后的登录失败实体）
     */
    @Override
    public void accept(Cache<LoginFailureKey, SessionEntityWrapper<LoginFailureEntity>> cache, Map.Entry<LoginFailureKey, SessionEntityWrapper<LoginFailureEntity>> entry) {
        var entity = entry.getValue().getEntity();
        long lifespan = SessionTimeouts.getLoginFailuresLifespanMs(permanentLockout, maxTemporaryLockouts, maxDeltaTimeMillis, entity);
        cache.getAdvancedCache()
                .withFlags(Flag.ZERO_LOCK_ACQUISITION_TIMEOUT, Flag.FAIL_SILENTLY, Flag.IGNORE_RETURN_VALUES)
                .computeIfPresent(entry.getKey(), ValueIdentityBiFunction.getInstance(), lifespan, TimeUnit.MILLISECONDS);
    }
}
