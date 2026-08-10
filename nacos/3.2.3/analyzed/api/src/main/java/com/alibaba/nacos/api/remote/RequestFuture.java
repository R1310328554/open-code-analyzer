/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.remote;

import com.alibaba.nacos.api.remote.response.Response;

/**
 * 异步 RPC 请求的 Future 抽象，支持阻塞等待响应。
 *
 * <p>由 {@link Requester#requestFuture} 返回；典型实现为 {@link DefaultRequestFuture}。</p>
 *
 * @author liuzunfei
 * @version $Id: RequestFuture.java, v 0.1 2020年09月01日 6:31 PM liuzunfei Exp $
 */
public interface RequestFuture {
    
    /**
     * 请求是否已完成（成功或失败）。
     *
     * @return 已完成返回 {@code true}
     */
    boolean isDone();
    
    /**
     * 无限期阻塞直到响应到达。
     *
     * @return 响应对象
     * @throws Exception 等待被中断或请求失败
     */
    Response get() throws Exception;
    
    /**
     * 在指定毫秒内等待响应。
     *
     * @param timeout 超时毫秒数
     * @return 响应对象
     * @throws Exception 超时、中断或请求失败
     */
    Response get(long timeout) throws Exception;
    
}
