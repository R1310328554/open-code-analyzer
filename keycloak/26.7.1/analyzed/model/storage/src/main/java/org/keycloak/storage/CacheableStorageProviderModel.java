/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.storage;

import java.util.Calendar;

import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.PrioritizedComponentModel;
import org.keycloak.models.cache.CachedObject;

/**
 * 可缓存的存储 Provider 配置模型：缓存策略、生命周期与启用状态。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CacheableStorageProviderModel extends PrioritizedComponentModel {
    /** 缓存策略配置键。 */
    public static final String CACHE_POLICY = "cachePolicy";
    /** 最大存活时间配置键。 */
    public static final String MAX_LIFESPAN = "maxLifespan";
    /** 每日驱逐时刻（小时）配置键。 */
    public static final String EVICTION_HOUR = "evictionHour";
    /** 每日驱逐时刻（分钟）配置键。 */
    public static final String EVICTION_MINUTE = "evictionMinute";
    /** 每周驱逐日配置键（1–7）。 */
    public static final String EVICTION_DAY = "evictionDay";
    /** 在此时间戳之前写入的缓存一律失效。 */
    public static final String CACHE_INVALID_BEFORE = "cacheInvalidBefore";
    /** Provider 是否启用。 */
    public static final String ENABLED = "enabled";

    /** 缓存策略（懒解析）。 */
    private transient CachePolicy cachePolicy;
    /** 最大存活时间（毫秒）。 */
    private transient long maxLifespan = -1;
    /** 驱逐小时（0–23）。 */
    private transient int evictionHour = -1;
    /** 驱逐分钟（0–59）。 */
    private transient int evictionMinute = -1;
    /** 驱逐星期几（1–7）。 */
    private transient int evictionDay = -1;
    /** 强制失效时间边界。 */
    private transient long cacheInvalidBefore = -1;
    /** 是否启用（懒解析）。 */
    private transient Boolean enabled;

    /** 默认构造。 */
    public CacheableStorageProviderModel() {
    }

    /** 从已有 ComponentModel 复制构造。 */
    public CacheableStorageProviderModel(ComponentModel copy) {
        super(copy);
    }

    /** 获取缓存策略枚举值。 */
    public CachePolicy getCachePolicy() {
        if (cachePolicy == null) {
            String str = getConfig().getFirst(CACHE_POLICY);
            if (str == null) return null;
            cachePolicy = CachePolicy.valueOf(str);
        }
        return cachePolicy;
    }

    /** 设置缓存策略。 */
    public void setCachePolicy(CachePolicy cachePolicy) {
        this.cachePolicy = cachePolicy;
        if (cachePolicy == null) {
            getConfig().remove(CACHE_POLICY);

        } else {
            getConfig().putSingle(CACHE_POLICY, cachePolicy.name());
        }
    }

    /** 获取最大存活时间（毫秒）；未配置时 -1。 */
    public long getMaxLifespan() {
        if (maxLifespan < 0) {
            String str = getConfig().getFirst(MAX_LIFESPAN);
            if (str == null) return -1;
            maxLifespan = Long.valueOf(str);
        }
        return maxLifespan;
    }

    /** 设置最大存活时间（毫秒）。 */
    public void setMaxLifespan(long maxLifespan) {
        this.maxLifespan = maxLifespan;
        getConfig().putSingle(MAX_LIFESPAN, Long.toString(maxLifespan));
    }

    /** 获取每日驱逐小时（0–23）。 */
    public int getEvictionHour() {
        if (evictionHour < 0) {
            String str = getConfig().getFirst(EVICTION_HOUR);
            if (str == null) return -1;
            evictionHour = Integer.valueOf(str);
        }
        return evictionHour;
    }

    /** 设置每日驱逐小时（0–23）。 */
    public void setEvictionHour(int evictionHour) {
        if (evictionHour > 23 || evictionHour < 0) throw new IllegalArgumentException("Must be between 0 and 23");
        this.evictionHour = evictionHour;
        getConfig().putSingle(EVICTION_HOUR, Integer.toString(evictionHour));
    }

    /** 获取每日驱逐分钟（0–59）。 */
    public int getEvictionMinute() {
        if (evictionMinute < 0) {
            String str = getConfig().getFirst(EVICTION_MINUTE);
            if (str == null) return -1;
            evictionMinute = Integer.valueOf(str);
        }
        return evictionMinute;
    }

    /** 设置每日驱逐分钟（0–59）。 */
    public void setEvictionMinute(int evictionMinute) {
        if (evictionMinute > 59 || evictionMinute < 0) throw new IllegalArgumentException("Must be between 0 and 59");
        this.evictionMinute = evictionMinute;
        getConfig().putSingle(EVICTION_MINUTE, Integer.toString(evictionMinute));
    }

    /** 获取每周驱逐日（1=周日 … 7=周六）。 */
    public int getEvictionDay() {
        if (evictionDay < 0) {
            String str = getConfig().getFirst(EVICTION_DAY);
            if (str == null) return -1;
            evictionDay = Integer.valueOf(str);
        }
        return evictionDay;
    }

    /** 设置每周驱逐日（1–7）。 */
    public void setEvictionDay(int evictionDay) {
        if (evictionDay > 7 || evictionDay < 1) throw new IllegalArgumentException("Must be between 1 and 7");
        this.evictionDay = evictionDay;
        getConfig().putSingle(EVICTION_DAY, Integer.toString(evictionDay));
    }

    /** 获取强制失效时间边界（毫秒时间戳）。 */
    public long getCacheInvalidBefore() {
        if (cacheInvalidBefore < 0) {
            String str = getConfig().getFirst(CACHE_INVALID_BEFORE);
            if (str == null) return -1;
            cacheInvalidBefore = Long.valueOf(str);
        }
        return cacheInvalidBefore;
    }

    /** 设置强制失效时间边界。 */
    public void setCacheInvalidBefore(long cacheInvalidBefore) {
        this.cacheInvalidBefore = cacheInvalidBefore;
        getConfig().putSingle(CACHE_INVALID_BEFORE, Long.toString(cacheInvalidBefore));
    }

    /** 设置 Provider 是否启用。 */
    public void setEnabled(boolean flag) {
        enabled = flag;
        getConfig().putSingle(ENABLED, Boolean.toString(flag));
    }

    /** 返回 Provider 是否启用；未配置时默认 true。 */
    public boolean isEnabled() {
        if (enabled == null) {
            String val = getConfig().getFirst(ENABLED);
            if (val == null) {
                enabled = true;
            } else {
                enabled = Boolean.valueOf(val);
            }
        }
        return enabled;

    }

    /** 根据当前缓存策略计算剩余存活时间（毫秒）；无限制时 -1。 */
    public long getLifespan() {
        CachePolicy policy = getCachePolicy();
        long lifespan = -1;
        if (policy == null || policy == CachePolicy.DEFAULT) {
            lifespan = -1;
        } else if (policy == CacheableStorageProviderModel.CachePolicy.EVICT_DAILY) {
            if (getEvictionHour() > -1 && getEvictionMinute() > -1) {
                lifespan = dailyTimeout(getEvictionHour(), getEvictionMinute()) - Time.currentTimeMillis();
            }
        } else if (policy == CacheableStorageProviderModel.CachePolicy.EVICT_WEEKLY) {
            if (getEvictionDay() > 0 && getEvictionHour() > -1 && getEvictionMinute() > -1) {
                lifespan = weeklyTimeout(getEvictionDay(), getEvictionHour(), getEvictionMinute()) - Time.currentTimeMillis();
            }
        } else if (policy == CacheableStorageProviderModel.CachePolicy.MAX_LIFESPAN) {
            lifespan = getMaxLifespan();
        }
        return lifespan;
    }

    /** 判断给定缓存对象是否应被失效（策略、禁用或超时）。 */
    public boolean shouldInvalidate(CachedObject cached) {
        boolean invalidate = false;
        if (!isEnabled()) {
            invalidate = true;
        } else {
            CacheableStorageProviderModel.CachePolicy policy = getCachePolicy();
            if (policy != null) {
                //String currentTime = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(new Date(Time.currentTimeMillis()));
                if (policy == CacheableStorageProviderModel.CachePolicy.NO_CACHE) {
                    invalidate = true;
                } else if (cached.getCacheTimestamp() < getCacheInvalidBefore()) {
                    invalidate = true;
                } else if (policy == CacheableStorageProviderModel.CachePolicy.MAX_LIFESPAN) {
                    if (cached.getCacheTimestamp() + getMaxLifespan() < Time.currentTimeMillis()) {
                        invalidate = true;
                    }
                } else if (policy == CacheableStorageProviderModel.CachePolicy.EVICT_DAILY) {
                    long dailyBoundary = dailyEvictionBoundary(getEvictionHour(), getEvictionMinute());
                    if (cached.getCacheTimestamp() <= dailyBoundary) {
                        invalidate = true;
                    }
                } else if (policy == CacheableStorageProviderModel.CachePolicy.EVICT_WEEKLY) {
                    int oneWeek = 7 * 24 * 60 * 60 * 1000;
                    long weeklyTimeout = weeklyTimeout(getEvictionDay(), getEvictionHour(), getEvictionMinute());
                    long lastTimeout = weeklyTimeout - oneWeek;
                    //String timeout = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(new Date(weeklyTimeout));
                    //String stamp = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(new Date(cached.getCacheTimestamp()));
                    if (cached.getCacheTimestamp() <= lastTimeout) {
                        invalidate = true;
                    }
                }
            }
        }
        return invalidate;
    }


    /** 计算下一次每日驱逐的超时时间戳（毫秒）。 */
    public static long dailyTimeout(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Time.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() < Time.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return cal.getTimeInMillis();
    }

    /** 返回最近一次已发生的每日驱逐边界时间戳。 */
    public static long dailyEvictionBoundary(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Time.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() > Time.currentTimeMillis()) {
            // 若今日驱逐尚未发生，边界设为昨日同一时刻
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return cal.getTimeInMillis();
    }

    /** 计算下一次每周驱逐的超时时间戳（毫秒）。 */
    public static long weeklyTimeout(int day, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Time.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.DAY_OF_WEEK, day);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() < Time.currentTimeMillis()) {
            int add = (7 * 24 * 60 * 60 * 1000);
            cal.add(Calendar.MILLISECOND, add);
        }

        return cal.getTimeInMillis();
    }



    /** 存储 Provider 缓存策略枚举。 */
    public enum CachePolicy {
        /** 不缓存。 */
        NO_CACHE,
        /** 使用默认策略。 */
        DEFAULT,
        /** 每日固定时刻驱逐。 */
        EVICT_DAILY,
        /** 每周固定时刻驱逐。 */
        EVICT_WEEKLY,
        /** 按最大存活时间驱逐。 */
        MAX_LIFESPAN
    }
}
