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

package org.keycloak.models.sessions.infinispan.query;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.infinispan.client.hotrod.impl.query.RemoteQuery;
import org.infinispan.commons.api.query.Query;
import org.infinispan.commons.api.query.QueryResult;

/**
 * 远程 Infinispan Ickle 查询的通用辅助工具。
 * <p>
 * 提供投影类型转换、单条结果获取、全量流式分批拉取等能力，避免一次性加载大结果集。
 */
public final class QueryHelper {

    /** 将单元素投影数组转换为 {@link Long}。 */
    public static final Function<Object[], Long> SINGLE_PROJECTION_TO_LONG = projection -> {
        assert projection.length == 1;
        return (long) projection[0];
    };

    /** 将单元素投影数组转换为 {@link String}。 */
    public static final Function<Object[], String> SINGLE_PROJECTION_TO_STRING = projection -> {
        assert projection.length == 1;
        return String.valueOf(projection[0]);
    };

    /** 将双元素投影数组转换为 {@link Map.Entry}{@code <String, Long>}（键为第一个投影，值为第二个）。 */
    public static final Function<Object[], Map.Entry<String, Long>> PROJECTION_TO_STRING_LONG_ENTRY = projection -> {
        assert projection.length == 2;
        return Map.entry((String) projection[0], (long) projection[1]);
    };

    private QueryHelper() {
    }

    /**
     * 从查询中获取至多一条结果并映射为 {@link Optional}。
     * <p>
     * 会修改 {@link Query} 状态：命中计数精度设为 1，最大结果数为 1。
     *
     * @param query   {@link Query} 实例
     * @param mapping 将查询结果（投影）映射为目标类型的函数
     * @param <T>     查询响应类型
     * @param <R>     映射后的结果类型
     * @return 包含映射结果的 {@link Optional}，无结果时为空
     */
    public static <T, R> Optional<R> fetchSingle(Query<T> query, Function<T, R> mapping) {
        query.hitCountAccuracy(1).maxResults(1);
        try (var iterator = query.iterator()) {
            return iterator.hasNext() ? Optional.ofNullable(mapping.apply(iterator.next())) : Optional.empty();
        }
    }

    /**
     * 以分批方式流式遍历查询的全部结果。
     * <p>
     * 预期结果集较大时推荐使用，避免单次请求下载过多数据；结果按需拉取。
     * <p>
     * 注意：忽略查询的起始偏移与最大结果限制，将返回全部匹配项。
     *
     * @param query     {@link Query} 实例
     * @param batchSize 每批远程请求拉取的结果数
     * @param mapping   将查询结果映射为流元素的函数
     * @param <T>       查询响应类型
     * @param <R>       流元素类型
     * @return 包含全部结果的 {@link Stream}
     */
    public static <T, R> Stream<R> streamAll(Query<T> query, int batchSize, Function<T, R> mapping) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new BatchingIterator<>(query, batchSize, mapping), 0), false);
    }

    /**
     * 执行查询并将全部结果收集为 {@link Collection}。
     * <p>
     * 相比 {@link Query#list()} 无需计算精确命中总数，索引查询性能更好。
     * 预期数据量很大时请改用 {@link #streamAll(Query, int, Function)}。
     *
     * @param query   {@link Query} 实例
     * @param mapping 将查询结果映射为集合元素的函数
     * @param <T>     查询响应类型
     * @param <R>     集合元素类型
     * @return 包含全部映射结果的 {@link Collection}
     */
    public static <T, R> Collection<R> toCollection(Query<T> query, Function<T, R> mapping) {
        try (var iterator = query.iterator()) {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
                    .map(mapping)
                    .collect(Collectors.toList());
        }
    }

    // TODO 待移除：Infinispan 15.1 起 API 已提供 publisher
    /** 按固定批次大小异步分页拉取远程查询结果的迭代器。 */
    private static class BatchingIterator<T, R> implements Iterator<R> {

        private final RemoteQuery<T> query;
        private final int batchSize;
        private final Function<T, R> mapping;
        private int currentOffset;
        private Iterator<T> currentResults;
        private CompletableFuture<QueryResult<T>> nextResults;
        private R next;
        private boolean completed;

        private BatchingIterator(Query<T> query, int batchSize, Function<T, R> mapping) {
            assert query instanceof RemoteQuery<T>;
            this.query = (RemoteQuery<T>) query.startOffset(0).hitCountAccuracy(batchSize).maxResults(batchSize);
            this.batchSize = batchSize;
            this.mapping = mapping;
            currentResults = Collections.emptyIterator();
            executeQueryAsync();
            fetchNext();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public R next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            var result = next;
            fetchNext();
            return result;
        }

        /** 异步提交当前偏移处的查询。 */
        private void executeQueryAsync() {
            nextResults = query.executeAsync().toCompletableFuture();
        }

        /** 从当前批次或下一批中预取下一个非 null 映射结果。 */
        private void fetchNext() {
            while (true) {
                while (currentResults.hasNext()) {
                    next = mapping.apply(currentResults.next());
                    if (next != null) {
                        return;
                    }
                }
                if (completed) {
                    next = null;
                    return;
                }
                useNextResultsAndRequestMore();
            }
        }

        /** 消费已完成的异步结果，并在仍有数据时发起下一批请求。 */
        private void useNextResultsAndRequestMore() {
            var rsp = nextResults.join();
            var resultList = rsp.list();
            if (resultList.isEmpty()) {
                completed = true;
                return;
            }
            currentResults = resultList.iterator();
            if (resultList.size() < batchSize) {
                completed = true;
                return;
            }
            currentOffset += resultList.size();
            if (rsp.count().exact() && currentOffset >= rsp.count().value()) {
                completed = true;
                return;
            }
            query.startOffset(currentOffset);
            executeQueryAsync();
        }
    }

}
