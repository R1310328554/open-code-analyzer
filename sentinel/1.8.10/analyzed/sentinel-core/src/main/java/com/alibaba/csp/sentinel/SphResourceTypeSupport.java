/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * 支持按资源类型分类的 Sph 扩展接口。
 *
 * @author Eric Zhao
 * @since 1.7.0
 */
public interface SphResourceTypeSupport {

    /**
     * 对给定分类的资源记录统计并执行规则检查。
     *
     * @param name         受保护资源的唯一名称
     * @param resourceType 资源分类
     * @param trafficType  流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                     仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount   单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args         用于热点参数流控或自定义 Slot 的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件
     */
    Entry entryWithType(String name, int resourceType, EntryType trafficType, int batchCount, Object[] args)
        throws BlockException;

    /**
     * 对给定分类的资源记录统计并执行规则检查。
     *
     * @param name         受保护资源的唯一名称
     * @param resourceType 资源分类（例如 Web 或 RPC）
     * @param trafficType  流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                     仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount   单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param prioritized  该 Entry 是否具有优先级
     * @param args         用于热点参数流控或自定义 Slot 的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件
     */
    Entry entryWithType(String name, int resourceType, EntryType trafficType, int batchCount, boolean prioritized,
                        Object[] args) throws BlockException;

    /**
     * 对表示异步调用的给定资源记录统计并执行规则检查。
     *
     * @param name         受保护资源的唯一名称
     * @param resourceType 资源分类（例如 Web 或 RPC）
     * @param trafficType  流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                     仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount   单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param prioritized  该 Entry 是否具有优先级
     * @param args         用于热点参数流控或自定义 Slot 的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件
     */
    AsyncEntry asyncEntryWithType(String name, int resourceType, EntryType trafficType, int batchCount,
                                  boolean prioritized,
                                  Object[] args) throws BlockException;
}
