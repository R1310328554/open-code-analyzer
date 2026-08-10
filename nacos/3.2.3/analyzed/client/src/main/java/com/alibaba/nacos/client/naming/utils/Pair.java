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

/**
 * 带权重的二元组。
 *
 * <p>用于 {@link Chooser} 维护候选项及其负载权重。</p>
 *
 * @author nkorange
 */
public class Pair<T> {
    
    /** 候选项。 */
    private final T item;
    
    /** 权重值（相对值，由 Chooser 归一化）。 */
    private final double weight;
    
    /** 构造带权二元组。 */
    public Pair(T item, double weight) {
        this.item = item;
        this.weight = weight;
    }
    
    /** 返回候选项。 */
    public T item() {
        return item;
    }
    
    /** 返回权重。 */
    public double weight() {
        return weight;
    }
}
