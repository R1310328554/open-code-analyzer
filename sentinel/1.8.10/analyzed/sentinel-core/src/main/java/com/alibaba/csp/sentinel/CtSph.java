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
import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.context.NullContext;
import com.alibaba.csp.sentinel.slotchain.MethodResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.SlotChainProvider;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.Rule;

/**
 * {@inheritDoc}
 *
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @author Eric Zhao
 * @see Sph
 */
public class CtSph implements Sph {

    private static final Object[] OBJECTS0 = new Object[0];

    /**
     * 相同资源（{@link ResourceWrapper#equals(Object)}）无论在哪个 {@link Context} 中
     * 都共享同一 {@link ProcessorSlotChain}。
     */
    private static volatile Map<ResourceWrapper, ProcessorSlotChain> chainMap
        = new HashMap<ResourceWrapper, ProcessorSlotChain>();

    private static final Object LOCK = new Object();

    private AsyncEntry asyncEntryWithNoChain(ResourceWrapper resourceWrapper, Context context) {
        AsyncEntry entry = new AsyncEntry(resourceWrapper, null, context);
        entry.initAsyncContext();
        // 异步 Entry 创建后立即从当前上下文中移除。
        entry.cleanCurrentEntryInLocal();
        return entry;
    }

    private AsyncEntry asyncEntryWithPriorityInternal(ResourceWrapper resourceWrapper, int count, boolean prioritized,
                                                      Object... args) throws BlockException {
        Context context = ContextUtil.getContext();
        if (context instanceof NullContext) {
            // {@link NullContext} 表示上下文数量已超过阈值，
            // 此处仅初始化 Entry，不执行规则检查。
            return asyncEntryWithNoChain(resourceWrapper, context);
        }
        if (context == null) {
            // 使用默认上下文。
            context = InternalContextUtil.internalEnter(Constants.CONTEXT_DEFAULT_NAME);
        }

        // 全局开关关闭，不执行规则检查。
        if (!Constants.ON) {
            return asyncEntryWithNoChain(resourceWrapper, context);
        }

        ProcessorSlot<Object> chain = lookProcessChain(resourceWrapper);

        // 表示 Processor 缓存大小超过 {@link Constants.MAX_SLOT_CHAIN_SIZE}，不执行规则检查。
        if (chain == null) {
            return asyncEntryWithNoChain(resourceWrapper, context);
        }

        AsyncEntry asyncEntry = new AsyncEntry(resourceWrapper, chain, context, count, args);
        try {
            chain.entry(context, resourceWrapper, null, count, prioritized, args);
            // 仅在 Entry 成功通过 Slot 链后初始化异步上下文。
            asyncEntry.initAsyncContext();
            // 异步调用可能在后台耗时，当前上下文不应挂在其上。
            // 因此需将当前异步 Entry 从当前上下文中移除。
            asyncEntry.cleanCurrentEntryInLocal();
        } catch (BlockException e1) {
            // 被阻断时，异步 Entry 将在当前上下文中 exit。
            // 异步上下文不会被初始化。
            asyncEntry.exitForContext(context, count, args);
            throw e1;
        } catch (Throwable e1) {
            // 正常情况下不应发生，除非 Sentinel 内部存在错误。
            // 发生时异步上下文不会被初始化。
            RecordLog.warn("Sentinel unexpected exception in asyncEntryInternal", e1);

            asyncEntry.cleanCurrentEntryInLocal();
        }
        return asyncEntry;
    }

    private AsyncEntry asyncEntryInternal(ResourceWrapper resourceWrapper, int count, Object... args)
        throws BlockException {
        return asyncEntryWithPriorityInternal(resourceWrapper, count, false, args);
    }

    private Entry entryWithPriority(ResourceWrapper resourceWrapper, int count, boolean prioritized, Object... args)
        throws BlockException {
        Context context = ContextUtil.getContext();
        if (context instanceof NullContext) {
            // {@link NullContext} 表示上下文数量已超过阈值，
            // 此处仅初始化 Entry，不执行规则检查。
            return new CtEntry(resourceWrapper, null, context);
        }

        if (context == null) {
            // 使用默认上下文。
            context = InternalContextUtil.internalEnter(Constants.CONTEXT_DEFAULT_NAME);
        }

        // 全局开关关闭，不执行规则检查。
        if (!Constants.ON) {
            return new CtEntry(resourceWrapper, null, context);
        }

        ProcessorSlot<Object> chain = lookProcessChain(resourceWrapper);

        /*
         * 表示资源（Slot 链）数量超过 {@link Constants.MAX_SLOT_CHAIN_SIZE}，
         * 不执行规则检查。
         */
        if (chain == null) {
            return new CtEntry(resourceWrapper, null, context);
        }

        Entry e = new CtEntry(resourceWrapper, chain, context, count, args);
        try {
            chain.entry(context, resourceWrapper, null, count, prioritized, args);
        } catch (BlockException e1) {
            e.exit(count, args);
            throw e1;
        } catch (Throwable e1) {
            // 正常情况下不应发生，除非 Sentinel 内部存在错误。
            RecordLog.info("Sentinel unexpected exception", e1);
        }
        return e;
    }

    /**
     * 对资源执行全部 {@link Rule} 检查。
     *
     * <p>每个不同资源使用一个 {@link ProcessorSlot} 做规则检查。相同资源全局共享同一
     * {@link ProcessorSlot}。</p>
     *
     * <p>注意 {@link ProcessorSlot} 总数不得超过 {@link Constants#MAX_SLOT_CHAIN_SIZE}，
     * 否则不做规则检查。此时所有请求直接通过，无检查也无异常。</p>
     *
     * @param resourceWrapper 资源名称
     * @param count           所需令牌数
     * @param args            用户方法调用参数
     * @return 表示本次调用的 {@link Entry}
     * @throws BlockException 若任一规则阈值被超过
     */
    public Entry entry(ResourceWrapper resourceWrapper, int count, Object... args) throws BlockException {
        return entryWithPriority(resourceWrapper, count, false, args);
    }

