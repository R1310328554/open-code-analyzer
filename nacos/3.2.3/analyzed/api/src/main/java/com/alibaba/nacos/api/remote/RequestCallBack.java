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

import java.util.concurrent.Executor;

/**
 * 异步 RPC 请求的结果回调接口。
 *
 * <p>由 {@link Requester#asyncRequest} 传入；响应到达后调用 {@link #onResponse}，异常或超时调用 {@link #onException}；可通过 {@link #getExecutor()} 指定回调线程池。</p>
 *
 * @author liuzunfei
 * @version $Id: PushCallBack.java, v 0.1 2020年09月01日 6:33 PM liuzunfei Exp $
 */
public interface RequestCallBack<T extends Response> {
    
    /**
     * 回调执行线程池，返回 {@code null} 时在 IO 线程直接执行。
     *
     * @return 执行器，可为 null
     */
    Executor getExecutor();
    
    /**
     * 请求等待超时时间（毫秒）。
     *
     * @return 超时毫秒数
     */
    long getTimeout();
    
    /**
     * 收到成功响应时调用。
     *
     * @param response 服务端响应
     */
    void onResponse(T response);
    
    /**
     * 请求失败或超时时调用。
     *
     * @param e 异常信息
     */
    void onException(Throwable e);
    
}
