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
package org.redisson.api;

import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapLoaderAsync;
import org.redisson.api.map.MapWriter;
import org.redisson.api.map.MapWriterAsync;

import java.time.Duration;

/**
 * {@link RMapCache} 的配置选项（已废弃）。
 * <p>扩展 {@link MapOptions}，支持在条目驱逐完成后移除空的驱逐任务。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
@Deprecated
public class MapCacheOptions<K, V> extends MapOptions<K, V> {

    private boolean removeEmptyEvictionTask;

    public static <K, V> MapCacheOptions<K, V> defaults() {
        return new MapCacheOptions<>();
    }

    @Override
    public MapCacheOptions<K, V> writer(MapWriter<K, V> writer) {
        return (MapCacheOptions<K, V>) super.writer(writer);
    }

    @Override
    public MapCacheOptions<K, V> writerAsync(MapWriterAsync<K, V> writer) {
        return (MapCacheOptions<K, V>) super.writerAsync(writer);
    }

    @Override
    public MapCacheOptions<K, V> writeBehindBatchSize(int writeBehindBatchSize) {
        return (MapCacheOptions<K, V>) super.writeBehindBatchSize(writeBehindBatchSize);
    }

    @Override
    public MapCacheOptions<K, V> writeBehindDelay(int writeBehindDelay) {
        return (MapCacheOptions<K, V>) super.writeBehindDelay(writeBehindDelay);
    }

    @Override
    public MapCacheOptions<K, V> writeMode(WriteMode writeMode) {
        return (MapCacheOptions<K, V>) super.writeMode(writeMode);
    }

    @Override
    public MapCacheOptions<K, V> loader(MapLoader<K, V> loader) {
        return (MapCacheOptions<K, V>) super.loader(loader);
    }

    @Override
    public MapCacheOptions<K, V> loaderAsync(MapLoaderAsync<K, V> loaderAsync) {
        return (MapCacheOptions<K, V>) super.loaderAsync(loaderAsync);
    }

    @Override
    public MapCacheOptions<K, V> writerRetryAttempts(int writerRetryAttempts) {
        return (MapCacheOptions<K, V>) super.writerRetryAttempts(writerRetryAttempts);
    }

    @Override
    public MapCacheOptions<K, V> writerRetryInterval(Duration writerRetryInterval) {
        return (MapCacheOptions<K, V>) super.writerRetryInterval(writerRetryInterval);
    }

    public boolean isRemoveEmptyEvictionTask() {
        return removeEmptyEvictionTask;
    }

    /**
     * 条目驱逐完成后，若 map 已空则从内存中移除驱逐任务。
     *
     * @return MapCacheOptions 实例
     */
    public MapCacheOptions<K, V> removeEmptyEvictionTask() {
        this.removeEmptyEvictionTask = true;
        return this;
    }

}
