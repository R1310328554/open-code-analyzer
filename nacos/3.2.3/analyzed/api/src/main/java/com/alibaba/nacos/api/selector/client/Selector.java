/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.selector.client;

/**
 * 客户端侧简化版选择器接口。
 *
 * <p>仅包含 {@link #select(Object)}，不含解析阶段；适用于客户端本地路由场景。</p>
 *
 * @param <C> 选择器上下文类型
 * @param <E> 选择结果类型
 * @author lideyou
 */
public interface Selector<C, E> {
    
    /**
     * 根据上下文执行选择并返回结果。
     *
     * @param context 选择器上下文
     * @return 选择结果
     */
    E select(C context);
}
