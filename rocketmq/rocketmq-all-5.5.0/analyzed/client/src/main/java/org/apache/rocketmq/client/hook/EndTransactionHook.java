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
package org.apache.rocketmq.client.hook;

/**
 * 事务消息结束钩子：在 Producer 向 Broker 提交或回滚半消息后触发，
 * 用于监控、审计事务消息生命周期。
 */
public interface EndTransactionHook {
    /** 返回钩子唯一名称。 */
    String hookName();

    /** 事务结束（提交/回滚）时回调。 */
    void endTransaction(final EndTransactionContext context);
}
