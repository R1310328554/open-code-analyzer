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

package com.alibaba.nacos.common.utils;

/**
 * 不可变二元组：封装 first/second 两个元素，通过 {@link #with(Object, Object)} 工厂方法创建。
 * Pair.
 *
 * @author nacos
 */
public class Pair<A, B> {
    
    private final A first;
    
    private final B second;
    
    /** 包内可见构造器，请使用 {@link #with(Object, Object)} */
    Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
    
    /** 创建包含两个元素的 Pair 实例 */
    public static <A, B> Pair<A, B> with(A first, B second) {
        return new Pair<>(first, second);
    }
    
    /** @return 第一个元素 */
    public A getFirst() {
        return first;
    }
    
    /** @return 第二个元素 */
    public B getSecond() {
        return second;
    }
}
