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
package com.alibaba.csp.sentinel.adapter.dubbo3.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

/**
 * Dubbo 阻断时的默认降级处理器，将 {@link BlockException} 包装为运行时异常返回。
 *
 * @author Eric Zhao
 */
public class DefaultDubboFallback implements DubboFallback {

    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {
        // 将异常包装后返回。
        return AsyncRpcResult.newDefaultAsyncResult(ex.toRuntimeException(), invocation);
    }
}
