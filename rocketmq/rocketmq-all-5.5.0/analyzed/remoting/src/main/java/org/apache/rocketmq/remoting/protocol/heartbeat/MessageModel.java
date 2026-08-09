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
 * $Id: MessageModel.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.heartbeat;

/**
 * 消息模型枚举：广播、集群与 Lite 选择性消费。
 */
public enum MessageModel {
    /** 广播模式：每条消息投递到组内所有消费者。 */
    BROADCASTING("BROADCASTING"),
    /** 集群模式：同组内消息负载均衡消费。 */
    CLUSTERING("CLUSTERING"),
    /** Lite 消费者的选择性消费模式。 */
    LITE_SELECTIVE("LITE_SELECTIVE");

    /** 模式中文标识串。 */
    private String modeCN;

    /** 指定模式标识串。 */
    MessageModel(String modeCN) {
        this.modeCN = modeCN;
    }

    /** 返回模式标识串。 */
    public String getModeCN() {
        return modeCN;
    }
}
