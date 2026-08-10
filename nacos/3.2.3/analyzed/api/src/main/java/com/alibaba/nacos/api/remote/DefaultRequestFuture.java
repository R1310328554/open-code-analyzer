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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link RequestFuture} 的默认实现，管理异步 RPC 请求的生命周期。
 *
 * <p>支持阻塞 {@link #get()}、限时 {@link #get(long)} 等待，并在响应到达或超时时触发 {@link RequestCallBack}；超时任务由 {@link RpcScheduledExecutor#TIMEOUT_SCHEDULER} 调度。</p>
 *
 * @author liuzunfei
 * @version $Id: DefaultRequestFuture.java, v 0.1 2020年09月01日 6:42 PM liuzunfei Exp $
 */
public class DefaultRequestFuture implements RequestFuture {
    
    /** 请求创建时间戳（毫秒）。 */
    private long timeStamp;
    
    /** 请求是否已完成（成功或失败）。 */
    private volatile boolean isDone = false;
    
    /** 响应是否成功。 */
    private boolean isSuccess;
    
    /** 异步回调，可为 {@code null}（同步等待模式）。 */
    private RequestCallBack requestCallBack;
    
    /** 失败时的异常信息。 */
    private Exception exception;
    
    /** 请求唯一标识。 */
    private String requestId;
    
    /** 所属 gRPC 连接标识。 */
    private String connectionId;
    
    /** 服务端返回的响应体。 */
    private Response response;
    
    /** 超时定时任务句柄。 */
    private ScheduledFuture timeoutFuture;
    
    /** 超时/取消时的清理触发器。 */
    FutureTrigger futureTrigger;
    
    /** 返回关联的异步回调。 */
    public RequestCallBack getRequestCallBack() {
        return requestCallBack;
    }
    
    /** 返回请求创建时间戳。 */
    public long getTimeStamp() {
        return timeStamp;
    }
    
    /**
     * 构造无回调的同步等待 Future。
     *
     * @param connectionId 连接 ID
     * @param requestId    请求 ID
     */
        this(connectionId, requestId, null, null);
    }
    
    /**
     * 构造完整 Future，可选注册回调与清理触发器。
     *
     * @param connectionId    连接 ID
     * @param requestId       请求 ID
     * @param requestCallBack 异步回调（可为 null）
     * @param futureTrigger   超时/取消清理触发器
     */
        this.timeStamp = System.currentTimeMillis();
        this.requestCallBack = requestCallBack;
        this.requestId = requestId;
        this.connectionId = connectionId;
        if (requestCallBack != null) {
            this.timeoutFuture =
                RpcScheduledExecutor.TIMEOUT_SCHEDULER.schedule(new TimeoutHandler(),
                    requestCallBack.getTimeout(), TimeUnit.MILLISECONDS);
        }
        this.futureTrigger = futureTrigger;
    }
    
    /** 设置成功响应并唤醒等待线程、触发回调。 */
    public void setResponse(final Response response) {
        isDone = true;
        this.response = response;
        this.isSuccess = response.isSuccess();
        if (this.timeoutFuture != null) {
            timeoutFuture.cancel(true);
        }
        synchronized (this) {
            notifyAll();
        }
        
        callBacInvoke();
    }
    
    /** 标记请求失败并唤醒等待线程、触发异常回调。 */
    public void setFailResult(Exception e) {
        isDone = true;
        isSuccess = false;
        this.exception = e;
        synchronized (this) {
            notifyAll();
        }
        
        callBacInvoke();
    }
    
    /** 在回调线程或当前线程执行 {@link RequestCallBack}。 */
    private void callBacInvoke() {
        if (requestCallBack != null) {
            if (requestCallBack.getExecutor() != null) {
                requestCallBack.getExecutor().execute(new CallBackHandler());
            } else {
                new CallBackHandler().run();
            }
        }
    }
    
    /** 返回请求 ID。 */
    public String getRequestId() {
        return this.requestId;
    }
    
    /** {@inheritDoc} 请求是否已完成。 */
    @Override
    public boolean isDone() {
        return isDone;
    }
    
    /** {@inheritDoc} 无限期阻塞直到响应到达。 */
    @Override
    public Response get() throws InterruptedException {
        synchronized (this) {
            while (!isDone) {
                wait();
            }
        }
        return response;
    }
    
    /** {@inheritDoc} 在指定毫秒内等待响应，超时抛出 {@link TimeoutException}。 */
    @Override
    public Response get(long timeout) throws TimeoutException, InterruptedException {
        if (timeout < 0) {
            synchronized (this) {
                while (!isDone) {
                    wait();
                }
            }
        } else if (timeout > 0) {
            long end = System.currentTimeMillis() + timeout;
            long waitTime = timeout;
            synchronized (this) {
                while (!isDone && waitTime > 0) {
                    wait(waitTime);
                    waitTime = end - System.currentTimeMillis();
                }
            }
        }
        
        if (isDone) {
            return response;
        } else {
            if (timeoutFuture == null && futureTrigger != null) {
                futureTrigger.triggerOnTimeout();
            }
            throw new TimeoutException(
                "request timeout after " + timeout + " milliseconds, requestId=" + requestId
                    + ", connectionId="
                    + connectionId);
        }
    }
    
    /** 在独立线程或回调执行器中分发响应/异常。 */
    class CallBackHandler implements Runnable {
        
        /** 根据结果调用 {@link RequestCallBack#onException} 或 {@link RequestCallBack#onResponse}。 */
        /** 超时到达时构造 {@link TimeoutException} 并通知 Future。 */
        @Override
        public void run() {
            if (exception != null) {
                requestCallBack.onException(exception);
            } else {
                requestCallBack.onResponse(response);
            }
        }
    }
    
    /** 超时定时任务：标记失败并触发 {@link FutureTrigger#triggerOnTimeout()}。 */
    class TimeoutHandler implements Runnable {
        
        /** 无参构造。 */
        public TimeoutHandler() {
        }
        
        @Override
        public void run() {
            setFailResult(new TimeoutException(
                "Timeout After " + requestCallBack.getTimeout() + " milliseconds, requestId="
                    + requestId
                    + ", connectionId=" + connectionId));
            if (futureTrigger != null) {
                futureTrigger.triggerOnTimeout();
            }
        }
    }
    
    /**
     * 请求失败、取消或超时时的资源清理钩子。
     */
    public interface FutureTrigger {
        
        /** 超时与取消共用的默认清理逻辑。 */
        void defaultTrigger();
        
        /** 请求超时时触发，默认委托 {@link #defaultTrigger()}。 */
        default void triggerOnTimeout() {
            defaultTrigger();
        }
        
        /** 请求取消时触发，默认委托 {@link #defaultTrigger()}。 */
        default void triggerOnCancel() {
            defaultTrigger();
        }
        
    }
    
    /** 返回所属连接 ID。 */
    public String getConnectionId() {
        return connectionId;
    }
    
    /**
     * 取消进行中的请求。
     *
     * <p>应在 {@link com.alibaba.nacos.core.remote.grpc.GrpcConnection#sendRequestInner} 中调用；无 {@link RequestCallBack} 的同步请求取消无效。</p>
     *
     * @param mayInterruptIfRunning 是否中断正在运行的超时任务
     */
    public void cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            notifyAll();
        }
        // 取消超时定时任务。
        if (timeoutFuture != null && !timeoutFuture.isDone()) {
            boolean cancel = timeoutFuture.cancel(mayInterruptIfRunning);
            if (cancel && futureTrigger != null) {
                futureTrigger.triggerOnCancel();
            }
        }
        
    }
}
