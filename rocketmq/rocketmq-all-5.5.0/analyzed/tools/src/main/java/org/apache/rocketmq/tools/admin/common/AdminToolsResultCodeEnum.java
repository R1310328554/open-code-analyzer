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
package org.apache.rocketmq.tools.admin.common;

/**
 * 管理工具结果码枚举：区分成功与各类远程/业务错误。
 */
public enum AdminToolsResultCodeEnum {

    /** 操作成功。 */
    SUCCESS(200),

    /** Remoting 通信异常。 */
    REMOTING_ERROR(-1001),
    /** Broker 端返回错误。 */
    MQ_BROKER_ERROR(-1002),
    /** 客户端异常。 */
    MQ_CLIENT_ERROR(-1003),
    /** 线程中断异常。 */
    INTERRUPT_ERROR(-1004),

    /** Topic 路由信息不存在。 */
    TOPIC_ROUTE_INFO_NOT_EXIST(-2001),
    /** 消费端不在线。 */
    CONSUMER_NOT_ONLINE(-2002),
    /** 广播消费模式下无法精确追踪。 */
    BROADCAST_CONSUMPTION(-2003);

    /** 数值结果码。 */
    private int code;

    /** 绑定数值码。 */
    AdminToolsResultCodeEnum(int code) {
        this.code = code;
    }

    /** 返回数值结果码。 */
    public int getCode() {
        return code;
    }
}
