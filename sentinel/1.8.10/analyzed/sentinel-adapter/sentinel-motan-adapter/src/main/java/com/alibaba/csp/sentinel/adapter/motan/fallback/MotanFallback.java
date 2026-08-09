/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.motan.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;

/**
 * Motan 适配器降级处理器接口，流控触发时由过滤器回调。
 *
 * @author zhangxn8
 */
public interface MotanFallback {

    /**
     * 处理流控异常并返回降级结果。
     *
     * @param caller Motan 调用方
     * @param request RPC 请求
     * @param ex 流控异常
     * @return 降级响应
     */
    Response handle(Caller<?> caller, Request request, BlockException ex);

}
