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

package org.apache.rocketmq.remoting.protocol;

/**
 * Remoting 层系统级响应码，与业务 {@link ResponseCode} 区分。
 */
public class RemotingSysResponseCode {

    /** 处理成功。 */
    public static final int SUCCESS = 0;

    /** 服务端未捕获异常。 */
    public static final int SYSTEM_ERROR = 1;

    /** 服务端流控或线程池饱和。 */
    public static final int SYSTEM_BUSY = 2;

    /** 未知或未注册的 RequestCode。 */
    public static final int REQUEST_CODE_NOT_SUPPORTED = 3;

    /** 事务消息处理失败。 */
    public static final int TRANSACTION_FAILED = 4;
}
