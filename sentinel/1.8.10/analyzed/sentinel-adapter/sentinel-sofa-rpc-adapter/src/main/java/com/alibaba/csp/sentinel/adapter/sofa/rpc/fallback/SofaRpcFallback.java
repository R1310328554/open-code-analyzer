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
package com.alibaba.csp.sentinel.adapter.sofa.rpc.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.FilterInvoker;

/**
 * SOFARPC 适配器降级处理器接口。
 *
 * @author cdfive
 */
public interface SofaRpcFallback {

    /**
     * 处理流控异常并提供降级结果。
     *
     * @param invoker FilterInvoker
     * @param request SofaRequest
     * @param ex 流控异常
     * @return 降级结果
     */
    SofaResponse handle(FilterInvoker invoker, SofaRequest request, BlockException ex);
}
