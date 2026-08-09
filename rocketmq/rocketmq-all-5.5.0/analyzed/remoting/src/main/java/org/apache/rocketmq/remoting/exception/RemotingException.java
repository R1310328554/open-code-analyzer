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
 * Remoting 层通用受检异常基类，涵盖连接、发送、超时与命令处理等错误。
 */
public class RemotingException extends Exception {
    private static final long serialVersionUID = -5690687334570505110L;

    /** 以描述信息构造异常。 */
    public RemotingException(String message) {
        super(message);
    }

    /** 以描述信息与根因构造异常。 */
    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}
