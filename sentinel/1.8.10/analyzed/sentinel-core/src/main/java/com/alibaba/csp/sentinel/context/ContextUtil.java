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
package com.alibaba.csp.sentinel.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;

/**
 * 在当前线程中获取或创建 {@link Context} 的工具类。
 *
 * <p>
 * 每次 {@link SphU}#entry() 或 {@link SphO}#entry() 都应在某个 {@link Context} 中执行；
 * 若未显式调用 {@link ContextUtil}#enter()，将使用 DEFAULT 上下文。
 * </p>
 *
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @author Eric Zhao
 */
public class ContextUtil {

    /**
     * 使用 ThreadLocal 存储上下文，便于访问。
     */
    private static ThreadLocal<Context> contextHolder = new ThreadLocal<>();

    /**
     * 保存全部 {@link EntranceNode}，每个 EntranceNode 对应一个独立的上下文名称。
     */
    private static volatile Map<String, DefaultNode> contextNameNodeMap = new HashMap<>();

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Context NULL_CONTEXT = new NullContext();

    static {
        // 缓存默认上下文的入口节点。
        initDefaultContext();
    }

    private static void initDefaultContext() {
        String defaultContextName = Constants.CONTEXT_DEFAULT_NAME;
        EntranceNode node = new EntranceNode(new StringResourceWrapper(defaultContextName, EntryType.IN), null);
        Constants.ROOT.addChild(node);
        contextNameNodeMap.put(defaultContextName, node);
    }

    /**
     * 非线程安全，仅供测试。
     */
    static void resetContextMap() {
        if (contextNameNodeMap != null) {
            RecordLog.warn("Context map cleared and reset to initial state");
            contextNameNodeMap.clear();
            initDefaultContext();
        }
    }

    /**
     * <p>
     * 进入调用上下文，标记调用链的入口。
     * 上下文封装在 {@code ThreadLocal} 中，即每个线程拥有独立的 {@link Context}；
     * 若当前线程尚无上下文则创建新上下文。
     * </p>
     * <p>
     * 上下文会绑定一个 {@link EntranceNode}，表示调用链的入口统计节点。
     * 若尚不存在则创建新的 EntranceNode。相同上下文名称全局共享同一 EntranceNode。
     * </p>
     * <p>
     * 来源节点在 {@link com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot} 中创建。
     * 不同资源的不同 {@code origin} 会创建不同的 {@link Node}，
     * 即来源统计节点总数约为：<br/>
     * {@code 不同资源名数量 × 不同 origin 数量}。<br/>
     * origin 过多时需仔细评估内存占用。
     * </p>
     * <p>
     * 不同上下文中的相同资源分别计数，见 {@link NodeSelectorSlot}。
     * </p>
     *
     * @param name   上下文名称
     * @param origin 本次调用的来源，通常为服务消费者的应用名；
     *               用于分别控制不同调用方/消费者
     * @return 当前线程的调用上下文
     */
    public static Context enter(String name, String origin) {
        if (Constants.CONTEXT_DEFAULT_NAME.equals(name)) {
            throw new ContextNameDefineException(
                "The " + Constants.CONTEXT_DEFAULT_NAME + " can't be permit to defined!");
        }
        return trueEnter(name, origin);
    }

    protected static Context trueEnter(String name, String origin) {
        Context context = contextHolder.get();
        if (context == null) {
            Map<String, DefaultNode> localCacheNameMap = contextNameNodeMap;
            DefaultNode node = localCacheNameMap.get(name);
            if (node == null) {
                if (localCacheNameMap.size() > Constants.MAX_CONTEXT_NAME_SIZE) {
                    setNullContext();
                    return NULL_CONTEXT;
                } else {
                    LOCK.lock();
                    try {
                        node = contextNameNodeMap.get(name);
                        if (node == null) {
                            if (contextNameNodeMap.size() > Constants.MAX_CONTEXT_NAME_SIZE) {
                                setNullContext();
                                return NULL_CONTEXT;
                            } else {
                                node = new EntranceNode(new StringResourceWrapper(name, EntryType.IN), null);
                                // 添加入口节点。
                                Constants.ROOT.addChild(node);

                                Map<String, DefaultNode> newMap = new HashMap<>(contextNameNodeMap.size() + 1);
                                newMap.putAll(contextNameNodeMap);
                                newMap.put(name, node);
                                contextNameNodeMap = newMap;
                            }
                        }
                    } finally {
                        LOCK.unlock();
                    }
                }
            }
            context = new Context(node, name);
            context.setOrigin(origin);
            contextHolder.set(context);
        }

        return context;
    }

