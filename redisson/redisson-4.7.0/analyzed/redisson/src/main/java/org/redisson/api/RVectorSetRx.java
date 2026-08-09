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

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import org.redisson.api.vector.VectorAddArgs;
import org.redisson.api.vector.VectorInfo;
import org.redisson.api.vector.VectorSimilarArgs;
import org.redisson.client.protocol.ScoreAttributesEntry;
import org.redisson.client.protocol.ScoredEntry;
import io.reactivex.rxjava3.core.Single;
import org.redisson.codec.JsonCodec;

/**
 * 向量集合（Vector Set）异步 API；各方法返回 {@link RFuture}。
 *
 * @author Nikita Koksharov
 *
 */
public interface RVectorSetRx extends RExpirableRx {

    /**
     * 添加或更新向量元素。
     *
     * @param args 添加参数（元素名、向量、属性等）
     * @return 新增则为 {@code true}，更新已有元素则为 {@code false}
     */
    Single<Boolean> add(VectorAddArgs args);

    /**
     * 返回集合中元素数量。
     *
     * @return 元素数量
     */
    Single<Integer> size();

    /**
     * 返回向量维度数。
     *
     * @return 向量维度数
     */
    Single<Integer> dimensions();

    /**
     * 获取指定元素名称对应的近似向量坐标。
     *
     * @param name 元素名称
     * @return 向量坐标列表
     */
    Maybe<List<Double>> getVector(String name);

    /**
     * 获取指定元素名称对应的向量原始内部表示。
     *
     * @param name 元素名称
     * @return 向量原始值列表
     */
    Maybe<List<Object>> getRawVector(String name);

    /**
     * 获取指定元素名称关联的属性对象。
     *
     * @param name 元素名称
     * @param clazz 反序列化目标类型
     * @return 属性对象
     */
    <T> Maybe<T> getAttributes(String name, Class<T> clazz);

    /**
     * 返回当前向量集合的元数据信息。
     *
     * @return 向量集合元数据
     */
    Single<VectorInfo> getInfo();

    /**
     * 获取指定元素在 HNSW 图索引中的邻居元素名称。
     *
     * @param element 元素名称
     * @return 邻居元素名称列表
     */
    Maybe<List<String>> getNeighbors(String element);

    /**
     * 获取指定元素的邻居元素及其距离分数。
     *
     * @param element 元素名称
     * @return 带分数的邻居元素列表
     */
    Maybe<List<ScoredEntry<String>>> getNeighborEntries(String element);

    /**
     * 随机返回一个元素名称。
     *
     * @return 随机元素名称
     */
    Maybe<String> random();

    /**
     * 随机返回多个元素名称。
     *
     * @param count 返回元素数量
     * @return 随机元素名称列表
     */
    Maybe<List<String>> random(int count);

    /**
     * 按名称移除元素。
     *
     * @param element 元素名称 to remove
     * @return 移除成功则为 {@code true}，否则 {@code false}
     */
    Single<Boolean> remove(String element);

    /**
     * 为指定元素设置 JSON 属性。
     *
     * @param element 元素名称
     * @param attributes 属性对象
     * @param jsonCodec 属性 JSON 编解码器
     * @return 设置成功则为 {@code true}，否则 {@code false}
     */
    Single<Boolean> setAttributes(String element, Object attributes, JsonCodec jsonCodec);

    /**
     * 按向量或元素名称检索相似元素名称列表。
     *
     * @param args 向量相似度检索参数
     * @return 相似元素名称列表
     */
    Maybe<List<String>> getSimilar(VectorSimilarArgs args);

    /**
     * 检索相似元素名称及其相似度分数。
     *
     * @param args 相似度检索参数
     * @return 相似元素名称列表 with scores
     */
    Maybe<List<ScoredEntry<String>>> getSimilarEntries(VectorSimilarArgs args);

    /**
     * 检索相似元素名称、分数及关联属性。
     *
     * @param args 相似度检索参数
     * @return 相似元素名称列表 with scores and attributes
     */
    Maybe<List<ScoreAttributesEntry<String>>> getSimilarEntriesWithAttributes(VectorSimilarArgs args);

    /**
     * 检查指定元素是否属于当前向量集合。
     *
     * @param element 元素名称
     * @return 存在则为 {@code true}，否则 {@code false}
     */
    Single<Boolean> contains(String element);

    /**
     * 返回指定字典序范围内的元素名称。
     * <p>
     * 边界默认为闭区间；{@code startElement} 用 {@code -}、{@code endElement} 用 {@code +} 表示全集合；元素名前加 {@code [}（含）或 {@code (}（不含）可显式指定开闭区间。
     *
     * @param startElement 字典序范围起始（含）
     * @param endElement 字典序范围结束（含）
     * @return 范围内元素名称列表
     */
    Maybe<List<String>> range(String startElement, String endElement);

    /**
     * 返回字典序范围内至多 {@code count} 个元素名称。
     * <p>
     * 边界规则同 {@link #range(String, String)}。
     *
     * @param startElement 字典序范围起始（含）
     * @param endElement 字典序范围结束（含）
     * @param count 最多返回元素数
     * @return 范围内元素名称列表
     */
    Maybe<List<String>> range(String startElement, String endElement, int count);

    /**
     * 按字典序返回全部元素名称的响应式流；元素分批懒加载。
     *
     * @return 元素名称流
     */
    Flowable<String> iterator();
}
