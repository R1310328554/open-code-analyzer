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
package org.redisson.micronaut.cache;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;
import org.redisson.api.map.*;
import org.redisson.api.options.MapParams;
import org.redisson.client.codec.Codec;

import java.time.Duration;

/**
 * Micronaut 缓存通用配置基类，封装 {@link MapParams} 与过期/容量策略。
 * <p>子类通过 {@link io.micronaut.context.annotation.EachProperty} 绑定命名缓存实例。
 *
 * @author Nikita Koksharov
 */
public class BaseCacheConfiguration implements Named {

    MapParams<Object, Object> mapOptions;

    private final String name;

    private Codec codec;
    private Duration expireAfterWrite = Duration.ZERO;
    private Duration expireAfterAccess = Duration.ZERO;
    private int maxSize;

    /** 以缓存名称初始化 {@link MapParams} 选项。 */
    public BaseCacheConfiguration(String name) {
        this.name = name;
        this.mapOptions = (MapParams<Object, Object>) org.redisson.api.options.MapOptions.name(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    public Codec getCodec() {
        return codec;
    }

    /**
     * 设置缓存条目的 Redis 编解码器。
     * <p>默认使用 {@link org.redisson.codec.Kryo5Codec}。
     *
     * @param className 编解码器全限定类名
     */
    public void setCodec(String className) {
        this.codec = create(className);
    }

    private <T> T create(String className) {
        try {
            return (T) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Duration getExpireAfterWrite() {
        return expireAfterWrite;
    }

    /**
     * 写入后条目的存活时间（TTL）。
     *
     * @param expireAfterWrite 写入后过期时长
     */
    public void setExpireAfterWrite(Duration expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public Duration getExpireAfterAccess() {
        return expireAfterAccess;
    }

    /**
     * 访问后条目的空闲过期时间（max idle）。
     *
     * @param expireAfterAccess 访问后过期时长
     */
    public void setExpireAfterAccess(Duration expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    public int getMaxSize() {
        return maxSize;
    }

    /**
     * 缓存最大条目数；超出时按 LRU 逐出。
     * <p>{@code 0} 表示不限制容量（默认）。
     *
     * @param maxSize 最大条目数
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 设置 Write-Behind 批量写入大小。
     * <p>MapWriter 执行期间累积的更新达到该批量大小时一并提交。
     * <p>默认 {@code 50}。
     *
     * @param writeBehindBatchSize 批量大小
     */
    public void setWriteBehindBatchSize(int writeBehindBatchSize) {
        mapOptions.writeBehindBatchSize(writeBehindBatchSize);
    }

    /**
     * 设置 Write-Behind 任务执行延迟（毫秒）。
     * <p>所有更新最晚在该延迟内异步落库。
     * <p>默认 {@code 1000} 毫秒。
     *
     * @param writeBehindDelay 延迟毫秒数
     */
    public void setWriteBehindDelay(int writeBehindDelay) {
        mapOptions.writeBehindDelay(writeBehindDelay);
    }

    /**
     * 设置 Write-Through/Write-Behind 使用的 {@link MapWriter}（反射实例化）。
     *
     * @param className MapWriter 全限定类名
     */
    public void setWriter(String className) {
        mapOptions.writer(create(className));
    }

    /**
     * 设置写入模式。
     * <p>默认 {@link WriteMode#WRITE_THROUGH}。
     *
     * @param writeMode 写入模式
     */
    public void setWriteMode(WriteMode writeMode) {
        mapOptions.writeMode(writeMode);
    }

    /**
     * 设置读穿透时使用的 {@link MapLoader}（反射实例化）。
     *
     * @param className MapLoader 全限定类名
     */
    public void setLoader(String className) {
        mapOptions.loader(create(className));
    }

    /** 构建普通 {@link RMap} 选项，合并 loader/writer/codec 等配置。 */
    public <K, V> org.redisson.api.options.MapOptions<K, V> getMapOptions() {
        org.redisson.api.options.MapOptions<K, V> ops = org.redisson.api.options.MapOptions.name(getName());
        ops.writer((MapWriter<K, V>) mapOptions.getWriter());
        ops.writeMode(mapOptions.getWriteMode());
        ops.writerAsync((MapWriterAsync<K, V>) mapOptions.getWriterAsync());
        ops.writeBehindDelay(mapOptions.getWriteBehindDelay());
        ops.writeBehindBatchSize(mapOptions.getWriteBehindBatchSize());
        ops.loader((MapLoader<K, V>) mapOptions.getLoader());
        ops.loaderAsync((MapLoaderAsync<K, V>) mapOptions.getLoaderAsync());
        ops.codec(getCodec());
        return ops;
    }

    /** 构建带 TTL/逐出能力的 {@link RMapCache} 选项。 */
    public <K, V> org.redisson.api.options.MapCacheOptions<K, V> getMapCacheOptions() {
        org.redisson.api.options.MapCacheOptions<K, V> ops = org.redisson.api.options.MapCacheOptions.name(getName());
        ops.writer((MapWriter<K, V>) mapOptions.getWriter());
        ops.writeMode(mapOptions.getWriteMode());
        ops.writerAsync((MapWriterAsync<K, V>) mapOptions.getWriterAsync());
        ops.writeBehindDelay(mapOptions.getWriteBehindDelay());
        ops.writeBehindBatchSize(mapOptions.getWriteBehindBatchSize());
        ops.loader((MapLoader<K, V>) mapOptions.getLoader());
        ops.loaderAsync((MapLoaderAsync<K, V>) mapOptions.getLoaderAsync());
        ops.codec(getCodec());
        return ops;
    }

}
