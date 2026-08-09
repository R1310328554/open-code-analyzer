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
package org.redisson.mybatis;

import org.apache.ibatis.cache.Cache;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 基于 Redisson {@link RMapCache} 的 MyBatis 二级缓存实现。
 * <p>通过 {@link #setRedissonConfig(String)} 从 classpath YAML 加载 Redisson 配置；
 * 支持写入 TTL、访问 max-idle 与 LRU 容量上限。
 *
 * @author Nikita Koksharov
 */
public class RedissonCache implements Cache {

    private String id;
    private RMapCache<Object, Object> mapCache;
    private long timeToLive;
    private long maxIdleTime;
    private int maxSize;

    /** @param id MyBatis 缓存命名空间 ID，同时作为 Redis Map 名称 */
    public RedissonCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    /** 写入缓存条目；MapCache 模式下附带 TTL 与 max-idle。 */
    @Override
    public void putObject(Object o, Object o1) {
        check();
        mapCache.fastPut(o, o1, timeToLive, TimeUnit.MILLISECONDS, maxIdleTime, TimeUnit.MILLISECONDS);
    }

    /** 读取缓存；无 max-idle/容量限制时使用 {@link RMapCache#getWithTTLOnly}。 */
    @Override
    public Object getObject(Object o) {
        check();
        if (maxIdleTime == 0 && maxSize == 0) {
            return mapCache.getWithTTLOnly(o);
        }

        return mapCache.get(o);
    }

    /** 移除单个键并返回先前值。 */
    @Override
    public Object removeObject(Object o) {
        check();
        return mapCache.remove(o);
    }

    /** 清空整个 MapCache。 */
    @Override
    public void clear() {
        check();
        mapCache.clear();
    }

    /** 返回当前缓存条目数。 */
    @Override
    public int getSize() {
        check();
        return mapCache.size();
    }

    /** 设置写入后 TTL（毫秒）。 */
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /** 设置访问后 max-idle（毫秒）。 */
    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    /** 设置 LRU 容量上限；{@code > 0} 时在 {@link #setRedissonConfig} 中生效。 */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /** MyBatis 并发锁；Redisson 实现返回 {@code null}（依赖 Redis 原子操作）。 */
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    /** 从 classpath 加载 Redisson YAML 并初始化 {@link RMapCache}。
     *  @param config 配置文件路径（如 {@code redisson.yaml}）
     */
    public void setRedissonConfig(String config) {
        Config cfg;
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(config);
            cfg = Config.fromYAML(is);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't parse config", e);
        }

        RedissonClient redisson = Redisson.create(cfg);
        mapCache = getMapCache(id, redisson);
        if (maxSize > 0) {
            mapCache.setMaxSize(maxSize);
        }
    }

    /** 子类可覆盖以使用 Native Map 等变体；默认 {@link RedissonClient#getMapCache}。 */
    protected RMapCache<Object, Object> getMapCache(String id, RedissonClient redisson) {
        return redisson.getMapCache(id);
    }

    /** 确保已通过 {@link #setRedissonConfig} 初始化 MapCache。 */
    private void check() {
        if (mapCache == null) {
            throw new IllegalStateException("Redisson config is not defined");
        }
    }

}
