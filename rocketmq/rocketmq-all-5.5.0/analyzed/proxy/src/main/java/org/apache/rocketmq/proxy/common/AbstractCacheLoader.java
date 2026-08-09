/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.proxy.common;

import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Nonnull;

/**
 * Guava CacheLoader 抽象基类：异步 reload 并在失败时保留旧值。
 */
public abstract class AbstractCacheLoader<K, V> extends CacheLoader<K, V> {
    /** 执行后台 refresh/reload 的线程池。 */
    private final ThreadPoolExecutor cacheRefreshExecutor;

    /** 指定缓存刷新线程池。 */
    public AbstractCacheLoader(ThreadPoolExecutor cacheRefreshExecutor) {
        this.cacheRefreshExecutor = cacheRefreshExecutor;
    }

    /** 异步 reload：失败时调用 onErr 并返回 oldValue。 */
    @Override
    public ListenableFuture<V> reload(@Nonnull K key, @Nonnull V oldValue) throws Exception {
        ListenableFutureTask<V> task = ListenableFutureTask.create(() -> {
            try {
                return getDirectly(key);
            } catch (Exception e) {
                onErr(key, e);
                return oldValue;
            }
        });
        cacheRefreshExecutor.execute(task);
        return task;
    }

    /** 同步 load，直接调用 getDirectly。 */
    @Override
    public V load(@Nonnull K key) throws Exception {
        return getDirectly(key);
    }

    /** 子类实现：从数据源加载缓存值。 */
    protected abstract V getDirectly(K key) throws Exception;

    /** 子类实现：reload 失败时的错误处理。 */
    protected abstract void onErr(K key, Exception e);
}