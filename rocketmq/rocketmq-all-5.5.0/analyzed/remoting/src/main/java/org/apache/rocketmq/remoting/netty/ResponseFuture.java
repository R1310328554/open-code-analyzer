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
package org.apache.rocketmq.remoting.netty;

import io.netty.channel.Channel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.rocketmq.remoting.InvokeCallback;
import org.apache.rocketmq.remoting.common.SemaphoreReleaseOnlyOnce;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 异步 RPC 响应 Future：同步等待或回调 {@link InvokeCallback}，并管理信号量释放。
 */
public class ResponseFuture {
    /** 关联的 Netty 通道。 */
    private final Channel channel;
    /** 请求 opaque 关联 ID。 */
    private final int opaque;
    private final RemotingCommand request;
    /** 超时毫秒数。 */
    private final long timeoutMillis;
    private final InvokeCallback invokeCallback;
    private final long beginTimestamp = System.currentTimeMillis();
    /** 同步等待响应的闭锁。 */
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final SemaphoreReleaseOnlyOnce once;

    /** 保证回调仅执行一次。 */
    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);
    private volatile RemotingCommand responseCommand;
    private volatile boolean sendRequestOK = true;
    private volatile Throwable cause;
    private volatile boolean interrupted = false;

    public ResponseFuture(Channel channel, int opaque, long timeoutMillis, InvokeCallback invokeCallback,
                          SemaphoreReleaseOnlyOnce once) {
        this(channel, opaque, null, timeoutMillis, invokeCallback, once);
    }

    public ResponseFuture(Channel channel, int opaque, RemotingCommand request, long timeoutMillis, InvokeCallback invokeCallback,
                          SemaphoreReleaseOnlyOnce once) {
        this.channel = channel;
        this.opaque = opaque;
        this.request = request;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.once = once;
    }

    /** 按成功/失败/超时分支触发 {@link InvokeCallback} 并释放资源。 */
    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            if (this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
                RemotingCommand response = getResponseCommand();
                if (response != null) {
                    invokeCallback.operationSucceed(response);
                } else {
                    if (!isSendRequestOK()) {
                        invokeCallback.operationFail(new RemotingSendRequestException(channel.remoteAddress().toString(), getCause()));
                    } else if (isTimeout()) {
                        invokeCallback.operationFail(new RemotingTimeoutException(channel.remoteAddress().toString(), getTimeoutMillis(), getCause()));
                    } else {
                        invokeCallback.operationFail(new RemotingException(getRequestCommand().toString(), getCause()));
                    }
                }
                invokeCallback.operationComplete(this);
            }
        }
    }

    /** 标记中断并立即执行回调。 */
    public void interrupt() {
        interrupted = true;
        executeInvokeCallback();
    }

    /** 释放关联的异步信号量（若存在）。 */
    public void release() {
        if (this.once != null) {
            this.once.release();
        }
    }

    /** 判断是否已超过 timeoutMillis。 */
    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    /** 阻塞等待响应，超时返回 null 或已设置的 responseCommand。 */
    public RemotingCommand waitResponse(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    /** 设置响应并唤醒等待线程。 */
    public void putResponse(final RemotingCommand responseCommand) {
        this.responseCommand = responseCommand;
        this.countDownLatch.countDown();
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public RemotingCommand getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(RemotingCommand responseCommand) {
        this.responseCommand = responseCommand;
    }

    public int getOpaque() {
        return opaque;
    }

    public RemotingCommand getRequestCommand() {
        return request;
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    @Override
    public String toString() {
        return "ResponseFuture [responseCommand=" + responseCommand + ", sendRequestOK=" + sendRequestOK
            + ", cause=" + cause + ", opaque=" + opaque + ", timeoutMillis=" + timeoutMillis
            + ", invokeCallback=" + invokeCallback + ", beginTimestamp=" + beginTimestamp
            + ", countDownLatch=" + countDownLatch + "]";
    }
}
