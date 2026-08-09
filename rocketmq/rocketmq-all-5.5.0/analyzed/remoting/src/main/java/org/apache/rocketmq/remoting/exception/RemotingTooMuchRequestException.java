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
 * Remoting 并发限流异常：异步/oneway 请求超过信号量许可上限时抛出。
 */
public class RemotingTooMuchRequestException extends RemotingException {
    private static final long serialVersionUID = 4326919581254519654L;

    /** 以限流说明信息构造异常。 */
    public RemotingTooMuchRequestException(String message) {
        super(message);
    }
}
