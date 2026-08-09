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
package org.redisson.jcache.bean;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.cache.management.CacheStatisticsMXBean;

/**
 * JCache 缓存统计 MXBean 实现，实现 {@link CacheStatisticsMXBean} 接口。
 * <p>
 * 使用 {@link AtomicLong} 线程安全地累计命中、未命中、写入、移除、驱逐次数
 * 及 get/put/remove 操作的累计耗时（纳秒），供 JMX 与管理接口查询。
 *
 * @author Nikita Koksharov
 *
 */
public class JCacheStatisticsMXBean implements CacheStatisticsMXBean {

    /** 缓存移除（remove）操作累计次数。 */
    private final AtomicLong removals = new AtomicLong();
    /** 缓存命中（get 成功）累计次数。 */
    private final AtomicLong hits = new AtomicLong();
    /** 缓存写入（put）累计次数。 */
    private final AtomicLong puts = new AtomicLong();
    /** 缓存未命中（get 失败）累计次数。 */
    private final AtomicLong misses = new AtomicLong();
    /** 缓存驱逐（eviction）累计次数。 */
    private final AtomicLong evictions = new AtomicLong();
    
    /** remove 操作累计耗时（纳秒）。 */
    private final AtomicLong removeTime = new AtomicLong();
    /** get 操作累计耗时（纳秒）。 */
    private final AtomicLong getTime = new AtomicLong();
    /** put 操作累计耗时（纳秒）。 */
    private final AtomicLong putTime = new AtomicLong();
    
    
    /** 重置所有计数器与耗时统计为零。 */
    @Override
    public void clear() {
        removals.set(0);
        hits.set(0);
        puts.set(0);
        misses.set(0);
        evictions.set(0);
        
        removeTime.set(0);
        getTime.set(0);
        putTime.set(0);
    }

    /** 累加命中次数。 */
    public void addHits(long value) {
        hits.addAndGet(value);
    }
    
    @Override
    public long getCacheHits() {
        return hits.get();
    }

    /** 返回命中率百分比：命中数 / (命中 + 未命中) × 100。 */
    @Override
    public float getCacheHitPercentage() {
        long gets = getCacheGets();
        if (gets == 0) {
            return 0;
        }
        return (getCacheHits() * 100) / (float) gets;
    }

    /** 累加未命中次数。 */
    public void addMisses(long value) {
        misses.addAndGet(value);
    }
    
    @Override
    public long getCacheMisses() {
        return misses.get();
    }

    /** 返回未命中率百分比。 */
    @Override
    public float getCacheMissPercentage() {
        long gets = getCacheGets();
        if (gets == 0) {
            return 0;
        }
        return (getCacheMisses() * 100) / (float) gets;
    }

    /** 返回 get 操作总次数（命中 + 未命中）。 */
    @Override
    public long getCacheGets() {
        return hits.get() + misses.get();
    }

    /** 累加 put 次数。 */
    public void addPuts(long value) {
        puts.addAndGet(value);
    }
    
    @Override
    public long getCachePuts() {
        return puts.get();
    }
    
    /** 累加 remove 次数。 */
    public void addRemovals(long value) {
        removals.addAndGet(value);
    }

    @Override
    public long getCacheRemovals() {
        return removals.get();
    }

    /** 累加驱逐次数。 */
    public void addEvictions(long value) {
        evictions.addAndGet(value);
    }
    
    @Override
    public long getCacheEvictions() {
        return evictions.get();
    }

    /** 计算单次操作平均耗时（微秒），value 为 0 或 timeInNanos 为 0 时返回 0。 */
    private float get(long value, long timeInNanos) {
        if (value == 0 || timeInNanos == 0) {
            return 0;
        }
        long timeInMicrosec = TimeUnit.NANOSECONDS.toMicros(timeInNanos);
        return timeInMicrosec / value;
    }
    
    /** 累加 get 操作耗时（纳秒）。 */
    public void addGetTime(long value) {
        getTime.addAndGet(value);
    }
    
    /** 返回 get 操作平均耗时（微秒）。 */
    @Override
    public float getAverageGetTime() {
        return get(getCacheGets(), getTime.get());
    }

    /** 累加 put 操作耗时（纳秒）。 */
    public void addPutTime(long value) {
        putTime.addAndGet(value);
    }
    
    /** 返回 put 操作平均耗时（微秒）。 */
    @Override
    public float getAveragePutTime() {
        return get(getCachePuts(), putTime.get());
    }
    
    /** 累加 remove 操作耗时（纳秒）。 */
    public void addRemoveTime(long value) {
        removeTime.addAndGet(value);
    }

    /** 返回 remove 操作平均耗时（微秒）。 */
    @Override
    public float getAverageRemoveTime() {
        return get(getCachePuts(), removeTime.get());
    }

}
