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
package org.apache.rocketmq.remoting;

import org.apache.rocketmq.remoting.netty.ResponseFuture;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 异步 Remoting 调用完成回调。
 */
public interface InvokeCallback {
    /**
     * 在 {@link #operationSucceed(RemotingCommand)} 或 {@link #operationFail(Throwable)} 之后调用。
     *
     * @param responseFuture 包含响应或异常的 Future 包装对象
     */
    void operationComplete(final ResponseFuture responseFuture);

    /** 异步调用成功时的默认空实现。 */
    default void operationSucceed(final RemotingCommand response) {

    }

    /** 异步调用失败时的默认空实现。 */
    default void operationFail(final Throwable throwable) {

    }
}
