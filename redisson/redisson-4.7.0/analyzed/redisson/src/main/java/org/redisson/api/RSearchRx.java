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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.redisson.api.search.SpellcheckOptions;
import org.redisson.api.search.aggregate.AggregationEntry;
import org.redisson.api.search.aggregate.AggregationOptions;
import org.redisson.api.search.aggregate.AggregationResult;
import org.redisson.api.search.aggregate.IterableAggregationOptions;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.IndexInfo;
import org.redisson.api.search.index.IndexOptions;
import org.redisson.api.search.profile.AggregateProfileResult;
import org.redisson.api.search.profile.ProfileAggregationOptions;
import org.redisson.api.search.profile.ProfileQueryOptions;
import org.redisson.api.search.profile.SearchProfileResult;
import org.redisson.api.search.query.QueryOptions;
import org.redisson.api.search.query.SearchResult;
import org.redisson.api.search.query.hybrid.HybridQueryArgs;
import org.redisson.api.search.query.hybrid.HybridSearchResult;

import java.util.List;
import java.util.Map;

/**
 * RediSearch 模块的 RxJava3 API。
 *
 * @author Nikita Koksharov
 *
 */
public interface RSearchRx {

    /**
     * 创建索引。
     * <p>
     * Code example:
     * <pre>
     *             search.create("idx", IndexOptions.defaults()
     *                                     .on(IndexType.HASH)
     *                                     .prefix(Arrays.asList("doc:")),
     *                                     FieldIndex.text("t1"),
     *                                     FieldIndex.tag("t2").withSuffixTrie());
     * </pre>
     *
     * @param indexName 索引名称
     * @param options 索引选项
     * @param fields 字段索引列表
     */
    Completable createIndex(String indexName, IndexOptions options, FieldIndex... fields);

    /**
     * 在指定索引上执行搜索查询。
     * <p>
     * Code example:
     * <pre>
     * SearchResult r = s.search("idx", "*", QueryOptions.defaults()
     *                                                   .returnAttributes(new ReturnAttribute("t1"), new ReturnAttribute("t2")));
     * </pre>
     *
     * @param indexName 索引名称
     * @param query 查询表达式
     * @param options 查询选项
     * @return 搜索结果
     */
    Single<SearchResult> search(String indexName, String query, QueryOptions options);

    /**
     * 通过 {@code FT.HYBRID} 命令执行混合搜索，结合全文检索与向量相似度。
     * <p>
     * 需要 <b>Redis Stack 8.4.0 及以上</b>。
     *
     * @param indexName 索引名称
     * @param args 混合查询参数
     * @return 搜索结果
     */
    Single<HybridSearchResult> hybridSearch(String indexName, HybridQueryArgs args);

    /**
     * 在指定索引上执行聚合查询。
     * <p>
     * Code example:
     * <pre>
     * AggregationResult r = s.aggregate("idx", "*", AggregationOptions.defaults()
     *                                                                 .load("t1", "t2"));
     * </pre>
     *
     * @param indexName 索引名称
     * @param query 查询表达式
     * @param options 聚合选项
     * @return 聚合结果
     */
    Single<AggregationResult> aggregate(String indexName, String query, AggregationOptions options);

    /**
     * 在指定索引上执行聚合查询。
     * <p>
     * Code example:
     * <pre>
     * Iterable<AggregationEntry> r = s.aggregate("idx", "*", IterableAggregationOptions.defaults()
     *                                                                 .load("t1", "t2"));
     * </pre>
     *
     * @param indexName 索引名称
     * @param query 查询表达式
     * @param options 可迭代聚合选项
     * @return 可迭代聚合条目
     */
    Single<AggregationEntry> aggregate(String indexName, String query, IterableAggregationOptions options);

    /**
     * 在指定索引上执行搜索并通过 {@code FT.PROFILE} 收集性能分析信息。
     *
     * @param indexName 索引名称
     * @param query 查询表达式
     * @param options 性能分析查询选项（继承 {@link QueryOptions}）
     * @return 搜索性能分析结果
     */
    Single<SearchProfileResult> profileSearch(String indexName, String query, ProfileQueryOptions options);

    /**
     * 在指定索引上执行聚合并通过 {@code FT.PROFILE} 收集性能分析信息。
     *
     * @param indexName 索引名称
     * @param query 查询表达式
     * @param options 性能分析聚合选项（继承 {@link AggregationOptions}）
     * @return 聚合性能分析结果
     */
    Single<AggregateProfileResult> profileAggregate(String indexName, String query, ProfileAggregationOptions options);

