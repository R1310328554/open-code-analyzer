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

import java.util.Collection;
import java.util.Set;
import org.redisson.api.bloomfilter.BloomFilterInfo;
import org.redisson.api.bloomfilter.BloomFilterInfoOption;
import org.redisson.api.bloomfilter.BloomFilterInitArgs;
import org.redisson.api.bloomfilter.BloomFilterInsertArgs;
import org.redisson.api.bloomfilter.BloomFilterScanDumpInfo;

/**
 * 基于 Redis {@code BF.*} 命令的原生布隆过滤器 API。
 * <p>支持 {@code BF.RESERVE/BF.ADD/BF.INSERT/BF.MEXISTS} 及扫描导出等操作。
 *
 * @author Su Ko
 * @param <T> 元素类型
 */
public interface RBloomFilterNativeAsync<T> extends RExpirableAsync {

    /**
     * 添加单个元素。
     *
     * @param element 待添加元素
     * @return 新插入为 {@code true}，已存在为 {@code false}
     */
    RFuture<Boolean> addAsync(T element);

    /**
     * 批量添加元素。
     *
     * @param elements 待添加元素集合
     * @return 各元素是否新插入成功的集合
     */
    RFuture<Set<T>> addAsync(Collection<T> elements);

    /**
     * 若过滤器不存在且非 NOCREATE 模式则创建，并批量添加元素。
     *
     * @param args 插入参数
     * @return 各元素是否新插入成功的集合
     */
    RFuture<Set<T>> insertAsync(BloomFilterInsertArgs<T> args);

    /**
     * 以误判率与预期容量初始化布隆过滤器。
     *
     * @param errorRate 可接受的误判率
     * @param capacity 预期元素数量
     */
    RFuture<Void> initAsync(double errorRate, long capacity);

    /**
     * 使用参数对象初始化布隆过滤器。
     *
     * @param args 初始化参数
     */
    RFuture<Void> initAsync(BloomFilterInitArgs args);

    /**
     * 检测单个元素是否可能存在。
     *
     * @param element 待检测元素
     * @return 可能存在为 {@code true}，肯定不存在为 {@code false}
     */
    RFuture<Boolean> existsAsync(T element);

    /**
     * 批量检测元素是否可能存在。
     *
     * @param elements 待检测元素集合
     * @return 各元素是否可能存在的集合
     */
    RFuture<Set<T>> existsAsync(Collection<T> elements);

    /**
     * 返回过滤器中可能存在的元素计数。
     *
     * @return 元素计数
     */
    RFuture<Long> countAsync();

    /**
     * 返回布隆过滤器完整信息。
     *
     * @return 过滤器信息对象
     */
    RFuture<BloomFilterInfo> getInfoAsync();

    /**
     * 返回指定选项的布隆过滤器信息值。
     *
     * @param option 信息选项
     * @return 对应信息值
     */
    RFuture<Long> getInfoAsync(BloomFilterInfoOption option);

    /**
     * 返回扫描导出信息（{@code BF.SCANDUMP}）。
     * <p>需要 <b>Redis Bloom 1.0.0 及以上</b>；迭代从 0 开始。
     *
     * @param iterator 上次 {@code BF.SCANDUMP} 返回的迭代器
     * @return 扫描导出信息
     */
    RFuture<BloomFilterScanDumpInfo> scanDumpAsync(long iterator);

    /**
     * 加载扫描导出的数据块。
     * <p>需要 <b>Redis Bloom 1.0.0 及以上</b>。
     *
     * @param iterator 上次 {@code BF.SCANDUMP} 返回的迭代器
     * @param data 待加载的数据
     */
    RFuture<Void> loadChunkAsync(long iterator, byte[] data);
}


