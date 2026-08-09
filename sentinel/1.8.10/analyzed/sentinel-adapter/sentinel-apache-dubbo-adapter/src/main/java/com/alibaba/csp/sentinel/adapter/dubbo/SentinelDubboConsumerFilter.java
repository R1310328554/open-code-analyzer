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
package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboAdapterGlobalConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.support.RpcUtils;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

/**
 * <p>Sentinel 集成的 Dubbo 消费者 Filter，默认自动激活。</p>
 * <p>
 * 如需禁用消费者 Filter，可配置：
 * <pre>
 * &lt;dubbo:consumer filter="-sentinel.dubbo.consumer.filter"/&gt;
 * </pre>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 * @author Lin Liang
 */
@Activate(group = CONSUMER)
public class SentinelDubboConsumerFilter extends BaseSentinelDubboFilter {

    /** 初始化消费者 Filter 并记录日志。 */
    public SentinelDubboConsumerFilter() {
        RecordLog.info("Sentinel Apache Dubbo consumer filter initialized");
    }

    @Override
    String getMethodName(Invoker invoker, Invocation invocation, String prefix) {
        return DubboUtils.getMethodResourceName(invoker, invocation, prefix);
    }

    @Override
    String getInterfaceName(Invoker invoker, String prefix) {
        return DubboUtils.getInterfaceName(invoker, prefix);
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        InvokeMode invokeMode = RpcUtils.getInvokeMode(invoker.getUrl(), invocation);
        if (InvokeMode.SYNC == invokeMode) {
            return syncInvoke(invoker, invocation);
        } else {
            return asyncInvoke(invoker, invocation);
        }
    }

    /** 同步调用路径：对接口与方法资源分别 entry/exit。 */
    private Result syncInvoke(Invoker<?> invoker, Invocation invocation) {
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        String prefix = DubboAdapterGlobalConfig.getDubboConsumerResNamePrefixKey();
        String interfaceResourceName = getInterfaceName(invoker, prefix);
        String methodResourceName = getMethodName(invoker, invocation, prefix);
        try {
            interfaceEntry = SphU.entry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT);
            methodEntry = SphU.entry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT,
                invocation.getArguments());
            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                Tracer.traceEntry(result.getException(), interfaceEntry);
                Tracer.traceEntry(result.getException(), methodEntry);
            }
            return result;
        } catch (BlockException e) {
            return DubboAdapterGlobalConfig.getConsumerFallback().handle(invoker, invocation, e);
        } catch (RpcException e) {
            Tracer.traceEntry(e, interfaceEntry);
            Tracer.traceEntry(e, methodEntry);
            throw e;
        } finally {
            if (methodEntry != null) {
                methodEntry.exit(1, invocation.getArguments());
            }
            if (interfaceEntry != null) {
                interfaceEntry.exit();
            }
        }
    }

    /** 异步调用路径：使用 asyncEntry 并在 whenComplete 中 exit。 */
    private Result asyncInvoke(Invoker<?> invoker, Invocation invocation) {
        LinkedList<EntryHolder> queue = new LinkedList<>();
        String prefix = DubboAdapterGlobalConfig.getDubboConsumerResNamePrefixKey();
        String interfaceResourceName = getInterfaceName(invoker, prefix);
        String methodResourceName = getMethodName(invoker, invocation, prefix);
        try {
            queue.push(new EntryHolder(
                SphU.asyncEntry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT), null));
            queue.push(new EntryHolder(
                SphU.asyncEntry(methodResourceName, ResourceTypeConstants.COMMON_RPC,
                    EntryType.OUT, 1, invocation.getArguments()), invocation.getArguments()));
            Result result = invoker.invoke(invocation);
            result.whenCompleteWithContext((r, throwable) -> {
                Throwable error = throwable;
                if (error == null) {
                    error = Optional.ofNullable(r).map(Result::getException).orElse(null);
                }
                while (!queue.isEmpty()) {
                    EntryHolder holder = queue.pop();
                    Tracer.traceEntry(error, holder.entry);
                    exitEntry(holder);
                }
            });
            return result;
        } catch (BlockException e) {
            while (!queue.isEmpty()) {
                exitEntry(queue.pop());
            }
            return DubboAdapterGlobalConfig.getConsumerFallback().handle(invoker, invocation, e);
        }
    }

    /** 异步调用时暂存 Entry 与参数，供完成回调 exit。 */
    static class EntryHolder {

        final private Entry entry;
        final private Object[] params;

        public EntryHolder(Entry entry, Object[] params) {
            this.entry = entry;
            this.params = params;
        }
    }

    /** 按是否携带参数退出 Entry。 */
    private void exitEntry(EntryHolder holder) {
        if (holder.params != null) {
            holder.entry.exit(1, holder.params);
        } else {
            holder.entry.exit();
        }
    }
}
