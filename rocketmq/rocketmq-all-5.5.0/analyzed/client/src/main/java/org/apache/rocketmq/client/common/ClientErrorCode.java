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

package org.apache.rocketmq.client.common;

/**
 * 客户端本地错误码常量：连接 Broker、NameServer 超时与 Topic 不存在等场景。
 */
public class ClientErrorCode {
    /** 连接 Broker 失败。 */
    public static final int CONNECT_BROKER_EXCEPTION = 10001;
    /** 访问 Broker 超时。 */
    public static final int ACCESS_BROKER_TIMEOUT = 10002;
    /** Broker 不存在。 */
    public static final int BROKER_NOT_EXIST_EXCEPTION = 10003;
    /** 未配置或无法连接 NameServer。 */
    public static final int NO_NAME_SERVER_EXCEPTION = 10004;
    /** Topic 路由未找到。 */
    public static final int NOT_FOUND_TOPIC_EXCEPTION = 10005;
    /** 请求超时。 */
    public static final int REQUEST_TIMEOUT_EXCEPTION = 10006;
    /** 创建 Reply 消息失败。 */
    public static final int CREATE_REPLY_MESSAGE_EXCEPTION = 10007;
}