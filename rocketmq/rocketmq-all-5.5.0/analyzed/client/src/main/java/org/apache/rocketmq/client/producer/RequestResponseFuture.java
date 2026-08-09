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

package org.apache.rocketmq.client.producer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.common.message.Message;

/**
 * Request-Reply 模式的单次请求上下文：持有 correlationId、超时与 CountDownLatch，
 * 用于同步等待或异步回调响应消息。
 */
public class RequestResponseFuture {
    /** 请求关联 ID，与 Reply 消息中的 correlationId 对应。 */
    private final String correlationId;
    /** 异步回调；为 null 时仅支持同步 wait。 */
    private final RequestCallback requestCallback;
    /** 请求创建时间戳，用于判断是否超时。 */
    private final long beginTimestamp = System.currentTimeMillis();
    /** 预留的请求消息引用（当前实现恒为 null）。 */
    private final Message requestMsg = null;
    /** 超时阈值（毫秒）。 */
    private long timeoutMillis;
    /** 响应到达时 countDown，唤醒等待线程。 */
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    /** 服务端返回的响应消息。 */
    private volatile Message responseMsg = null;
    /** 请求是否成功发出（网络层）。 */
    private volatile boolean sendRequestOk = true;
    /** 失败时的异常原因。 */
    private volatile Throwable cause = null;

    /** 构造 Request-Reply Future。 */
    public RequestResponseFuture(String correlationId, long timeoutMillis, RequestCallback requestCallback) {
        this.correlationId = correlationId;
        this.timeoutMillis = timeoutMillis;
        this.requestCallback = requestCallback;
    }

    /** 根据发送结果调用成功或异常回调。 */
    public void executeRequestCallback() {
        if (requestCallback != null) {
            if (sendRequestOk && cause == null) {
                requestCallback.onSuccess(responseMsg);
            } else {
                requestCallback.onException(cause);
            }
        }
    }

    /** 判断是否已超过 timeoutMillis。 */
    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    /** 阻塞等待响应，超时后返回当前 responseMsg（可能为 null）。 */
    public Message waitResponseMessage(final long timeout) throws InterruptedException {
        this.countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        return this.responseMsg;
    }

    /** 写入响应并唤醒等待线程。 */
    public void putResponseMessage(final Message responseMsg) {
        this.responseMsg = responseMsg;
        this.countDownLatch.countDown();
    }

    /** 返回 correlationId。 */
    public String getCorrelationId() {
        return correlationId;
    }

    /** 返回超时毫秒数。 */
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    /** 设置超时毫秒数。 */
    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /** 返回异步回调。 */
    public RequestCallback getRequestCallback() {
        return requestCallback;
    }

    /** 返回请求开始时间戳。 */
    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    /** 返回同步等待用的 CountDownLatch。 */
    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    /** 替换 CountDownLatch（测试或特殊场景）。 */
    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    /** 返回响应消息。 */
    public Message getResponseMsg() {
        return responseMsg;
    }

    /** 设置响应消息。 */
    public void setResponseMsg(Message responseMsg) {
        this.responseMsg = responseMsg;
    }

    /** 请求是否成功发出。 */
    public boolean isSendRequestOk() {
        return sendRequestOk;
    }

    /** 标记请求发送是否成功。 */
    public void setSendRequestOk(boolean sendRequestOk) {
        this.sendRequestOk = sendRequestOk;
    }

    /** 返回请求消息引用。 */
    public Message getRequestMsg() {
        return requestMsg;
    }

    /** 返回失败异常。 */
    public Throwable getCause() {
        return cause;
    }

    /** 设置失败异常。 */
    public void setCause(Throwable cause) {
        this.cause = cause;
    }
}
