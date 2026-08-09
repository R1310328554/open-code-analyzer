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
 * 请求来源标识：区分 SDK 直连与 Proxy 各类转发模式。
 */
public enum RequestSource {

    /** 原生 SDK 直连。 */
    SDK(-1),
    /** Proxy 顺序消息转发。 */
    PROXY_FOR_ORDER(0),
    /** Proxy 广播消费转发。 */
    PROXY_FOR_BROADCAST(1),
    /** Proxy 流式消费转发。 */
    PROXY_FOR_STREAM(2);

    /** JVM 系统属性键，用于覆盖默认请求来源。 */
    public static final String SYSTEM_PROPERTY_KEY = "rocketmq.requestSource";
    /** 协议层整型来源码。 */
    private final int value;

    /** 绑定来源码。 */
    RequestSource(int value) {
        this.value = value;
    }

    /** 返回来源码。 */
    public int getValue() {
        return value;
    }

    /** 判断整型值是否在已知来源范围内。 */
    public static boolean isValid(Integer value) {
        return null != value && value >= -1 && value < RequestSource.values().length - 1;
    }

    /** 解析整型来源码，非法时回退为 {@link #SDK}。 */
    public static RequestSource parseInteger(Integer value) {
        if (isValid(value)) {
            return RequestSource.values()[value + 1];
        }
        return SDK;
    }
}
