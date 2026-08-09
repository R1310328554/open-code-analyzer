/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.micronaut.session;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.util.Toggleable;
import io.micronaut.session.http.HttpSessionConfiguration;
import org.redisson.client.codec.Codec;

/**
 * Redisson 分布式 HTTP Session 的 Micronaut 配置（Micronaut 4.x）。
 * <p>绑定 {@code redisson.*} 前缀下的键前缀、编解码器、更新模式与集群广播开关。
 *
 * @author Nikita Koksharov
 */
@ConfigurationProperties("redisson")
public class RedissonHttpSessionConfiguration extends HttpSessionConfiguration implements Toggleable {

    /** Session 属性持久化策略：异步写回或请求结束时批量保存。 */
    public enum UpdateMode {WRITE_BEHIND, AFTER_REQUEST}

    private String keyPrefix = "";
    private Codec codec;
    private UpdateMode updateMode = UpdateMode.AFTER_REQUEST;
    private boolean broadcastSessionUpdates = false;

    public boolean isBroadcastSessionUpdates() {
        return broadcastSessionUpdates;
    }

    /**
     * 是否将 Session 变更广播到所有 Micronaut 服务实例。
     * <p>启用后通过 Redis Topic 同步属性增删改。
     *
     * @param broadcastSessionUpdates {@code true} 时广播变更
     */
    public void setBroadcastSessionUpdates(boolean broadcastSessionUpdates) {
        this.broadcastSessionUpdates = broadcastSessionUpdates;
    }

    public UpdateMode getUpdateMode() {
        return updateMode;
    }

    /**
     * Session 属性更新模式。
     * <p>{@link UpdateMode#WRITE_BEHIND} — 变更立即异步写入 Redis。
     * <p>{@link UpdateMode#AFTER_REQUEST} — 仅在
     * {@link io.micronaut.session.SessionStore#save(io.micronaut.session.Session)} 时批量持久化。
     * <p>默认 {@link UpdateMode#AFTER_REQUEST}。
     *
     * @param updateMode 更新模式
     */
    public void setUpdateMode(UpdateMode updateMode) {
        this.updateMode = updateMode;
    }

    public Codec getCodec() {
        return codec;
    }

    /**
     * Session 属性值的 Redis 编解码器。
     * <p>默认 {@link org.redisson.codec.Kryo5Codec}。
     *
     * @param codec 编解码器实例
     */
    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     * 所有 Session 相关 Redis 键的统一前缀。
     *
     * @param keyPrefix 键前缀字符串
     */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
