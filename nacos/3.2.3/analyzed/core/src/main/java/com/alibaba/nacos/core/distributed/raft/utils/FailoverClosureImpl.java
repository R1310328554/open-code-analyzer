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
import com.alibaba.nacos.consistency.exception.ConsistencyException;
import com.alipay.sofa.jraft.Status;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@link FailoverClosure} 默认实现：在 JRaft {@link com.alipay.sofa.jraft.Status} 回调中将成功响应或一致性异常写入 {@link java.util.concurrent.CompletableFuture}。
 * Closure with internal retry mechanism.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class FailoverClosureImpl implements FailoverClosure {
    
    /** 上层异步等待的 Future，由 {@link #run(com.alipay.sofa.jraft.Status)} 完成或异常结束。 */
    private final CompletableFuture<Response> future;
    
    /** apply 成功时待回传的业务响应。 */
    private volatile Response data;
    
    /** apply 失败时记录的根因异常。 */
    private volatile Throwable throwable;
    
    /**
     * 绑定异步结果 Future。
     *
     * @param future 待完成的 CompletableFuture
     */
    public FailoverClosureImpl(final CompletableFuture<Response> future) {
        this.future = future;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setResponse(Response data) {
        this.data = data;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
    
    /**
     * JRaft 回调：Status 正常则 {@code future.complete(data)}，否则以 {@link com.alibaba.nacos.consistency.exception.ConsistencyException} 异常完成 Future。
     *
     * @param status Raft apply 状态
     */
    @Override
    public void run(Status status) {
        if (status.isOk()) {
            future.complete(data);
            return;
        }
        final Throwable throwable = this.throwable;
        future.completeExceptionally(
            Objects.nonNull(throwable) ? new ConsistencyException(throwable.getMessage())
                : new ConsistencyException("operation failure"));
    }
    
}
