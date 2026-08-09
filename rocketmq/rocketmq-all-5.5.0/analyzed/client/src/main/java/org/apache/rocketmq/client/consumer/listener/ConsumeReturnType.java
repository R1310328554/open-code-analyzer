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

package org.apache.rocketmq.client.consumer.listener;

/**
 * 消费结果类型：用于统计与监控区分成功、超时、异常等场景。
 */
public enum ConsumeReturnType {
    /** 正常消费成功。 */
    SUCCESS,
    /** 消费超时（即使业务逻辑已成功）。 */
    TIME_OUT,
    /** 消费过程抛出异常。 */
    EXCEPTION,
    /** listener 返回 null。 */
    RETURNNULL,
    /** 消费明确失败。 */
    FAILED
}