    /**
     * 为指定索引添加别名。
     *
     * @param alias 别名
     * @param indexName 索引名称
     */
    Completable addAlias(String alias, String indexName);

    /**
     * 删除索引别名。
     *
     * @param alias 别名
     */
    Completable delAlias(String alias);

    /**
     * 为指定索引添加别名。.
     * Re-assigns the alias if it was used before with a different index.
     *
     * @param alias 别名
     * @param indexName 索引名称
     */
    Completable updateAlias(String alias, String indexName);

    /**
     * 返回指定索引已定义的全部别名列表
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param indexName 索引名称
     * @return 别名列表
     */
    Single<List<String>> getAliases(String indexName);

    /**
     * 向索引添加新字段属性。
     *
     * @param indexName 索引名称
     * @param skipInitialScan 为 true 时跳过初始全量扫描
     * @param fields field indexes
     */
    Completable alter(String indexName, boolean skipInitialScan, FieldIndex... fields);

    /**
     * 按参数名返回配置映射。
     *
     * @param parameter 参数名称
     * @return 配置映射
     */
    Maybe<Map<String, String>> getConfig(String parameter);

    /**
     * 按参数名设置配置值。
     *
     * @param parameter 参数名称
     * @param value 参数值
     */
    Completable setConfig(String parameter, String value);

    /**
     * 按索引名与游标 ID 删除游标。
     *
     * @param indexName 索引名称
     * @param cursorId 游标 ID
     */
    Completable delCursor(String indexName, long cursorId);

    /**
     * 按索引名与游标 ID 读取下一批聚合结果。
     *
     * @param indexName 索引名称
     * @param cursorId 游标 ID
     * @return 聚合结果
     */
    Single<AggregationResult> readCursor(String indexName, long cursorId);

    /**
     * 按索引名、游标 ID 与批次大小读取下一批聚合结果
     *
     * @param indexName 索引名称
     * @param cursorId 游标 ID
     * @param count 结果批次大小
     * @return 聚合结果
     */
    Single<AggregationResult> readCursor(String indexName, long cursorId, int count);

    /**
     * 向词典添加指定词条。
     *
     * @param dictionary 词典名称
     * @param terms 词条列表
     * @return 新增词条数量
     */
    Single<Long> addDict(String dictionary, String... terms);

    /**
     * 从词典删除指定词条。
     *
     * @param dictionary 词典名称
     * @param terms 词条列表
     * @return 删除词条数量
     */
    Single<Long> delDict(String dictionary, String... terms);

    /**
     * 返回词典中存储的全部词条。
     *
     * @param dictionary 词典名称
     * @return 词条列表
     */
    Maybe<List<String>> dumpDict(String dictionary);

    /**
     * 按名称删除索引。
     *
     * @param indexName 索引名称
     */
    Completable dropIndex(String indexName);

    /**
     * 按名称删除索引。 and associated documents.
     * Associated documents are deleted asynchronously.
     * Method {@link #info(String)} can be used to check for process completion.
     *
     * @param indexName 索引名称
     */
    Completable dropIndexAndDocuments(String indexName);

    /**
     * 按名称返回索引元信息。
     *
     * @param indexName 索引名称
     * @return 索引信息
     */
    Single<IndexInfo> info(String indexName);

    /**
     * 返回索引是否存在。
     *
     * @param indexName 索引名称
     * @return 是否存在
     */
    Single<Boolean> hasIndex(String indexName);

    /**
     * 在指定索引上对查询执行拼写检查，返回拼写错误词及其得分映射。
     *
     * <pre>
     * Map<String, Map<String, Double>> res = s.spellcheck("idx", "Hocke sti", SpellcheckOptions.defaults()
     *                                                                                          .includedTerms("name"));
     * </pre>
     *
     * @param indexName 索引名称
     * @param query 查询文本
     * @param options 拼写检查选项
     * @return 拼写错误词及其得分映射
     */
    Single<Map<String, Map<String, Double>>> spellcheck(String indexName, String query, SpellcheckOptions options);

    /**
     * 返回指定索引的同义词映射（词 → 同义词列表）。
     *
     * @param indexName 索引名称
     * @return 同义词映射
     */
    Single<Map<String, List<String>>> dumpSynonyms(String indexName);

    /**
     * 更新同义词组。
     *
     * @param indexName 索引名称
     * @param synonymGroupId 同义词组 ID
     * @param terms 词条列表
     */
    Completable updateSynonyms(String indexName, String synonymGroupId, String... terms);

    /**
     * 返回全部已创建索引的名称列表。
     *
     * @return 索引名称列表
     */
    Single<List<String>> getIndexes();

}
