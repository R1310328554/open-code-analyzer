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

package com.alibaba.nacos.core.distributed.raft.utils;

import com.alibaba.nacos.consistency.entity.Response;
import com.alipay.sofa.jraft.Closure;

/**
 * 基于 JRaft {@link Closure} 的故障回调接口：Raft apply 失败时携带业务响应与异常信息，供 {@link FailoverClosureImpl} 完成 {@link java.util.concurrent.CompletableFuture}。
 * Failure callback based on Closure.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface FailoverClosure extends Closure {
    
    /**
     * 设置业务层返回数据（apply 成功时使用）。
     *
     * @param response {@link Response} data
     */
    void setResponse(Response response);
    
    /**
     * 记录 apply 过程中捕获的异常（失败时优先用于构造 {@link com.alibaba.nacos.consistency.exception.ConsistencyException}）。
     *
     * @param throwable {@link Throwable}
     */
    void setThrowable(Throwable throwable);
    
}
