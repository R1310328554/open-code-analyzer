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
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * <p>Sentinel 记录资源统计并执行规则检查的基础 API。</p>
 * <p>
 * 概念上，需要保护的物理或逻辑资源应被 Entry 包裹。若满足任一条件
 *（例如任一 {@link Rule} 阈值被超过），对该资源的请求将被阻断，并抛出
 * {@link BlockException}。
 * </p>
 * <p>
 * 可通过 <code>XxxRuleManager.loadRules()</code> 加载规则以配置条件。
 * </p>
 *
 * <p>
 * 示例如下，{@code "abc"} 表示受保护资源的唯一名称：
 * </p>
 *
 * <pre>
 *  public void foo() {
 *     Entry entry = null;
 *     try {
 *        entry = SphU.entry("abc");
 *        // 需要保护的资源
 *     } catch (BlockException blockException) {
 *         // 进入此处表示被阻断
 *         // 在此添加阻断处理逻辑
 *     } catch (Throwable bizException) {
 *         // 业务异常
 *         Tracer.trace(bizException);
 *     } finally {
 *         // 确保 finally 被执行
 *         if (entry != null){
 *             entry.exit();
 *         }
 *     }
 *  }
 * </pre>
 *
 * <p>
 * 确保 {@code SphU.entry()} 与 {@link Entry#exit()} 在同一线程中配对调用，
 * 否则将抛出 {@link ErrorEntryFreeException}。
 * </p>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @see SphO
 */
public class SphU {

    private static final Object[] OBJECTS0 = new Object[0];

    private SphU() {}

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name 受保护资源的唯一名称
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name) throws BlockException {
        return Env.sph.entry(name, EntryType.OUT, 1, OBJECTS0);
    }

    /**
     * 检查受保护方法相关的全部 {@link Rule}。
     *
     * @param method 受保护的方法
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method) throws BlockException {
        return Env.sph.entry(method, EntryType.OUT, 1, OBJECTS0);
    }

    /**
     * 检查受保护方法相关的全部 {@link Rule}。
     *
     * @param method     受保护的方法
     * @param batchCount 单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method, int batchCount) throws BlockException {
        return Env.sph.entry(method, EntryType.OUT, batchCount, OBJECTS0);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name       资源的唯一字符串标识
     * @param batchCount 单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name, int batchCount) throws BlockException {
        return Env.sph.entry(name, EntryType.OUT, batchCount, OBJECTS0);
    }

    /**
     * 检查受保护方法相关的全部 {@link Rule}。
     *
     * @param method      受保护的方法
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method, EntryType trafficType) throws BlockException {
        return Env.sph.entry(method, trafficType, 1, OBJECTS0);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name, EntryType trafficType) throws BlockException {
        return Env.sph.entry(name, trafficType, 1, OBJECTS0);
    }

    /**
     * 检查受保护方法相关的全部 {@link Rule}。
     *
     * @param method      受保护的方法
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method, EntryType trafficType, int batchCount) throws BlockException {
        return Env.sph.entry(method, trafficType, batchCount, OBJECTS0);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name, EntryType trafficType, int batchCount) throws BlockException {
        return Env.sph.entry(name, trafficType, batchCount, OBJECTS0);
    }

    /**
     * 检查受保护方法相关的全部 {@link Rule}。
     *
     * @param method      受保护的方法
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args        用于热点参数流控或自定义 Slot 的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method, EntryType trafficType, int batchCount, Object... args)
        throws BlockException {
        return Env.sph.entry(method, trafficType, batchCount, args);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args        用于热点参数流控的参数
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name, EntryType trafficType, int batchCount, Object... args)
        throws BlockException {
        return Env.sph.entry(name, trafficType, batchCount, args);
    }

    /**
     * 对表示异步调用的资源记录统计并检查全部规则。
     *
     * @param name 受保护资源的唯一名称
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 0.2.0
     */
    public static AsyncEntry asyncEntry(String name) throws BlockException {
        return Env.sph.asyncEntry(name, EntryType.OUT, 1, OBJECTS0);
    }

    /**
     * 对表示异步调用的资源记录统计并检查全部规则。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 0.2.0
     */
    public static AsyncEntry asyncEntry(String name, EntryType trafficType) throws BlockException {
        return Env.sph.asyncEntry(name, trafficType, 1, OBJECTS0);
    }

    /**
     * 对表示异步调用的资源记录统计并检查全部规则。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args        用于热点参数流控的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 0.2.0
     */
    public static AsyncEntry asyncEntry(String name, EntryType trafficType, int batchCount, Object... args)
        throws BlockException {
        return Env.sph.asyncEntry(name, trafficType, batchCount, args);
    }

    /**
     * 对给定资源记录统计并执行规则检查，Entry 具有优先级。
     *
     * @param name 受保护资源的唯一名称
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 1.4.0
     */
    public static Entry entryWithPriority(String name) throws BlockException {
        return Env.sph.entryWithPriority(name, EntryType.OUT, 1, true);
    }

    /**
     * 对给定资源记录统计并执行规则检查，Entry 具有优先级。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 1.4.0
     */
    public static Entry entryWithPriority(String name, EntryType trafficType) throws BlockException {
        return Env.sph.entryWithPriority(name, trafficType, 1, true);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name         受保护资源的唯一名称
     * @param resourceType 资源分类（例如 Web 或 RPC）
     * @param trafficType  流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                     仅入站流量可被 {@link SystemRule} 阻断
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static Entry entry(String name, int resourceType, EntryType trafficType) throws BlockException {
        return Env.sph.entryWithType(name, resourceType, trafficType, 1, OBJECTS0);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name         受保护资源的唯一名称
     * @param trafficType  流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                     仅入站流量可被 {@link SystemRule} 阻断
     * @param resourceType 资源分类（例如 Web 或 RPC）
     * @param args         用于热点参数流控或自定义 Slot 的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static Entry entry(String name, int resourceType, EntryType trafficType, Object[] args)
        throws BlockException {
        return Env.sph.entryWithType(name, resourceType, trafficType, 1, args);
    }

    /**
     * Record statistics and perform rule checking for the given resource that indicates an async invocation.
     *
     * @param name         受保护资源的唯一名称
     * @param trafficType  流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                     仅入站流量可被 {@link SystemRule} 阻断
     * @param resourceType 资源分类（例如 Web 或 RPC）
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static AsyncEntry asyncEntry(String name, int resourceType, EntryType trafficType)
        throws BlockException {
        return Env.sph.asyncEntryWithType(name, resourceType, trafficType, 1, false, OBJECTS0);
    }

    /**
     * Record statistics and perform rule checking for the given resource that indicates an async invocation.
     *
     * @param name         受保护资源的唯一名称
     * @param trafficType  流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                     仅入站流量可被 {@link SystemRule} 阻断
     * @param resourceType 资源分类（例如 Web 或 RPC）
     * @param args         用于热点参数流控或自定义 Slot 的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static AsyncEntry asyncEntry(String name, int resourceType, EntryType trafficType, Object[] args)
        throws BlockException {
        return Env.sph.asyncEntryWithType(name, resourceType, trafficType, 1, false, args);
    }

    /**
     * Record statistics and perform rule checking for the given resource that indicates an async invocation.
     *
     * @param name         受保护资源的唯一名称
     * @param trafficType  流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                     仅入站流量可被 {@link SystemRule} 阻断
     * @param resourceType 资源分类（例如 Web 或 RPC）
     * @param batchCount   单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args         用于热点参数流控或自定义 Slot 的参数
     * @return 本次调用的 {@link Entry}（用于标记调用完成并获取上下文数据）
     * @throws BlockException 若满足阻断条件 (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static AsyncEntry asyncEntry(String name, int resourceType, EntryType trafficType, int batchCount,
                                        Object[] args) throws BlockException {
        return Env.sph.asyncEntryWithType(name, resourceType, trafficType, batchCount, false, args);
    }
}
