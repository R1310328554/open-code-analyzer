/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.context;

import java.util.function.Supplier;

/**
 * 请求上下文 ThreadLocal 持有者：为每个工作线程懒创建 {@link RequestContext}，请求结束须调用 {@link #removeContext()} 防止线程池复用导致泄漏。
 * Holder for request context for each worker thread.
 *
 * @author xiweng.yy
 */
public class RequestContextHolder {
    
    /** 首次访问时以当前时间戳构造 {@link RequestContext}。 */
    private static final Supplier<RequestContext> REQUEST_CONTEXT_FACTORY = () -> {
        long requestTimestamp = System.currentTimeMillis();
        return new RequestContext(requestTimestamp);
    };
    
    /** 线程级请求上下文存储。 */
    private static final ThreadLocal<RequestContext> CONTEXT_HOLDER =
        ThreadLocal.withInitial(REQUEST_CONTEXT_FACTORY);
    
    /** 获取当前线程的请求上下文（不存在则自动创建）。 */
    public static RequestContext getContext() {
        return CONTEXT_HOLDER.get();
    }
    
    /** 清除当前线程上下文，应在请求处理 finally 块中调用。 */
    public static void removeContext() {
        CONTEXT_HOLDER.remove();
    }
}