    /**
     * 获取资源的 {@link ProcessorSlotChain}。若资源尚未关联则创建新的
     * {@link ProcessorSlotChain}。
     *
     * <p>相同资源（{@link ResourceWrapper#equals(Object)}）全局共享同一
     * {@link ProcessorSlotChain}，与所在 {@link Context} 无关。<p/>
     *
     * <p>
     * 注意 {@link ProcessorSlot} 总数不得超过 {@link Constants#MAX_SLOT_CHAIN_SIZE}，
     * 否则返回 null。
     * </p>
     *
     * @param resourceWrapper 目标资源
     * @return 资源的 {@link ProcessorSlotChain}
     */
    ProcessorSlot<Object> lookProcessChain(ResourceWrapper resourceWrapper) {
        ProcessorSlotChain chain = chainMap.get(resourceWrapper);
        if (chain == null) {
            synchronized (LOCK) {
                chain = chainMap.get(resourceWrapper);
                if (chain == null) {
                    // Entry 数量上限。
                    if (chainMap.size() >= Constants.MAX_SLOT_CHAIN_SIZE) {
                        return null;
                    }

                    chain = SlotChainProvider.newSlotChain();
                    Map<ResourceWrapper, ProcessorSlotChain> newMap = new HashMap<ResourceWrapper, ProcessorSlotChain>(
                        chainMap.size() + 1);
                    newMap.putAll(chainMap);
                    newMap.put(resourceWrapper, chain);
                    chainMap = newMap;
                }
            }
        }
        return chain;
    }

    /**
     * 获取已创建 Slot 链的当前数量。
     *
     * @return 已创建 Slot 链数量
     * @since 0.2.0
     */
    public static int entrySize() {
        return chainMap.size();
    }

    /**
     * 重置 Slot 链映射。仅供内部测试。
     *
     * @since 0.2.0
     */
    static void resetChainMap() {
        chainMap.clear();
    }

    /**
     * 仅供内部测试。
     *
     * @since 0.2.0
     */
    static Map<ResourceWrapper, ProcessorSlotChain> getChainMap() {
        return chainMap;
    }

    /**
     * 用于跳过上下文名称检查的辅助类。
     */
    private final static class InternalContextUtil extends ContextUtil {
        static Context internalEnter(String name) {
            return trueEnter(name, "");
        }

        static Context internalEnter(String name, String origin) {
            return trueEnter(name, origin);
        }
    }

    @Override
    public Entry entry(String name) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, EntryType.OUT);
        return entry(resource, 1, OBJECTS0);
    }

    @Override
    public Entry entry(Method method) throws BlockException {
        MethodResourceWrapper resource = new MethodResourceWrapper(method, EntryType.OUT);
        return entry(resource, 1, OBJECTS0);
    }

    @Override
    public Entry entry(Method method, EntryType type) throws BlockException {
        MethodResourceWrapper resource = new MethodResourceWrapper(method, type);
        return entry(resource, 1, OBJECTS0);
    }

    @Override
    public Entry entry(String name, EntryType type) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, type);
        return entry(resource, 1, OBJECTS0);
    }

    @Override
    public Entry entry(Method method, EntryType type, int count) throws BlockException {
        MethodResourceWrapper resource = new MethodResourceWrapper(method, type);
        return entry(resource, count, OBJECTS0);
    }

    @Override
    public Entry entry(String name, EntryType type, int count) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, type);
        return entry(resource, count, OBJECTS0);
    }

    @Override
    public Entry entry(Method method, int count) throws BlockException {
        MethodResourceWrapper resource = new MethodResourceWrapper(method, EntryType.OUT);
        return entry(resource, count, OBJECTS0);
    }

    @Override
    public Entry entry(String name, int count) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, EntryType.OUT);
        return entry(resource, count, OBJECTS0);
    }

    @Override
    public Entry entry(Method method, EntryType type, int count, Object... args) throws BlockException {
        MethodResourceWrapper resource = new MethodResourceWrapper(method, type);
        return entry(resource, count, args);
    }

    @Override
    public Entry entry(String name, EntryType type, int count, Object... args) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, type);
        return entry(resource, count, args);
    }

    @Override
    public AsyncEntry asyncEntry(String name, EntryType type, int count, Object... args) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, type);
        return asyncEntryInternal(resource, count, args);
    }

    @Override
    public Entry entryWithPriority(String name, EntryType type, int count, boolean prioritized) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, type);
        return entryWithPriority(resource, count, prioritized);
    }

    @Override
    public Entry entryWithPriority(String name, EntryType type, int count, boolean prioritized, Object... args)
        throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, type);
        return entryWithPriority(resource, count, prioritized, args);
    }

    @Override
    public Entry entryWithType(String name, int resourceType, EntryType entryType, int count, Object[] args)
        throws BlockException {
        return entryWithType(name, resourceType, entryType, count, false, args);
    }

    @Override
    public Entry entryWithType(String name, int resourceType, EntryType entryType, int count, boolean prioritized,
                               Object[] args) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, entryType, resourceType);
        return entryWithPriority(resource, count, prioritized, args);
    }

    @Override
    public AsyncEntry asyncEntryWithType(String name, int resourceType, EntryType entryType, int count,
                                         boolean prioritized, Object[] args) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, entryType, resourceType);
        return asyncEntryWithPriorityInternal(resource, count, prioritized, args);
    }
}
