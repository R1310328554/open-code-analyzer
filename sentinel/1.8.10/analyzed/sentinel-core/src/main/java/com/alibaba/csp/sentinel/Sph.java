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
package com.alibaba.csp.sentinel;

import java.lang.reflect.Method;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * 对资源记录统计并执行规则检查的基础接口。
 *
 * @author qinan.qn
 * @author jialiang.linjl
 * @author leyou
 * @author Eric Zhao
 */
public interface Sph extends SphResourceTypeSupport {

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name 受保护资源的唯一名称
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）.
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(String name) throws BlockException;

    /**
     * 对给定方法记录统计并执行规则检查。
     *
     * @param method 受保护的方法
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）.
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(Method method) throws BlockException;

    /**
     * 对给定方法记录统计并执行规则检查。
     *
     * @param method     受保护的方法
     * @param batchCount 单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）.
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(Method method, int batchCount) throws BlockException;

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name       资源的唯一字符串标识
     * @param batchCount 单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）.
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(String name, int batchCount) throws BlockException;

    /**
     * 对给定方法记录统计并执行规则检查。
     *
     * @param method      受保护的方法
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）.
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(Method method, EntryType trafficType) throws BlockException;

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）.
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(String name, EntryType trafficType) throws BlockException;

    /**
     * 对给定方法记录统计并执行规则检查。
     *
     * @param method      受保护的方法
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）.
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(Method method, EntryType trafficType, int batchCount) throws BlockException;

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）.
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(String name, EntryType trafficType, int batchCount) throws BlockException;

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param method      受保护的方法
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args        parameters of the method for flow control or customized slots
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）.
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(Method method, EntryType trafficType, int batchCount, Object... args) throws BlockException;

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args        用于热点参数流控或自定义 Slot 的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件
     */
    Entry entry(String name, EntryType trafficType, int batchCount, Object... args) throws BlockException;

    /**
     * 创建受保护的异步资源。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args        用于热点参数流控或自定义 Slot 的参数
     * @return 创建的异步 Entry
     * @throws BlockException 若满足阻断条件
     * @since 0.2.0
     */
    AsyncEntry asyncEntry(String name, EntryType trafficType, int batchCount, Object... args) throws BlockException;

    /**
     * 创建带优先级的受保护资源。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param prioritized 该 Entry 是否具有优先级
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件
     * @since 1.4.0
     */
    Entry entryWithPriority(String name, EntryType trafficType, int batchCount, boolean prioritized)
        throws BlockException;

    /**
     * 创建带优先级的受保护资源。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param prioritized 该 Entry 是否具有优先级
     * @param args        用于热点参数流控或自定义 Slot 的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件
     * @since 1.5.0
     */
    Entry entryWithPriority(String name, EntryType trafficType, int batchCount, boolean prioritized, Object... args)
        throws BlockException;
}
