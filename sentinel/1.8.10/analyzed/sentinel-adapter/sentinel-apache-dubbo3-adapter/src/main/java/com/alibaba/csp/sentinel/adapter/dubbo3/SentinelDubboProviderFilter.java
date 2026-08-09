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
package com.alibaba.csp.sentinel.adapter.dubbo3;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.dubbo3.config.DubboAdapterGlobalConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * <p>集成 Sentinel 的 Apache Dubbo 服务 Provider 过滤器，默认自动激活。</p>
 * <p>注意：仅适用于 Apache Dubbo 2.7.x 及以上版本。</p>
 * <p>
 * 如需禁用 Provider 过滤器，可配置：
 * <pre>
 * &lt;dubbo:provider filter="-sentinel.dubbo.provider.filter"/&gt;
 * </pre>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
@Activate(group = PROVIDER)
public class SentinelDubboProviderFilter extends BaseSentinelDubboFilter implements Filter {

    public SentinelDubboProviderFilter() {
        RecordLog.info("Sentinel Apache Dubbo3 provider filter initialized");
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
        // 获取调用方来源。
        String origin = DubboAdapterGlobalConfig.getOriginParser().parse(invoker, invocation);
        if (null == origin) {
            origin = "";
        }
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        String prefix = DubboAdapterGlobalConfig.getDubboProviderResNamePrefixKey();
        String interfaceResourceName = getInterfaceName(invoker, prefix);
        String methodResourceName = getMethodName(invoker, invocation, prefix);
        try {
            // 仅在 Provider 侧创建入口 Context，Context 仅在调用链入口（入站流量）生效。
            ContextUtil.enter(methodResourceName, origin);
            interfaceEntry = SphU.entry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN);
            methodEntry = SphU.entry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN,
                invocation.getArguments());
            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                Tracer.traceEntry(result.getException(), interfaceEntry);
                Tracer.traceEntry(result.getException(), methodEntry);
            }
            return result;
        } catch (BlockException e) {
            return DubboAdapterGlobalConfig.getProviderFallback().handle(invoker, invocation, e);
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
            ContextUtil.exit();
        }
    }

}

