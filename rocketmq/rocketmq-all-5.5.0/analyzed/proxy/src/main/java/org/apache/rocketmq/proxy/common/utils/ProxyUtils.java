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
package org.apache.rocketmq.proxy.common.utils;

/**
 * Proxy 通用常量与工具占位类。
 */
public class ProxyUtils {

    /** 单次 POP 请求允许拉取的最大消息条数。 */
    public static final int MAX_MSG_NUMS_FOR_POP_REQUEST = 32;

    /** 上下文或属性中 Broker 地址的键名。 */
    public static final String BROKER_ADDR = "brokerAddr";
}
