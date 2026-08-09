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

package org.apache.rocketmq.proxy.service.channel;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 单次 Remoting 调用的上下文：持有响应 CompletableFuture 与创建时间戳。
 */
public class InvocationContext implements InvocationContextInterface {
    /** 异步响应 Future，由 handle 完成。 */
    private final CompletableFuture<RemotingCommand> response;
    /** 上下文创建时间，用于超时判定。 */
    private final long timestamp = System.currentTimeMillis();

    /** @param resp 待完成的响应 Future */
    public InvocationContext(CompletableFuture<RemotingCommand> resp) {
        this.response = resp;
    }

    /** 判断自创建起是否已超过 expiredTimeSec 秒。 */
    public boolean expired(long expiredTimeSec) {
        return System.currentTimeMillis() - timestamp >= Duration.ofSeconds(expiredTimeSec).toMillis();
    }

    /** 返回关联的响应 Future。 */
    public CompletableFuture<RemotingCommand> getResponse() {
        return response;
    }

    /** 收到响应后以 remotingCommand 完成 Future。 */
    public void handle(RemotingCommand remotingCommand) {
        response.complete(remotingCommand);
    }
}
