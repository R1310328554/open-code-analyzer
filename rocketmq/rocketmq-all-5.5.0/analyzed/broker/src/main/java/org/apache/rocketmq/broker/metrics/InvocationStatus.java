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

package org.apache.rocketmq.broker.metrics;

/**
 * RPC 调用结果状态，用作指标标签 {@code invocation_status} 的取值。
 */
public enum InvocationStatus {
    /** 调用成功。 */
    SUCCESS("success"),
    /** 调用失败。 */
    FAILURE("failure");

    private final String name;

    InvocationStatus(String name) {
        this.name = name;
    }

    /** 返回指标标签字符串值。 */
    public String getName() {
        return name;
    }
}