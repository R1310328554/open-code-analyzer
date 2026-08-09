/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.rocketmq.promise;

import io.openmessaging.Promise;
import io.openmessaging.FutureListener;
import io.openmessaging.exception.OMSRuntimeException;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * OMS {@link Promise} 默认实现：基于 wait/notify 的异步结果容器。
 */
public class DefaultPromise<V> implements Promise<V> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPromise.class);
    /** 状态变更与 get 等待的互斥锁。 */
    private final Object lock = new Object();
    /** 当前 Promise 状态。 */
    private volatile FutureState state = FutureState.DOING;
    private V result = null;
    private long timeout;
    private long createTime;
    private Throwable exception = null;
    /** 完成时待通知的监听器列表。 */
    private List<FutureListener<V>> promiseListenerList;

    /** 创建 Promise，默认超时 5000ms。 */
    public DefaultPromise() {
        createTime = System.currentTimeMillis();
        promiseListenerList = new ArrayList<>();
        timeout = 5000;
    }

    /** 取消操作（当前恒返回 false）。 */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return state.isCancelledState();
    }

    @Override
    public boolean isDone() {
        return state.isDoneState();
    }

    @Override
    public V get() {
        return result;
    }

    /** 带超时阻塞获取结果，超时则标记为 CANCELLED。 */
    @Override
    public V get(final long timeout) {
        synchronized (lock) {
            if (!isDoing()) {
                return getValueOrThrowable();
            }

            if (timeout <= 0) {
                try {
                    lock.wait();
                } catch (Exception e) {
                    cancel(e);
                }
                return getValueOrThrowable();
            } else {
                long waitTime = timeout - (System.currentTimeMillis() - createTime);
                if (waitTime > 0) {
                    for (; ; ) {
                        try {
                            lock.wait(waitTime);
                        } catch (InterruptedException e) {
                            LOG.error("promise get value interrupted,exception:{}", e.getMessage());
                        }

                        if (!isDoing()) {
                            break;
                        } else {
                            waitTime = timeout - (System.currentTimeMillis() - createTime);
                            if (waitTime <= 0) {
                                break;
                            }
                        }
                    }
                }

                if (isDoing()) {
                    timeoutSoCancel();
                }
            }
            return getValueOrThrowable();
        }
    }

    /** 设置成功结果并唤醒等待线程。 */
    @Override
    public boolean set(final V value) {
        if (value == null)
            return false;
        this.result = value;
        return done();
    }

    /** 设置失败原因并完成 Promise。 */
    @Override
    public boolean setFailure(final Throwable cause) {
        if (cause == null)
            return false;
        this.exception = cause;
        return done();
    }

    /** 注册完成监听器；若已完成则立即回调。 */
    @Override
    public void addListener(final FutureListener<V> listener) {
        if (listener == null) {
            throw new NullPointerException("FutureListener is null");
        }

        boolean notifyNow = false;
        synchronized (lock) {
            if (!isDoing()) {
                notifyNow = true;
            } else {
                if (promiseListenerList == null) {
                    promiseListenerList = new ArrayList<>();
                }
                promiseListenerList.add(listener);
            }
        }

        if (notifyNow) {
            notifyListener(listener);
        }
    }

    @Override
    public Throwable getThrowable() {
        return exception;
    }

    private void notifyListeners() {
        if (promiseListenerList != null) {
            for (FutureListener<V> listener : promiseListenerList) {
                notifyListener(listener);
            }
        }
    }

    private boolean isSuccess() {
        return isDone() && exception == null;
    }

    /** 等待超时后将状态置为 CANCELLED 并通知监听器。 */
    private void timeoutSoCancel() {
        synchronized (lock) {
            if (!isDoing()) {
                return;
            }
            state = FutureState.CANCELLED;
            exception = new RuntimeException("Get request result is timeout or interrupted");
            lock.notifyAll();
        }
        notifyListeners();
    }

    /** 返回结果或包装异常为 {@link OMSRuntimeException} 抛出。 */
    private V getValueOrThrowable() {
        if (exception != null) {
            Throwable e = exception.getCause() != null ? exception.getCause() : exception;
            throw new OMSRuntimeException("-1", e);
        }
        notifyListeners();
        return result;
    }

    private boolean isDoing() {
        return state.isDoingState();
    }

    private boolean done() {
        synchronized (lock) {
            if (!isDoing()) {
                return false;
            }

            state = FutureState.DONE;
            lock.notifyAll();
        }

        notifyListeners();
        return true;
    }

    /** 安全调用监听器，捕获回调中的异常。 */
    private void notifyListener(final FutureListener<V> listener) {
        try {
            listener.operationComplete(this);
        } catch (Throwable t) {
            LOG.error("notifyListener {} Error:{}", listener.getClass().getSimpleName(), t);
        }
    }

    private boolean cancel(Exception e) {
        synchronized (lock) {
            if (!isDoing()) {
                return false;
            }

            state = FutureState.CANCELLED;
            exception = e;
            lock.notifyAll();
        }

        notifyListeners();
        return true;
    }
}

