/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.nacos.api.selector.context;

import com.alibaba.nacos.api.selector.Selector;

/**
 * 为 {@link Selector#select(Object)} 构建上下文的工厂接口。
 *
 * <p>{@link #build(Object, Object)} 根据消费者与提供者组装上下文；{@link #getContextType()} 声明所构建上下文的类型标识。</p>
 *
 * @author chenglu
 * @date 2021-07-09 21:34
 */
public interface SelectorContextBuilder<T, C, P> {
    
    /**
     * 构建供 {@link Selector#select(Object)} 使用的上下文。
     *
     * <p>调用方需提供消费者与提供者；默认实现可返回 {@link CmdbContext}，以便基于 {@link com.alibaba.nacos.api.naming.pojo.Instance} 的 CMDB 元数据筛选。</p>
     *
     * @param consumer 发起选择的消费者
     * @param provider 待筛选的提供者集合
     * @return 选择器上下文
     */
    T build(C consumer, P provider);
    
    /**
     * 返回本构建器产出的上下文类型标识。
     *
     * <p>默认实现对应 CMDB 上下文类型常量。</p>
     *
     * @return 上下文类型名
     */
    String getContextType();
}
