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
 * 消息拉取被禁止的原因码常量。
 */
public interface ForbiddenType {

    /** 1=Broker 全局禁止拉取。 */
    /** Broker 级禁止。 */
    int BROKER_FORBIDDEN               = 1;
    /** 2=消费者组被禁止。 */
    /** 消费组级禁止。 */
    int GROUP_FORBIDDEN                = 2;
    /** 3=Topic 被禁止。 */
    /** Topic 级禁止。 */
    int TOPIC_FORBIDDEN                = 3;
    /** 4=广播模式被禁止。 */
    /** 广播消费禁止。 */
    int BROADCASTING_DISABLE_FORBIDDEN = 4;
    /** 5=特定订阅（组+Topic）被禁止。 */
    /** 订阅级禁止。 */
    int SUBSCRIPTION_FORBIDDEN         = 5;

}
