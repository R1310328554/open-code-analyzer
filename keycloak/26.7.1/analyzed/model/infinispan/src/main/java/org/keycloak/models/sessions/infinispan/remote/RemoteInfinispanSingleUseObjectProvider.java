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

package org.keycloak.models.sessions.infinispan.remote;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.sessions.infinispan.entities.SingleUseObjectValueEntity;
import org.keycloak.models.sessions.infinispan.remote.transaction.SingleUseObjectTransaction;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.jboss.logging.Logger;

/**
 * 基于远程 Infinispan 的 {@link SingleUseObjectProvider} 实现。
 * <p>
 * 管理操作令牌、撤销令牌标记等一次性对象；写入与删除经 {@link SingleUseObjectTransaction}
 * 批量提交，撤销令牌时额外通知持久化层。
 */
public class RemoteInfinispanSingleUseObjectProvider implements SingleUseObjectProvider {

    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    /** 表示已撤销令牌的占位实体（空 notes）。 */
    public static final SingleUseObjectValueEntity REVOKED_TOKEN_VALUE = new SingleUseObjectValueEntity(Collections.emptyMap());

    /** 一次性对象变更事务。 */
    private final SingleUseObjectTransaction transaction;
    /** 令牌撤销时的回调（可选持久化）。 */
    private final RevokeTokenConsumer revokeTokenConsumer;

    public RemoteInfinispanSingleUseObjectProvider(SingleUseObjectTransaction transaction, RevokeTokenConsumer revokeTokenConsumer) {
        this.transaction = Objects.requireNonNull(transaction);
        this.revokeTokenConsumer = Objects.requireNonNull(revokeTokenConsumer);

    }

    @Override
    public void put(String key, long lifespanSeconds, Map<String, String> notes) {
        Objects.requireNonNull(key);
        if (lifespanSeconds <= 0) {
            throw new IllegalArgumentException("lifespanSeconds must be positive");
        }
        if (key.endsWith(REVOKED_KEY)) {
            revokeToken(key, lifespanSeconds);
            return;
        }
        transaction.put(key, wrap(notes), lifespanSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Map<String, String> get(String key) {
        Objects.requireNonNull(key);
        return unwrap(transaction.get(key));
    }

    @Override
    public Map<String, String> remove(String key) {
        Objects.requireNonNull(key);
        try {
            // 先读后删：Infinispan 状态转移期间 remove 可能不返回值，getWithMetadata 可保证取回旧值
            // 待 https://github.com/infinispan/infinispan/issues/16703 修复后可移除此 workaround
            var data = transaction.getCache().getWithMetadata(key);
            if (data == null) {
                return null;
            }
            return transaction.getCache().removeWithVersion(key, data.getVersion()) ?
                    unwrap(data.getValue()) :
                    null;
        } catch (HotRodClientException re) {
            // 无需重试：Hot Rod 客户端已对随机网络错误内置重试；锁冲突通常表示他处已并发移除
            logger.debugf(re, "Failed when removing code %s", key);
            return null;
        }
    }

    @Override
    public boolean replace(String key, Map<String, String> notes) {
        Objects.requireNonNull(key);
        return withReturnValue().replace(key, wrap(notes)) != null;
    }

    @Override
    public boolean putIfAbsent(String key, long lifespanInSeconds) {
        Objects.requireNonNull(key);
        if (lifespanInSeconds <= 0) {
            throw new IllegalArgumentException("lifespanInSeconds must be positive");
        }
        try {
            boolean result = withReturnValue().putIfAbsent(key, wrap(null), lifespanInSeconds, TimeUnit.SECONDS) == null;
            if (key.endsWith(REVOKED_KEY)) {
                revokeToken(key, lifespanInSeconds);
            }
            return result;
        } catch (HotRodClientException re) {
            // 无需重试：锁冲突通常表示令牌已在其他节点被消费
            logger.debugf(re, "Failed when adding token %s", key);
            return false;
        }
    }

    @Override
    public boolean contains(String key) {
        Objects.requireNonNull(key);
        return transaction.getCache().containsKey(key);
    }

    @Override
    public void close() {

    }

    /** 返回强制返回旧值的远程缓存视图，用于条件写操作。 */
    private RemoteCache<String, SingleUseObjectValueEntity> withReturnValue() {
        return transaction.getCache().withFlags(Flag.FORCE_RETURN_VALUE);
    }

    /** 写入撤销标记并通知持久化 Provider 记录撤销令牌。 */
    private void revokeToken(String key, long lifespanSeconds) {
        transaction.put(key, REVOKED_TOKEN_VALUE, lifespanSeconds, TimeUnit.SECONDS);
        var token = key.substring(0, key.length() - REVOKED_KEY.length());
        revokeTokenConsumer.onTokenRevoke(token, lifespanSeconds);
    }

    private static Map<String, String> unwrap(SingleUseObjectValueEntity entity) {
        return entity == null ? null : entity.getNotes();
    }

    private static SingleUseObjectValueEntity wrap(Map<String, String> notes) {
        return new SingleUseObjectValueEntity(notes);
    }

    /** 令牌撤销时的回调接口。 */
    public interface RevokeTokenConsumer {
        void onTokenRevoke(String token, long lifespanSeconds);
    }
}
