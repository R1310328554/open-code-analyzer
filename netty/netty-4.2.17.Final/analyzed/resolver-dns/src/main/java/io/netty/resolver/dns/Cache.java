/*
 * Copyright 2018 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static java.util.Collections.singletonList;

/**
 * 抽象 DNS 缓存：条目在 TTL 到期后由 {@link EventLoop} 定时任务自动移除。
 * <p>同一主机名下的多条记录共享一个过期定时器；任一记录 TTL 到期会清除该主机名的全部条目，
 * 以避免 A/AAAA 记录 TTL 不一致时返回不一致的地址族组合。</p>
 *
 * @param <E> 缓存值类型（如 {@link InetAddress}、CNAME 字符串等）
 */
abstract class Cache<E> {
    private static final AtomicReferenceFieldUpdater<Cache.Entries, ScheduledFuture> FUTURE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(Cache.Entries.class, ScheduledFuture.class, "expirationFuture");

    /** 已取消的占位定时任务，使 {@link Delayed#getDelay} 返回最小值以便比较。 */
    private static final ScheduledFuture<?> CANCELLED = new ScheduledFuture<Object>() {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            // 忽略 unit，始终返回最小值，确保已取消标记的 TTL 为最小。
            return Long.MIN_VALUE;
        }

        @Override
        public int compareTo(Delayed o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Object get() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    };

    // 所有 EventLoop 实现均支持最长约两年延迟，作为 TTL 上限是安全的。
    // 参见: https://github.com/netty/netty/commit/b47fb817991b42ec8808c7d26538f3f2464e1fa6
    static final int MAX_SUPPORTED_TTL_SECS = (int) TimeUnit.DAYS.toSeconds(365 * 2);

    /** 主机名到条目列表的并发映射。 */
    private final ConcurrentMap<String, Entries> resolveCache = new ConcurrentHashMap<>();

    /**
     * 清空整个缓存并取消所有挂起的过期任务。
     */
    final void clear() {
        while (!resolveCache.isEmpty()) {
            for (Iterator<Entry<String, Entries>> i = resolveCache.entrySet().iterator(); i.hasNext();) {
                Map.Entry<String, Entries> e = i.next();
                i.remove();

                e.getValue().clearAndCancel();
            }
        }
    }

    /**
     * 清除指定主机名的全部条目；若存在条目则返回 {@code true}。
     */
    final boolean clear(String hostname) {
        Entries entries = resolveCache.remove(hostname);
        return entries != null && entries.clearAndCancel();
    }

    /**
     * 返回给定主机名的全部缓存条目，无缓存时返回 {@code null}。
     */
    final List<? extends E> get(String hostname) {
        Entries entries = resolveCache.get(hostname);
        return entries == null ? null : entries.get();
    }

    /**
     * 为给定主机名写入条目，并在 TTL 秒后由 {@link EventLoop} 调度过期。
     */
    final void cache(String hostname, E value, int ttl, EventLoop loop) {
        Entries entries = resolveCache.get(hostname);
        if (entries == null) {
            entries = new Entries(hostname);
            Entries oldEntries = resolveCache.putIfAbsent(hostname, entries);
            if (oldEntries != null) {
                entries = oldEntries;
            }
        }
        entries.add(value, ttl, loop);
    }

    /**
     * 返回当前已缓存至少一条记录的主机名数量。
     */
    final int size() {
        return resolveCache.size();
    }

    /**
     * 若返回 {@code true}，新条目应替换该主机名下所有已有条目（如负缓存）。
     */
    protected abstract boolean shouldReplaceAll(E entry);

    /**
     * 在写入缓存前对 {@code hostname} 对应的条目列表排序（子类可覆盖）。
     */
    protected void sortEntries(
            @SuppressWarnings("unused") String hostname, @SuppressWarnings("unused") List<E> entries) {
        // 默认不排序。
    }

    /**
     * 判断两条缓存条目是否表示同一逻辑值（用于去重与更新）。
     */
    protected abstract boolean equals(E entry, E otherEntry);

    /**
     * 单个主机名下的条目集合，继承 {@link AtomicReference} 以支持无锁 CAS 更新列表。
     */
    private final class Entries extends AtomicReference<List<E>> implements Runnable {

