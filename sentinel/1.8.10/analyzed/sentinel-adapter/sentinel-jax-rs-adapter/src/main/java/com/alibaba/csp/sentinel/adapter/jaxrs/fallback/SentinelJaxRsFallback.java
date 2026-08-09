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
package com.alibaba.csp.sentinel.adapter.jaxrs.fallback;

import javax.ws.rs.core.Response;
import java.util.concurrent.Future;

/**
 * JAX-RS 适配器降级处理器接口。
 *
 * @author sea
 */
public interface SentinelJaxRsFallback {

    /**
     * 根据执行失败原因提供降级响应。
     *
     * @param route 降级对应的路由
     * @param cause 主方法失败原因，可能为 <code>null</code>
     * @return 降级响应
     */
    Response fallbackResponse(String route, Throwable cause);

    /**
     * 根据执行失败原因提供异步降级响应 Future。
     *
     * @param route 降级对应的路由
     * @param cause 主方法失败原因，可能为 <code>null</code>
     * @return 降级响应 Future
     */
    Future<Response> fallbackFutureResponse(String route, Throwable cause);
}