    private static boolean shouldWarn = true;

    private static void setNullContext() {
        contextHolder.set(NULL_CONTEXT);
        // 无需线程安全。
        if (shouldWarn) {
            RecordLog.warn("[SentinelStatusChecker] WARN: Amount of context exceeds the threshold "
                + Constants.MAX_CONTEXT_NAME_SIZE + ". Entries in new contexts will NOT take effect!");
            shouldWarn = false;
        }
    }

    /**
     * <p>
     * 进入调用上下文，标记调用链的入口。
     * 上下文封装在 {@code ThreadLocal} 中，即每个线程拥有独立的 {@link Context}；
     * 若当前线程尚无上下文则创建新上下文。
     * </p>
     * <p>
     * 上下文会绑定一个 {@link EntranceNode}，表示调用链的入口统计节点。
     * 若尚不存在则创建新的 EntranceNode。相同上下文名称全局共享同一 EntranceNode。
     * </p>
     * <p>
     * 不同上下文中的相同资源分别计数，见 {@link NodeSelectorSlot}。
     * </p>
     *
     * @param name 上下文名称
     * @return 当前线程的调用上下文
     */
    public static Context enter(String name) {
        return enter(name, "");
    }

    /**
     * 退出当前线程的上下文，即从 ThreadLocal 中移除 {@link Context}。
     */
    public static void exit() {
        Context context = contextHolder.get();
        if (context != null && context.getCurEntry() == null) {
            contextHolder.set(null);
        }
    }

    /**
     * 获取上下文入口节点映射的当前大小。
     *
     * @return 上下文入口节点映射的当前大小
     * @since 0.2.0
     */
    public static int contextSize() {
        return contextNameNodeMap.size();
    }

    /**
     * 检查给定上下文是否为自动创建的默认上下文。
     *
     * @param context 待检查的上下文
     * @return 若为默认上下文则返回 true，否则返回 false
     * @since 0.2.0
     */
    public static boolean isDefaultContext(Context context) {
        if (context == null) {
            return false;
        }
        return Constants.CONTEXT_DEFAULT_NAME.equals(context.getName());
    }

    /**
     * 获取当前线程的 {@link Context}。
     *
     * @return 当前线程的上下文；若当前线程无上下文则返回 null
     */
    public static Context getContext() {
        return contextHolder.get();
    }

    /**
     * <p>
     * 用给定上下文替换当前上下文。
     * 主要用于上下文切换（例如异步调用场景）。
     * </p>
     * <p>
     * 注意：手动切换上下文后应恢复原始上下文。
     * 常见场景可使用 {@link #runOnContext(Context, Runnable)}。
     * </p>
     *
     * @param newContext 要设置的新上下文
     * @return 被替换的旧上下文
     * @since 0.2.0
     */
    static Context replaceContext(Context newContext) {
        Context backupContext = contextHolder.get();
        if (newContext == null) {
            contextHolder.remove();
        } else {
            contextHolder.set(newContext);
        }
        return backupContext;
    }

    /**
     * 在指定上下文中执行代码。
     * 主要用于上下文切换（例如异步调用场景）。
     *
     * @param context 目标上下文
     * @param f       在上下文中运行的 Runnable
     * @since 0.2.0
     */
    public static void runOnContext(Context context, Runnable f) {
        Context curContext = replaceContext(context);
        try {
            f.run();
        } finally {
            replaceContext(curContext);
        }
    }
}