        /** 所属主机名，过期回调时用于从 map 中移除。 */
        private final String hostname;
        // 需为包级可见，以便 AtomicReferenceFieldUpdater 访问
        volatile ScheduledFuture<?> expirationFuture;

        Entries(String hostname) {
            super(Collections.<E>emptyList());
            this.hostname = hostname;
        }

        void add(E e, int ttl, EventLoop loop) {
            if (!shouldReplaceAll(e)) {
                for (;;) {
                    List<E> entries = get();
                    if (!entries.isEmpty()) {
                        final E firstEntry = entries.get(0);
                        if (shouldReplaceAll(firstEntry)) {
                            assert entries.size() == 1;

                            if (compareAndSet(entries, singletonList(e))) {
                                scheduleCacheExpirationIfNeeded(ttl, loop);
                                return;
                            } else {
                                // CAS 失败，重试。
                                continue;
                            }
                        }

                        // 写时复制，保证并发读者看到一致快照。
                        List<E> newEntries = new ArrayList<E>(entries.size() + 1);
                        int i = 0;
                        E replacedEntry = null;
                        do {
                            E entry = entries.get(i);
                            // 与待添加条目逻辑相等则替换该条，保留其余条目顺序。
                            if (!Cache.this.equals(e, entry)) {
                                newEntries.add(entry);
                            } else {
                                replacedEntry = entry;
                                newEntries.add(e);

                                ++i;
                                for (; i < entries.size(); ++i) {
                                    newEntries.add(entries.get(i));
                                }
                                break;
                            }
                        } while (++i < entries.size());
                        if (replacedEntry == null) {
                            newEntries.add(e);
                        }
                        sortEntries(hostname, newEntries);

                        if (compareAndSet(entries, Collections.unmodifiableList(newEntries))) {
                            scheduleCacheExpirationIfNeeded(ttl, loop);
                            return;
                        }
                    } else if (compareAndSet(entries, singletonList(e))) {
                        scheduleCacheExpirationIfNeeded(ttl, loop);
                        return;
                    }
                }
            } else {
                set(singletonList(e));
                scheduleCacheExpirationIfNeeded(ttl, loop);
            }
        }

        private void scheduleCacheExpirationIfNeeded(int ttl, EventLoop loop) {
            for (;;) {
                // 高并发下 CAS 重试时不重新计算 TTL；当前以秒为单位调度，影响通常可忽略。
                ScheduledFuture<?> oldFuture = FUTURE_UPDATER.get(this);
                if (oldFuture == null || oldFuture.getDelay(TimeUnit.SECONDS) > ttl) {
                    ScheduledFuture<?> newFuture = loop.schedule(this, ttl, TimeUnit.SECONDS);
                    // 可能出现：(1) 定时任务在本行与 CAS 之间触发；(2) 并发设置多个定时器。
                    // (1) CAS 失败后会看到 CANCELLED，TTL 不会更短，循环会退出。
                    // (2) 为避免 synchronized 争用，允许短暂存在多个定时器，旧任务会被 cancel。
                    if (FUTURE_UPDATER.compareAndSet(this, oldFuture, newFuture)) {
                        if (oldFuture != null) {
                            oldFuture.cancel(true);
                        }
                        break;
                    } else {
                        // 期间已有其他调度，取消本次新建并重试。
                        newFuture.cancel(true);
                    }
                } else {
                    break;
                }
            }
        }

        boolean clearAndCancel() {
            List<E> entries = getAndSet(Collections.<E>emptyList());
            if (entries.isEmpty()) {
                return false;
            }

            ScheduledFuture<?> expirationFuture = FUTURE_UPDATER.getAndSet(this, CANCELLED);
            if (expirationFuture != null) {
                expirationFuture.cancel(false);
            }

            return true;
        }

        @Override
        public void run() {
            // 任一记录 TTL 到期即移除该主机名的全部条目。虽非最细粒度失效策略，
            // 但可保证解析器在偏好某一地址族时，不会因 A/AAAA TTL 不同返回意外组合。
            //
            // TTL 只是“最长可缓存时间”的提示，提前整组清除完全合规。
            //
            // 参见 https://github.com/netty/netty/issues/7329
            resolveCache.remove(hostname, this);

            clearAndCancel();
        }
    }
}
