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
package org.apache.rocketmq.remoting.exception;

/**
 * Remoting 连接异常：无法与远端地址建立 TCP 连接时抛出。
 */
public class RemotingConnectException extends RemotingException {
    private static final long serialVersionUID = -5565366231695911316L;

    /** 连接 {@code addr} 失败。 */
    public RemotingConnectException(String addr) {
        this(addr, null);
    }

    /** 连接 {@code addr} 失败，并携带底层 I/O 异常。 */
    public RemotingConnectException(String addr, Throwable cause) {
        super("connect to " + addr + " failed", cause);
    }
}
