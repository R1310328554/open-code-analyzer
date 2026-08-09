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
import org.redisson.api.map.RetryableMapWriter;
import org.redisson.api.map.RetryableMapWriterAsync;

import java.time.Duration;

/**
 * 分布式 Map 读写策略配置（已废弃，请改用 org.redisson.api.options.MapOptions）。
 * <p>可配置 {@link MapLoader}/{@link MapWriter}、write-through/write-behind
 * 及写入重试参数。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
@Deprecated
public class MapOptions<K, V> {
    
    public enum WriteMode {
        
        /** write-behind 模式：map 写入异步批量落库至 {@link MapWriter}。 */

        WRITE_BEHIND,
        
        /**
         * write-through 模式：map 写操作与 {@link MapWriter} 同步；
         * 若 {@link MapWriter} 抛错则原样传递给调用方。
         */
        WRITE_THROUGH
        
    }
    
    private MapLoader<K, V> loader;
    private MapWriter<K, V> writer;
    private MapWriterAsync<K, V> writerAsync;

    private MapLoaderAsync<K, V> loaderAsync;

    private WriteMode writeMode = WriteMode.WRITE_THROUGH;
    private int writeBehindBatchSize = 50;
    private int writeBehindDelay = 1000;
    private int writerRetryAttempts = 0;
    //ms
    private long writerRetryInterval = 100;

    protected MapOptions() {
    }
    
    protected MapOptions(MapOptions<K, V> copy) {
    }
    
    /**
     * 创建默认 {@link MapOptions} 实例。
     * <p>
     * 等价于：
     * <pre>
     *     new MapOptions()
     *      .writer(null, null).loader(null);
     * </pre>
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return MapOptions 实例
     */
    public static <K, V> MapOptions<K, V> defaults() {
        return new MapOptions<K, V>();
    }
    
    /**
     * 设置写操作调用的 {@link MapWriter}。
     *
     * @param writer MapWriter 实例
     * @return MapOptions 实例
     */
    public MapOptions<K, V> writer(MapWriter<K, V> writer) {
        if (writer != null) {
            this.writer = new RetryableMapWriter<>(this, writer);
        }
        return this;
    }
    public MapWriter<K, V> getWriter() {
        return writer;
    }

    /**
     * 设置写操作调用的 {@link MapWriterAsync}。
     *
     * @param writer MapWriterAsync 实例
     * @return MapOptions 实例
     */
    public MapOptions<K, V> writerAsync(MapWriterAsync<K, V> writer) {
        if (writer != null) {
            this.writerAsync = new RetryableMapWriterAsync<>(this, writer);
        }
        return this;
    }
    public MapWriterAsync<K, V> getWriterAsync() {
        return writerAsync;
    }

    /**
     * 设置 write-behind 批量大小；更新累积到指定批次后通过 {@link MapWriter} 写入。
     * <p>
     * 默认值为 <code>50</code>。
     *
     * @param writeBehindBatchSize 批次大小
     * @return MapOptions 实例
     */
    public MapOptions<K, V> writeBehindBatchSize(int writeBehindBatchSize) {
        this.writeBehindBatchSize = writeBehindBatchSize;
        return this;
    }
    public int getWriteBehindBatchSize() {
        return writeBehindBatchSize;
    }
    
    /**
     * 设置 write-behind 任务执行延迟；更新通过 {@link MapWriter} 写入且滞后不超过该延迟。
     * <p>
     * 默认值为 <code>1000</code> 毫秒。
     *
     * @param writeBehindDelay 延迟（毫秒）
     * @return MapOptions 实例
     */
    public MapOptions<K, V> writeBehindDelay(int writeBehindDelay) {
        this.writeBehindDelay = writeBehindDelay;
        return this;
    }
    public int getWriteBehindDelay() {
        return writeBehindDelay;
    }
    
    /**
     * 设置写入模式。
     * <p>
     * 默认值为 <code>{@link WriteMode#WRITE_THROUGH}</code>。
     *
     * @param writeMode 写入模式
     * @return MapOptions 实例
     */
    public MapOptions<K, V> writeMode(WriteMode writeMode) {
        this.writeMode = writeMode;
        return this;
    }
    public WriteMode getWriteMode() {
        return writeMode;
    }

    public int getWriterRetryAttempts() {
        return writerRetryAttempts;
    }

    /**
     * 设置 {@link RetryableMapWriter} 或 {@link RetryableMapWriterAsync} 的最大重试次数。
     *
     * @param writerRetryAttempts 最大重试次数
     * @return MapOptions 实例
     */
    public MapOptions<K, V> writerRetryAttempts(int writerRetryAttempts) {
        if (writerRetryAttempts <= 0){
            throw new IllegalArgumentException("writerRetryAttempts must be bigger than 0");
        }
        this.writerRetryAttempts = writerRetryAttempts;
        return this;
    }

    public long getWriterRetryInterval() {
        return writerRetryInterval;
    }

    /**
     * 设置 {@link RetryableMapWriter} 或 {@link RetryableMapWriterAsync} 的重试间隔。
     *
     * @param writerRetryInterval 重试间隔 {@link Duration}
     * @return MapOptions 实例
     */
    public MapOptions<K, V> writerRetryInterval(Duration writerRetryInterval) {
        if (writerRetryInterval.isNegative()) {
            throw new IllegalArgumentException("writerRetryInterval must be positive");
        }
        this.writerRetryInterval = writerRetryInterval.toMillis();
        return this;
    }

    /**
     * 设置 {@link MapLoader}。
     *
     * @param loader MapLoader 实例
     * @return MapOptions 实例
     */
    public MapOptions<K, V> loader(MapLoader<K, V> loader) {
        this.loader = loader;
        return this;
    }
    public MapLoader<K, V> getLoader() {
        return loader;
    }

    /**
     * 设置 {@link MapLoaderAsync}。
     *
     * @param loaderAsync MapLoaderAsync 实例
     * @return MapOptions 实例
     */
    public MapOptions<K, V> loaderAsync(MapLoaderAsync<K, V> loaderAsync) {
        this.loaderAsync = loaderAsync;
        return this;
    }
    public MapLoaderAsync<K, V> getLoaderAsync() {
        return loaderAsync;
    }
}
