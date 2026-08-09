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
 * Redisson 分布式 HTTP Session 的 Micronaut 配置（{@code redisson.*}，Micronaut 2.x）。
 * <p>扩展 {@link HttpSessionConfiguration}，支持 Redis 键前缀、编解码器、更新模式与跨节点广播。
 *
 * @author Nikita Koksharov
 */
@ConfigurationProperties("redisson")
public class RedissonHttpSessionConfiguration extends HttpSessionConfiguration implements Toggleable {

    /** Session 属性写入 Redis 的时机策略。 */
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
     *
     * @param broadcastSessionUpdates 为 {@code true} 时通过 Redis Topic 同步属性变更
     */
    public void setBroadcastSessionUpdates(boolean broadcastSessionUpdates) {
        this.broadcastSessionUpdates = broadcastSessionUpdates;
    }

    public UpdateMode getUpdateMode() {
        return updateMode;
    }

    /**
     * 设置 Session 属性更新模式。
     * <p>{@link UpdateMode#WRITE_BEHIND}：变更异步写入 Redis。
     * <p>{@link UpdateMode#AFTER_REQUEST}：仅在
     * {@link io.micronaut.session.SessionStore#save(io.micronaut.session.Session)} 时批量落库。
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
     * 设置 Session 属性值的 Redis 编解码器。
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
     * 设置写入 Redis 时所有 Session 相关键的统一前缀。
     *
     * @param keyPrefix 键前缀字符串
     */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
