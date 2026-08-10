/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.naming.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通用轮询器实现。
 *
 * <p>基于原子递增索引对列表循环取模，提供简单的 round-robin 选取。</p>
 *
 * @author nkorange
 */
public class GenericPoller<T> implements Poller<T> {
    
    /** 轮询游标（线程安全递增）。 */
    private final AtomicInteger index = new AtomicInteger(0);
    
    /** 候选项列表。 */
    private List<T> items = new ArrayList<>();
    
    /** 使用指定列表创建轮询器。 */
    public GenericPoller(List<T> items) {
        this.items = items;
    }
    
    /** 返回下一项（索引对 size 取模）。 */
    @Override
    public T next() {
        return items.get(Math.abs(index.getAndIncrement() % items.size()));
    }
    
    /** 用新列表创建轮询器实例（不保留旧索引）。 */
    @Override
    public Poller<T> refresh(List<T> items) {
        return new GenericPoller<>(items);
    }
}
