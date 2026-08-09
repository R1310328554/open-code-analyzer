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
package org.apache.rocketmq.common.message;

/**
 * 消息拉取请求模式：PULL 传统拉取，POP 共享队列的 Pop 消费。
 */
public enum MessageRequestMode {

    /** 传统 Pull 拉取模式。 */
    /** Pull 拉取。 */
    PULL("PULL"),

    /** Pop 模式：多消费者可共享同一 MessageQueue。 */
    /** Pop 消费。 */
    POP("POP");

    /** 模式名称字符串。 */
    private String name;

    MessageRequestMode(String name) {
        this.name = name;
    }

    /** 返回模式名。 */
    public String getName() {
        return name;
    }
}
