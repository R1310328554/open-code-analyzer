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
package org.apache.rocketmq.client.consumer;

/**
 * Pull 任务执行上下文：控制下次 pull 延迟并持有当前 {@link MQPullConsumer} 引用。
 */
public class PullTaskContext {

    /** 下次 pull 延迟毫秒数，默认 200ms。 */
    private int pullNextDelayTimeMillis = 200;

    /** 关联的 Pull 消费者实例。 */
    private MQPullConsumer pullConsumer;

    /** 返回下次 pull 延迟。 */
    public int getPullNextDelayTimeMillis() {
        return pullNextDelayTimeMillis;
    }

    /** 设置下次 pull 延迟。 */
    public void setPullNextDelayTimeMillis(int pullNextDelayTimeMillis) {
        this.pullNextDelayTimeMillis = pullNextDelayTimeMillis;
    }

    /** 返回 Pull 消费者。 */
    public MQPullConsumer getPullConsumer() {
        return pullConsumer;
    }

    /** 绑定 Pull 消费者。 */
    public void setPullConsumer(MQPullConsumer pullConsumer) {
        this.pullConsumer = pullConsumer;
    }
}
