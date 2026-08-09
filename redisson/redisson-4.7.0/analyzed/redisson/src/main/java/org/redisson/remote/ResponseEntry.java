/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.remote;

import io.netty.util.Timeout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 远程响应监听条目：
 * 按响应队列名维护待完成的 {@link CompletableFuture} 列表，
 * 并跟踪 Netty 超时定时器以便在收到响应或超时时清理。
 * <p>
 * 内部 {@link Result} 封装单个请求的 promise 与超时句柄。
 *
 * @author Nikita Koksharov
 *
 */
public class ResponseEntry {

    /** 单个远程请求的异步结果与超时控制。 */
    public static class Result {

        /** 等待 {@link RRemoteServiceResponse} 的 Future。 */
        private final CompletableFuture<? extends RRemoteServiceResponse> promise;
        /** Netty 响应超时定时任务。 */
        private Timeout responseTimeoutFuture;
        
        /** @param promise 待完成的响应 Future */
        public Result(CompletableFuture<? extends RRemoteServiceResponse> promise) {
            super();
            this.promise = promise;
        }
        
        /** @return 类型安全的响应 Future */
        public <T extends RRemoteServiceResponse> CompletableFuture<T> getPromise() {
            return (CompletableFuture<T>) promise;
        }

        /** 绑定 Netty 超时定时器。 */
        public void setResponseTimeoutFuture(Timeout responseTimeoutFuture) {
            this.responseTimeoutFuture = responseTimeoutFuture;
        }

        /** 收到响应后取消超时定时器。 */
        public void cancelResponseTimeout() {
            responseTimeoutFuture.cancel();
        }
        
    }
    
    /** 请求 ID → 待完成 Result 列表（支持同一 ID 多监听场景）。 */
    private final Map<String, List<Result>> responses = new HashMap<String, List<Result>>();
    /** 响应队列监听是否已启动。 */
    private final AtomicBoolean started = new AtomicBoolean(); 
    
    /** @return 响应 Future 映射表 */
    public Map<String, List<Result>> getResponses() {
        return responses;
    }
    
    /** @return 监听启动标志 */
    public AtomicBoolean getStarted() {
        return started;
    }
    
}
