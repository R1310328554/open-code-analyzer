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

package com.alibaba.nacos.api.selector;

import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * 通用选择器接口：解析条件并执行筛选。
 *
 * <p>{@link #parse(Object)} 接收 Nacos 下发的表达式并构建内部状态；{@link #select(Object)} 在给定上下文中执行筛选并返回目标结果；{@link #getType()} 与 {@link #getContextType()} 分别标识选择器类型及 {@link #select} 所需上下文类型。</p>
 *
 * <p>当前 Nacos 主要提供 {@link AbstractCmdbSelector} 供用户扩展 CMDB 筛选逻辑，其他类型待完善。</p>
 *
 * @author chenglu
 * @date 2021-07-09 21:24
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface Selector<R, C, E> extends Serializable {
    
    /**
     * 解析选择条件，构建供 {@link #select(Object)} 使用的内部状态。
     *
     * @param expression 表达式或条件字符串
     * @return 解析后的选择器自身（通常 {@code this}）
     * @throws NacosException 解析失败
     */
    Selector<R, C, E> parse(E expression) throws NacosException;
    
    /**
     * 在给定上下文中执行筛选。
     *
     * @param context 选择器上下文
     * @return 筛选结果
     */
    R select(C context);
    
    /**
     * 返回选择器类型标识。
     *
     * @return 类型名字符串
     */
    String getType();
    
    /**
     * 返回 {@link #select(Object)} 所需的上下文类型。
     *
     * @return 上下文类型名字符串
     */
    String getContextType();
}
