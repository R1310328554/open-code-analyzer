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

/**
 * $Id: ConsumeType.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.heartbeat;

/**
 * 客户端消费类型枚举：区分主动 Pull、被动 Push 与 Pop 消费模式。
 */
public enum ConsumeType {

    /** 主动消费（Pull）。 */
    CONSUME_ACTIVELY("PULL"),

    /** 被动消费（Push）。 */
    CONSUME_PASSIVELY("PUSH"),

    /** Pop 消费模式。 */
    CONSUME_POP("POP");

    /** 类型中文标识串。 */
    private String typeCN;

    /** 指定类型标识串。 */
    ConsumeType(String typeCN) {
        this.typeCN = typeCN;
    }

    /** 返回类型标识串。 */
    public String getTypeCN() {
        return typeCN;
    }
}
