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
package org.apache.rocketmq.store.exception;

/**
 * 消费队列相关异常：封装 ConsumeQueue 读写或索引操作失败。
 */
public class ConsumeQueueException extends StoreException {
    /** 无参构造。 */
    public ConsumeQueueException() {
    }

    /** 以消息构造异常。 */
    public ConsumeQueueException(String message) {
        super(message);
    }

    /** 以消息与原因构造异常。 */
    public ConsumeQueueException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 以原因构造异常。 */
    public ConsumeQueueException(Throwable cause) {
        super(cause);
    }

    public ConsumeQueueException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
