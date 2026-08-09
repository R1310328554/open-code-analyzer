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
import java.util.List;

import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

/**
 * 概念上，需要保护的物理或逻辑资源应被 Entry 包裹。若满足任一条件
 *（例如任一 {@link Rule} 阈值被超过），对该资源的请求将被阻断。一旦被阻断，
 * {@link SphO}#entry() 将返回 false。
 *
 * <p>
 * 可通过 <code>XXXRuleManager.loadRules()</code> 配置条件，例如
 * {@link FlowRuleManager#loadRules(List)}、{@link DegradeRuleManager#loadRules(List)}、
 * {@link SystemRuleManager#loadRules(List)}。
 * </p>
 *
 * <p>
 * 示例如下，{@code "abc"} 表示受保护资源的唯一名称：
 * </p>
 *
 * <pre>
 * public void foo() {
 *    if (SphO.entry("abc")) {
 *        try {
 *            // 业务逻辑
 *        } finally {
 *            SphO.exit(); // 必须 exit()
 *        }
 *    } else {
 *        // 未能进入受保护资源
 *    }
 * }
 * </pre>
 *
 * 确保 {@code SphO.entry()} 与 {@link SphO#exit()} 在同一线程中配对调用，
 * 否则将抛出 {@link ErrorEntryFreeException}。
 *
 * @author jialiang.linjl
 * @author leyou
 * @author Eric Zhao
 * @see SphU
 */
public class SphO {

    private static final Object[] OBJECTS0 = new Object[0];

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name 受保护资源的唯一名称
     * @return 若无规则阈值被超过则返回 true，否则返回 false
     */
    public static boolean entry(String name) {
        return entry(name, EntryType.OUT, 1, OBJECTS0);
    }

    /**
     * 检查受保护方法相关的全部 {@link Rule}。
     *
     * @param method 受保护的方法
     * @return 若无规则阈值被超过则返回 true，否则返回 false
     */
    public static boolean entry(Method method) {
        return entry(method, EntryType.OUT, 1, OBJECTS0);
    }

    /**
     * 检查受保护方法相关的全部 {@link Rule}。
     *
     * @param method     受保护的方法
     * @param batchCount 单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 若无规则阈值被超过则返回 true，否则返回 false
     */
    public static boolean entry(Method method, int batchCount) {
        return entry(method, EntryType.OUT, batchCount, OBJECTS0);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name       资源的唯一字符串标识
     * @param batchCount 单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 若无规则阈值被超过则返回 true，否则返回 false
     */
    public static boolean entry(String name, int batchCount) {
        return entry(name, EntryType.OUT, batchCount, OBJECTS0);
    }

    /**
     * 检查受保护方法相关的全部 {@link Rule}。
     *
     * @param method 受保护的方法
     * @param type   资源为入站或出站方法。用于标记系统不稳定时是否可被限流，
     *               仅入站流量可被 {@link SystemRule} 阻断
     * @return 若无规则阈值被超过则返回 true，否则返回 false
     */
    public static boolean entry(Method method, EntryType type) {
        return entry(method, type, 1, OBJECTS0);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name 受保护资源的唯一名称
     * @param type 资源为入站或出站方法。用于标记系统不稳定时是否可被限流，
     *             仅入站流量可被 {@link SystemRule} 阻断
     * @return 若无规则阈值被超过则返回 true，否则返回 false
     */
    public static boolean entry(String name, EntryType type) {
        return entry(name, type, 1, OBJECTS0);
    }

    /**
     * 检查受保护方法相关的全部 {@link Rule}。
     *
     * @param method 受保护的方法
     * @param type   资源为入站或出站方法。用于标记系统不稳定时是否可被限流，
     *               仅入站流量可被 {@link SystemRule} 阻断
     * @param count  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 若无规则阈值被超过则返回 true，否则返回 false
     */
    public static boolean entry(Method method, EntryType type, int count) {
        return entry(method, type, count, OBJECTS0);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name  受保护资源的唯一名称
     * @param type  资源为入站或出站方法。用于标记系统不稳定时是否可被限流，
     *              仅入站流量可被 {@link SystemRule} 阻断
     * @param count 单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @return 若无规则阈值被超过则返回 true，否则返回 false
     */
    public static boolean entry(String name, EntryType type, int count) {
        return entry(name, type, count, OBJECTS0);
    }

    /**
     * 对给定资源记录统计并执行规则检查。
     *
     * @param name        受保护资源的唯一名称
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args        用于热点参数流控或自定义 Slot 的参数
     * @return 若无规则阈值被超过则返回 true，否则返回 false。
     */
    public static boolean entry(String name, EntryType trafficType, int batchCount, Object... args) {
        try {
            Env.sph.entry(name, trafficType, batchCount, args);
        } catch (BlockException e) {
            return false;
        } catch (Throwable e) {
            RecordLog.warn("SphO fatal error", e);
            return true;
        }
        return true;
    }

    /**
     * 对给定方法资源记录统计并执行规则检查。
     *
     * @param method      受保护的方法
     * @param trafficType 流量类型（入站、出站或内部）。用于标记系统不稳定时是否可被限流，
     *                    仅入站流量可被 {@link SystemRule} 阻断
     * @param batchCount  单次调用内的请求数量（例如 batchCount=2 表示申请 2 个令牌）
     * @param args        用于热点参数流控或自定义 Slot 的参数
     * @return 若无规则阈值被超过则返回 true，否则返回 false。
     */
    public static boolean entry(Method method, EntryType trafficType, int batchCount, Object... args) {
        try {
            Env.sph.entry(method, trafficType, batchCount, args);
        } catch (BlockException e) {
            return false;
        } catch (Throwable e) {
            RecordLog.warn("SphO fatal error", e);
            return true;
        }
        return true;
    }

    public static void exit(int count, Object... args) {
        ContextUtil.getContext().getCurEntry().exit(count, args);
    }

    public static void exit(int count) {
        ContextUtil.getContext().getCurEntry().exit(count, OBJECTS0);
    }

    public static void exit() {
        ContextUtil.getContext().getCurEntry().exit();
    }
}
