/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.config.server.utils;

/**
 * 当前线程配置持久化上下文：通过 ThreadLocal 控制是否跳过历史记录写入，适用于数据迁移、批量导入等内部场景。
 * Config persistence context for current thread.
 *
 * <p>Used to control some persistence behaviors (e.g. whether to write history records)
 * for internal batch operations such as data migration or skill upload.</p>
 */
public final class ConfigPersistContext {
    
    /** 线程级“跳过历史写入”标志，默认 false */
    private static final ThreadLocal<Boolean> SKIP_HISTORY =
        ThreadLocal.withInitial(() -> Boolean.FALSE);
    
    private ConfigPersistContext() {
    }
    
    /**
     * 当前线程是否应跳过配置历史记录写入。
     * Whether current thread should skip writing config history.
     */
    public static boolean isSkipHistory() {
        Boolean v = SKIP_HISTORY.get();
        return v != null && v;
    }
    
    /**
     * 设置当前线程是否跳过历史写入；false 时清除上下文。
     * Set whether to skip history for current thread.
     *
     * <p>Callers should use {@link #withSkipHistory()} whenever possible to ensure cleanup.</p>
     */
    public static void setSkipHistory(boolean skipHistory) {
        if (skipHistory) {
            SKIP_HISTORY.set(Boolean.TRUE);
        } else {
            clear();
        }
    }
    
    /**
     * 清除当前线程的持久化上下文。
     * Clear thread local context.
     */
    public static void clear() {
        SKIP_HISTORY.remove();
    }
    
    /**
     * 以 try-with-resources 方式启用跳过历史写入，close 时自动恢复。
     * Enable skip-history in try-with-resources style.
     */
    public static Guard withSkipHistory() {
        return new Guard(true);
    }
    
    /**
     * 自动关闭守卫：close 时恢复进入前的 skipHistory 状态。
     * A guard which restores previous value when closed.
     */
    public static final class Guard implements AutoCloseable {
        
        /** 进入 Guard 前线程原有的 skipHistory 值 */
        private final Boolean previous;
        
        private Guard(boolean skipHistory) {
            this.previous = SKIP_HISTORY.get();
            setSkipHistory(skipHistory);
        }
        
        @Override
        public void close() {
            if (previous == null || !previous) {
                clear();
            } else {
                SKIP_HISTORY.set(previous);
            }
        }
    }
}
