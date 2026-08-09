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
 * Remoting 命令异常：序列化/反序列化或扩展头字段校验失败时抛出。
 */
public class RemotingCommandException extends RemotingException {
    private static final long serialVersionUID = -6061365915274953096L;

    /** 以描述信息构造异常。 */
    public RemotingCommandException(String message) {
        super(message, null);
    }

    /** 以描述信息与根因构造异常。 */
    public RemotingCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
