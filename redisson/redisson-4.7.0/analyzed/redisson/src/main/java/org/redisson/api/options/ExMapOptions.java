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
package org.redisson.api.options;

import org.redisson.api.map.*;
import org.redisson.client.codec.Codec;

import java.time.Duration;

/**
 * 扩展 Map 选项接口，涵盖写入器、加载器、写后模式及重试等配置。
 *
 * @author Nikita Koksharov
 *
 * @param <T> 链式返回的类型
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface ExMapOptions<T extends ExMapOptions<T, K, V>, K, V> extends CodecOptions<T, Codec>, ReadModeOptions<T> {

    /**
     * 设置写操作期间调用的 {@link MapWriter}。
     *
     * @param writer MapWriter 实例
     * @return MapOptions 实例
     */
    T writer(MapWriter<K, V> writer);

    /**
     * 设置写操作期间调用的异步 {@link MapWriterAsync}。
     *
     * @param writer MapWriterAsync 实例
     * @return MapOptions 实例
     */
    T writerAsync(MapWriterAsync<K, V> writer);
    /**
     * 设置写后（write-behind）任务的批处理大小。
     * 累积的更新按指定批次大小通过 {@link MapWriter} 批量写入。
     * <p>
     * 默认值为 <code>50</code>
     *
     * @param writeBehindBatchSize 批大小
     * @return MapOptions 实例
     */
    T writeBehindBatchSize(int writeBehindBatchSize);

    /**
     * 设置写后任务的执行延迟（毫秒）。
     * 所有更新通过 {@link MapWriter} 写入，且滞后不超过指定延迟。
     * <p>
     * 默认值为 <code>1000</code> 毫秒
     *
     * @param writeBehindDelay 延迟（毫秒）
     * @return MapOptions 实例
     */
    T writeBehindDelay(int writeBehindDelay);

    /**
     * 设置写入模式。
     * <p>
     * 默认值为 <code>{@link WriteMode#WRITE_THROUGH}</code>
     *
     * @param writeMode 写入模式
     * @return MapOptions 实例
     */
    T writeMode(WriteMode writeMode);

    /**
     * 设置写入失败时的最大重试次数。
     *
     * @param writerRetryAttempts 最大重试次数
     * @return MapOptions 实例
     */
    T writeRetryAttempts(int writerRetryAttempts);

    /**
     * 设置写入重试间隔。
     *
     * @param writerRetryInterval 重试间隔 {@link Duration}
     * @return MapOptions 实例
     */
    T writeRetryInterval(Duration writerRetryInterval);

    /**
     * 设置 {@link MapLoader}，用于缓存未命中时从外部加载条目。
     *
     * @param loader MapLoader 实例
     * @return MapOptions 实例
     */
    T loader(MapLoader<K, V> loader);

    /**
     * 设置异步 {@link MapLoaderAsync}，用于缓存未命中时异步加载条目。
     *
     * @param loaderAsync MapLoaderAsync 实例
     * @return MapOptions 实例
     */
    T loaderAsync(MapLoaderAsync<K, V> loaderAsync);

}
